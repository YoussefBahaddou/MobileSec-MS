
from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.models.metadata import APKMetadata
from app.api.deps import get_current_user
import math

router = APIRouter()

@router.get("/results")
def list_analysis_results(
    page: int = 0,
    size: int = 10,
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user)
):
    """
    Paginated list of analysis results for the current user.
    Mimics Spring Data Page interface for frontend compatibility.
    """
    offset = page * size
    
    # Query for total count (for filtering)
    query = db.query(APKMetadata).filter(APKMetadata.user_id == user_id)
    total_elements = query.count()
    
    # Calculate pages
    total_pages = math.ceil(total_elements / size) if size > 0 else 0
    
    # Query for content
    results = query.order_by(APKMetadata.created_at.desc()).offset(offset).limit(size).all()
    
    # Map to frontend model (if needed, or return raw)
    # The frontend uses fields like: id, fileName, scanDate, score, etc.
    # APKMetadata cols: scan_id, file_name, created_at, etc.
    # We might need to map keys to match what 'ResultsPage.js' expects from the old 'Analysis Service'.
    # Old AnalysisResult.java had: id, fileName, fileHash, uploadedAt, securityScore, etc.
    # Frontend props: result.id, result.fileName, result.securityScore
    
    content = []
    for r in results:
        # Calculate a score if not present (simple placeholder logic)
        # Using the same logic as dashboard:
        score = 100
        if r.is_debuggable: score -= 20
        if r.allow_backup: score -= 20
        if r.uses_cleartext_traffic: score -= 20
        if score < 0: score = 0
        
        content.append({
            "id": r.scan_id, # Frontend expects 'id'
            "scan_id": r.scan_id,
            "fileName": r.file_name, # Frontend expects camelCase
            "file_name": r.file_name,
            "package_name": r.package_name,
            "uploadedAt": r.created_at.isoformat(), # Frontend expects 'uploadedAt'
            "created_at": r.created_at,
            "securityScore": score, # Frontend expects 'securityScore'
            "riskLevel": "HIGH" if score < 60 else ("MEDIUM" if score < 80 else "LOW"),
            "status": "COMPLETED"
        })

    return {
        "content": content,
        "totalPages": total_pages,
        "totalElements": total_elements,
        "size": size,
        "number": page,
        "numberOfElements": len(content),
        "first": page == 0,
        "last": page >= total_pages - 1 if total_pages > 0 else True,
        "empty": len(content) == 0
    }
