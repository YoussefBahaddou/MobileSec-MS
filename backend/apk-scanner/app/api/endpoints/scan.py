from fastapi import APIRouter, UploadFile, File, Depends, HTTPException
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.services.scanner import APKScannerService
from app.models.metadata import APKMetadata
import shutil
import os
import uuid

router = APIRouter()

@router.post("/analyze")
async def analyze_apk(file: UploadFile = File(...), db: Session = Depends(get_db)):
    # 1. Save uploaded file temporarily
    scan_id = str(uuid.uuid4())
    temp_file = f"temp_{scan_id}.apk"
    
    try:
        with open(temp_file, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
            
        # 2. Analyze
        results = APKScannerService.analyze_apk(temp_file)
        
        # 3. Save to DB
        metadata = APKMetadata(
            scan_id=scan_id,
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
            uses_cleartext_traffic=results["uses_cleartext_traffic"]
        )
        db.add(metadata)
        db.commit()
        db.refresh(metadata)
        
        return {"scan_id": scan_id, "status": "completed", "package": results["package_name"]}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        # Cleanup
        if os.path.exists(temp_file):
            os.remove(temp_file)

@router.get("/{scan_id}")
def get_results(scan_id: str, db: Session = Depends(get_db)):
    result = db.query(APKMetadata).filter(APKMetadata.scan_id == scan_id).first()
    if not result:
        raise HTTPException(status_code=404, detail="Scan not found")
    return result
