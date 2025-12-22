from fastapi import APIRouter, File, UploadFile, HTTPException, Form
from typing import List, Optional
from app.services.scanner import SecretScannerService
import shutil
import os
import tempfile
import logging
from fastapi import UploadFile


logger = logging.getLogger(__name__)


def _seek_file_length(upload_file: UploadFile) -> int:
    file_obj = upload_file.file
    original_pos = file_obj.tell()
    try:
        file_obj.seek(0, os.SEEK_END)
        size = file_obj.tell()
        return size
    finally:
        file_obj.seek(original_pos, os.SEEK_SET)

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
    normalized_text = text.strip() if text and text.strip() else None
    logger.info("SecretHunter analyze request received; text=%s, file=%s",
                "present" if normalized_text else "empty",
                file.filename if file else "none")

    # 1. Scan Text Input
    try:
        if normalized_text:
            text_findings = SecretScannerService.scan_text(normalized_text)
            findings.extend(text_findings)
        # 2. Scan File
        file_to_scan = file if file and file.filename else None
        if file_to_scan:
            logger.info("Scanning uploaded file %s (%d bytes)", file.filename, _seek_file_length(file))
            try:
                with tempfile.NamedTemporaryFile(delete=False, suffix=f"_{file.filename}") as tmp:
                    shutil.copyfileobj(file.file, tmp)
                    tmp_path = tmp.name

                file_findings = SecretScannerService.scan_file(tmp_path)
                findings.extend(file_findings)

                os.remove(tmp_path)
            except Exception as e:
                logger.exception("Error scanning file %s", file.filename)
                raise HTTPException(status_code=500, detail=f"File scanning failed: {str(e)}")
    except HTTPException:
        raise
    except Exception as exc:
        logger.exception("Unexpected error while scanning request")
        raise HTTPException(status_code=500, detail="An unexpected error occurred while scanning.") from exc

    if not normalized_text and not (file and file.filename):
        raise HTTPException(status_code=400, detail=f"Provide 'file' or 'text' form fields.")

    return {
        "status": "completed",
        "findings_count": len(findings),
        "findings": findings
    }
