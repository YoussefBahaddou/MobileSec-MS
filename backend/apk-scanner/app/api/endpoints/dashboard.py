from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func
from app.db.session import get_db
from app.models.metadata import APKMetadata
from app.api.deps import get_current_user
from typing import List

router = APIRouter()

@router.get("/stats")
def get_dashboard_stats(db: Session = Depends(get_db), user_id: str = Depends(get_current_user)):
    """
    Returns aggregated statistics for the dashboard.
    """
    try:
        total_scans = db.query(APKMetadata).filter(APKMetadata.user_id == user_id).count()
        
        # Simple risk logic based on flags
        # In a real app, this might query a RiskAssessment table
        scans = db.query(APKMetadata).filter(APKMetadata.user_id == user_id).all()
        
        high_risk = 0
        medium_risk = 0
        low_risk = 0
        
        recent_scans = []
        
        pass # sorted_scans calculation
        
        sorted_scans = sorted(scans, key=lambda x: x.created_at, reverse=True)
        
        for s in sorted_scans:
            risk = "LOW"
            if s.is_debuggable or s.uses_cleartext_traffic:
                risk = "HIGH"
                high_risk += 1
            elif s.allow_backup:
                risk = "MEDIUM"
                medium_risk += 1
            else:
                low_risk += 1
    
            recent_scans.append({
                "id": s.scan_id,
                "packageName": s.package_name,
                "versionName": s.version_code,
                "riskLevel": risk,
                "createdAt": s.created_at
            })
            
        return {
            "totalScans": total_scans,
            "highRiskCount": high_risk,
            "mediumRiskCount": medium_risk,
            "lowRiskCount": low_risk,
            "recentScans": recent_scans[:10] # Top 10
        }
    except Exception as e:
        import traceback
        import logging
        logger = logging.getLogger(__name__)
        logger.error(f"Dashboard Stats Failed: {str(e)}")
        logger.error(traceback.format_exc())
        raise e
