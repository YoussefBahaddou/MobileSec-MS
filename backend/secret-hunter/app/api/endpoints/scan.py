from fastapi import APIRouter, File, UploadFile, HTTPException, Form
from typing import List, Optional
from app.services.scanner import SecretScannerService
import shutil
import os
import tempfile

router = APIRouter()

@router.post("/analyze")
async def analyze_content(
    text: Optional[str] = Form(None),
    file: Optional[UploadFile] = File(None)
):
    """
    Scans uploaded file OR text for secrets.
    """
    findings = []
    
    # 1. Scan Text Input
    if text:
        text_findings = SecretScannerService.scan_text(text)
        findings.extend(text_findings)
        
    # 2. Scan File
    if file:
        try:
            # Save tmp file to scan
            with tempfile.NamedTemporaryFile(delete=False, suffix=f"_{file.filename}") as tmp:
                shutil.copyfileobj(file.file, tmp)
                tmp_path = tmp.name
                
            file_findings = SecretScannerService.scan_file(tmp_path)
            findings.extend(file_findings)
            
            # Clean up
            os.remove(tmp_path)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"File scanning failed: {str(e)}")

    if not text and not file:
        raise HTTPException(status_code=400, detail=f"Provide 'file' or 'text' form fields.")

    return {
        "status": "completed",
        "findings_count": len(findings),
        "findings": findings
    }
