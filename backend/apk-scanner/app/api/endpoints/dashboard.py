from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException
from app.api.deps import get_current_user
from app.services.firestore_storage import list_reports, FirebaseError

router = APIRouter()


def _compute_risk_level(report: dict) -> str:
    if report.get("is_debuggable") or report.get("uses_cleartext_traffic"):
        return "HIGH"
    if report.get("allow_backup"):
        return "MEDIUM"
    return "LOW"


def _parse_created_at(report: dict) -> datetime:
    created_at = report.get("created_at") or report.get("createdAt")
    if isinstance(created_at, str):
        try:
            return datetime.fromisoformat(created_at)
        except ValueError:
            pass
    return datetime.utcnow()


@router.get("/stats")
def get_dashboard_stats(user_id: str = Depends(get_current_user)):
    """
    Returns aggregated statistics for the dashboard.
    """
    try:
        scans = list_reports(user_id=user_id)
    except FirebaseError as exc:
        raise HTTPException(status_code=500, detail="Unable to fetch dashboard stats") from exc

    total_scans = len(scans)
    high_risk = 0
    medium_risk = 0
    low_risk = 0
    recent_scans = []

    sorted_scans = sorted(scans, key=_parse_created_at, reverse=True)

    for report in sorted_scans:
        risk = _compute_risk_level(report)
        if risk == "HIGH":
            high_risk += 1
        elif risk == "MEDIUM":
            medium_risk += 1
        else:
            low_risk += 1

        recent_scans.append({
            "id": report.get("scan_id"),
            "packageName": report.get("package_name"),
            "versionName": report.get("version_name") or report.get("version_code"),
            "riskLevel": risk,
            "createdAt": _parse_created_at(report).isoformat(),
        })

    return {
        "totalScans": total_scans,
        "highRiskCount": high_risk,
        "mediumRiskCount": medium_risk,
        "lowRiskCount": low_risk,
        "recentScans": recent_scans[:10],
    }
