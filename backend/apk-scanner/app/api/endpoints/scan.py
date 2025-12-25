
import os
import uuid
import aiofiles
import logging
from datetime import datetime, timezone

from fastapi import APIRouter, UploadFile, File, Depends, HTTPException

from app.api.deps import get_current_user
from app.services.scanner import APKScannerService
from app.services.firestore_storage import (
    FirebaseError,
    get_report,
    list_recent_reports,
    upload_report,
)

router = APIRouter()
logger = logging.getLogger(__name__)


@router.get("/recents")
def get_recent_scans(limit: int = 10, user_id: str = Depends(get_current_user)):
    """
    Returns the most recent scans for the logged-in user stored in Firebase Storage.
    """
    try:
        reports = list_recent_reports(limit=limit, user_id=user_id)
    except FirebaseError as exc:
        logger.error("Failed to list recent reports from Firebase: %s", exc)
        raise HTTPException(status_code=500, detail="Unable to fetch recent scans") from exc

    return [
        {
            "scan_id": report.get("scan_id"),
            "package_name": report.get("package_name"),
            "version_code": report.get("version_code"),
            "created_at": report.get("created_at"),
            "is_debuggable": report.get("is_debuggable"),
            "allow_backup": report.get("allow_backup"),
            "uses_cleartext_traffic": report.get("uses_cleartext_traffic"),
        }
        for report in reports
    ]

@router.post("/analyze")
async def analyze_apk(
    file: UploadFile = File(...),
    user_id: str = Depends(get_current_user),
):
    scan_id = str(uuid.uuid4())
    temp_file = f"temp_{scan_id}.apk"

    try:
        contents = await file.read()
        async with aiofiles.open(temp_file, "wb") as buffer:
            await buffer.write(contents)

        results = APKScannerService.analyze_apk(temp_file, original_filename=file.filename)

        payload = {
            "scan_id": scan_id,
            "user_id": user_id,
            "file_name": file.filename,
            "package_name": results["package_name"],
            "version_code": results["version_code"],
            "version_name": results.get("version_name", results["version_code"]),
            "permissions": results.get("permissions"),
            "exported_activities": results.get("exported_activities", []),
            "exported_services": results.get("exported_services", []),
            "exported_receivers": results.get("exported_receivers", []),
            "exported_providers": results.get("exported_providers", []),
            "is_debuggable": results.get("is_debuggable", False),
            "allow_backup": results.get("allow_backup", True),
            "uses_cleartext_traffic": results.get("uses_cleartext_traffic", False),
            "created_at": datetime.now(timezone.utc).isoformat(),
            "manifest": results,
        }
        upload_report(scan_id, payload)

        return {
            "scan_id": scan_id,
            "status": "completed",
            "manifest": results,
        }
    except FirebaseError as exc:
        logger.error("Failed to persist scan metadata to Firebase: %s", exc)
        raise HTTPException(status_code=500, detail="Unable to store scan metadata") from exc
    except Exception as exc:
        import traceback

        logger.error(f"Analysis Failed: {str(exc)}")
        logger.error(traceback.format_exc())
        raise HTTPException(status_code=500, detail=str(exc))
    finally:
        if os.path.exists(temp_file):
            os.remove(temp_file)

@router.get("/{scan_id}")
def get_results(scan_id: str, user_id: str = Depends(get_current_user)):
    try:
        report = get_report(scan_id, user_id=user_id)
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="Scan not found")
    except PermissionError:
        raise HTTPException(status_code=403, detail="Not authorized to view this scan")
    except FirebaseError as exc:
        logger.error("Failed to retrieve scan metadata from Firebase: %s", exc)
        raise HTTPException(status_code=500, detail="Unable to retrieve scan metadata") from exc

    return report

@router.post("/extract-strings")
async def extract_strings(
    file: UploadFile = File(...)
):
    """
    Extracts all strings from the APK's DEX files.
    """
    try:
        scan_id = str(uuid.uuid4())
        filename = f"temp_extract_{scan_id}.apk"
        
        contents = await file.read()
        async with aiofiles.open(filename, 'wb') as out_file:
            await out_file.write(contents)
            
        # Extract Strings using Androguard
        from androguard.core.apk import APK
        from androguard.core.dex import DEX
        
        a = APK(filename)
        strings = set()
        
        # Iterate over all dex files
        for d in a.get_all_dex():
            # Androguard may return bytes or Dex object.
            # We assume it returns bytes of the DEX file.
            msg = None
            try:
                dex_obj = DEX(d)
                for s in dex_obj.get_strings():
                    # s is usually bytes or str? Androguard returns str or bytes
                    if isinstance(s, bytes):
                        s = s.decode('utf-8', errors='ignore')
                    if len(s) > 4: # Filter noise
                        strings.add(s)
            except Exception as e:
                logger.warning(f"Failed to parse a dex file: {e}")

        # Cleanup
        if os.path.exists(filename):
            os.remove(filename)
        
        # Limit content size for network
        sorted_strings = sorted(list(strings))
        payload_content = "\\n".join(sorted_strings[:100000])
        clean_content = payload_content.encode("utf-8", errors="ignore").decode("utf-8")
        return {
             "status": "completed",
             "count": len(strings),
             "content": clean_content # Payload limit increased
        }
    except Exception as e:
        logger.error(f"String extraction failed: {str(e)}")
        return {"status": "failed", "error": str(e), "content": ""}
