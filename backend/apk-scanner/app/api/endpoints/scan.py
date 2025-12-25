
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.services.scanner import APKScannerService
from app.models.metadata import APKMetadata
import shutil
import os
import uuid
import aiofiles # Added for async file operations
from fastapi import APIRouter, UploadFile, File, Depends, HTTPException
from app.api.deps import get_current_user
from datetime import datetime # Added for scan_date
import logging # Added for logging

# Initialize logger
logger = logging.getLogger(__name__)

router = APIRouter()


@router.get("/recents")
def get_recent_scans(limit: int = 10, db: Session = Depends(get_db), user_id: str = Depends(get_current_user)):
    """
    Returns the most recent scans for the logged-in user.
    """
    scans = db.query(APKMetadata).filter(APKMetadata.user_id == user_id).order_by(APKMetadata.created_at.desc()).limit(limit).all()
    # Serialize manually if needed, or rely on Pydantic/ORM mode
    return [
        {
            "scan_id": s.scan_id,
            "package_name": s.package_name,
            "version_code": s.version_code,
            "created_at": s.created_at,
            "is_debuggable": s.is_debuggable,
            "allow_backup": s.allow_backup,
            "uses_cleartext_traffic": s.uses_cleartext_traffic
        }
        for s in scans
    ]

@router.post("/analyze")
async def analyze_apk(
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user)
):
    # 1. Save uploaded file temporarily
    scan_id = str(uuid.uuid4())
    temp_file = f"temp_{scan_id}.apk"
    
    try:
        # Use aiofiles for async file write
        # Stream file processing to avoid MemoryError
        async with aiofiles.open(temp_file, "wb") as buffer:
            while content := await file.read(1024 * 1024 * 10): # 10MB chunks
                await buffer.write(content)
            
        # 2. Analyze
        # Pass file.filename to enable simulation checks logic
        results = APKScannerService.analyze_apk(temp_file, original_filename=file.filename)
        
        # 3. Save to DB
        metadata = APKMetadata(
            scan_id=scan_id,
            user_id=user_id,
            file_name=file.filename,
            package_name=results["package_name"],
            version_code=results["version_code"],
            permissions=results["permissions"],
            exported_activities=results.get("exported_activities", []),
            exported_services=results.get("exported_services", []),
            exported_receivers=results.get("exported_receivers", []),
            exported_providers=results.get("exported_providers", []),
            is_debuggable=results["is_debuggable"],
            allow_backup=results["allow_backup"],
            uses_cleartext_traffic=results["uses_cleartext_traffic"],
            created_at=datetime.utcnow()
        )
        db.add(metadata)
        db.commit()
        db.refresh(metadata)
        
        # Match frontend expected format: { scan_id, manifest: { ... } }
        return {
            "scan_id": scan_id,
            "status": "completed",
            "manifest": results
        }
    except Exception as e:
        import traceback
        logger.error(f"Analysis Failed: {str(e)}")
        logger.error(traceback.format_exc())
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        # Cleanup
        if os.path.exists(temp_file):
            os.remove(temp_file)

@router.get("/{scan_id}")
def get_results(scan_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user)):
    result = db.query(APKMetadata).filter(APKMetadata.scan_id == scan_id, APKMetadata.user_id == user_id).first()
    if not result:
        raise HTTPException(status_code=404, detail="Scan not found")
    return result

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
        
        # Stream file processing
        async with aiofiles.open(filename, 'wb') as out_file:
            while content := await file.read(1024 * 1024 * 10):
                await out_file.write(content)
            
        # Validate Zip first to fail fast
        import zipfile
        if not zipfile.is_zipfile(filename):
             return {"status": "failed", "error": "Invalid APK file (Not a valid ZIP)", "content": ""}

        # Extract Strings using Androguard
        # Run in threadpool to prevent blocking the async loop
        import asyncio
        from concurrent.futures import ThreadPoolExecutor
        
        def process_in_thread():
            from androguard.core.apk import APK
            from androguard.core.dex import DEX
            local_strings = set()
            try:
                a = APK(filename)
                for d in a.get_all_dex():
                    try:
                        dex_obj = DEX(d)
                        for s in dex_obj.get_strings():
                            if isinstance(s, bytes):
                                s = s.decode('utf-8', errors='ignore')
                            if len(s) > 4: 
                                local_strings.add(s)
                    except Exception as dex_err:
                        logger.warning(f"Failed to parse a dex file: {dex_err}")
                return local_strings
            except Exception as e:
                raise e

        loop = asyncio.get_event_loop()
        strings = await loop.run_in_executor(None, process_in_thread)

        # Cleanup
        if os.path.exists(filename):
            os.remove(filename)
        
        # Limit content size for network
        sorted_strings = sorted(list(strings))
        return {
             "status": "completed",
             "count": len(strings),
             "content": "\\n".join(sorted_strings[:100000]) # Payload limit increased
        }
    except MemoryError:
        logger.error("MemoryError during string extraction: APK/DEX is too large for RAM.")
        import traceback
        logger.error(traceback.format_exc())
        return {"status": "failed", "error": "Server Memory Limit Exceeded (APK too complex)", "content": ""}
    except Exception as e:
        import traceback
        logger.error(f"String extraction failed: {str(e)}")
        logger.error(traceback.format_exc())
        # Return 200 with error details to avoid generic 500 if possible, 
        # but if this itself fails, FastAPI will send 500.
        return {"status": "failed", "error": f"{type(e).__name__}: {str(e)}", "content": ""}
