
import math
from typing import List, Dict
from fastapi import APIRouter, Depends, HTTPException
from app.api.deps import get_current_user
from app.services.firestore_storage import list_reports, FirebaseError

router = APIRouter()


def _score_report(report: Dict[str, any]) -> int:
    score = 100
    if report.get("is_debuggable"):
        score -= 20
    if report.get("allow_backup"):
        score -= 20
    if report.get("uses_cleartext_traffic"):
        score -= 20
    return max(0, score)


def _calculate_pagination(reports: List[Dict[str, any]], page: int, size: int) -> List[Dict[str, any]]:
    if size <= 0:
        return reports
    start = page * size
    end = start + size
    return reports[start:end]


@router.get("/results")
def list_analysis_results(
    page: int = 0,
    size: int = 10,
    user_id: str = Depends(get_current_user),
):
    """
    Paginated list of analysis results for the current user.
    """
    try:
        reports = list_reports(user_id=user_id)
    except FirebaseError as exc:
        raise HTTPException(status_code=500, detail="Unable to load analysis results") from exc

    total_elements = len(reports)
    paged_reports = _calculate_pagination(reports, page, size)

    content = []
    for report in paged_reports:
        score = _score_report(report)
        content.append({
            "id": report.get("scan_id"),
            "scan_id": report.get("scan_id"),
            "fileName": report.get("file_name") or report.get("fileName"),
            "file_name": report.get("file_name") or report.get("fileName"),
            "package_name": report.get("package_name"),
            "uploadedAt": report.get("created_at") or report.get("createdAt"),
            "created_at": report.get("created_at") or report.get("createdAt"),
            "createdAt": report.get("created_at") or report.get("createdAt"),
            "versionName": report.get("version_name") or report.get("versionName"),
            "versionCode": report.get("version_code") or report.get("versionCode"),
            "securityScore": score,
            "riskLevel": "HIGH" if score < 60 else ("MEDIUM" if score < 80 else "LOW"),
            "status": "COMPLETED"
        })

    total_pages = math.ceil(total_elements / size) if size > 0 else 0

    return {
        "content": content,
        "totalPages": total_pages,
        "totalElements": total_elements,
        "size": size,
        "number": page,
        "numberOfElements": len(content),
        "first": page == 0,
        "last": page >= total_pages - 1 if total_pages > 0 else True,
        "empty": len(content) == 0,
    }
