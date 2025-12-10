from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.models.finding import NetworkFinding
from app.services.proxy_manager import ProxyManager
from pydantic import BaseModel
import uuid

router = APIRouter()

class StartScanRequest(BaseModel):
    scan_id: str | None = None

class ScanStatus(BaseModel):
    is_running: bool
    scan_id: str | None

@router.post("/start", response_model=ScanStatus)
def start_scan(request: StartScanRequest):
    if ProxyManager.is_running():
        raise HTTPException(status_code=400, detail="Proxy is already running")
    
    scan_id = request.scan_id or str(uuid.uuid4())
    ProxyManager.start_proxy(scan_id)
    return {"is_running": True, "scan_id": scan_id}

@router.post("/stop", response_model=ScanStatus)
def stop_scan():
    if not ProxyManager.is_running():
        return {"is_running": False, "scan_id": None}
    
    ProxyManager.stop_proxy()
    return {"is_running": False, "scan_id": None}

@router.get("/status", response_model=ScanStatus)
def get_status():
    return {"is_running": ProxyManager.is_running(), "scan_id": None} # We might want to store active scan ID in ProxyManager

@router.get("/{scan_id}/results")
def get_results(scan_id: str, db: Session = Depends(get_db)):
    findings = db.query(NetworkFinding).filter(NetworkFinding.scan_id == scan_id).all()
    return findings

from app.services.docker_manager import DockerManager

@router.post("/avd/start")
def start_avd():
    """
    Triggers the Docker-based Android Sandbox.
    """
    try:
        result = DockerManager.start_android_container()
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
