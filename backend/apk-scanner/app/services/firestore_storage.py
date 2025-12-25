from __future__ import annotations

from datetime import datetime, timezone
from typing import Any, Dict, List, Optional

import firebase_admin
from firebase_admin import credentials, exceptions as firebase_exceptions, firestore

from app.core.config import settings

_firestore_client: Optional[firestore.Client] = None


def _ensure_firestore_client() -> firestore.Client:
    global _firestore_client
    if _firestore_client:
        return _firestore_client

    kwargs = {}
    if settings.FIREBASE_PROJECT_ID:
        kwargs["projectId"] = settings.FIREBASE_PROJECT_ID

    try:
        cred = (
            credentials.Certificate(settings.FIREBASE_CREDENTIALS_PATH)
            if settings.FIREBASE_CREDENTIALS_PATH
            else credentials.ApplicationDefault()
        )
    except Exception as exc:
        raise RuntimeError("Unable to build Firebase credentials") from exc

    try:
        app = firebase_admin.get_app()
    except ValueError:
        app = firebase_admin.initialize_app(cred, kwargs or None)

    _firestore_client = firestore.client(app=app)
    return _firestore_client


def _normalize_datetime(value: Optional[Any]) -> Optional[str]:
    if isinstance(value, datetime):
        return value.astimezone(timezone.utc).isoformat()
    if isinstance(value, str):
        return value
    return None


def _prepare_payload(payload: Dict[str, Any]) -> Dict[str, Any]:
    prepared = dict(payload)
    created_at = payload.get("created_at")
    if isinstance(created_at, str):
        try:
            prepared["created_at"] = datetime.fromisoformat(created_at)
        except ValueError:
            prepared["created_at"] = datetime.now(timezone.utc)
    elif created_at is None:
        prepared["created_at"] = datetime.now(timezone.utc)
    return prepared


def _document_to_dict(doc: firestore.DocumentSnapshot) -> Dict[str, Any]:
    data = doc.to_dict() or {}
    data["scan_id"] = doc.id
    data["created_at"] = _normalize_datetime(data.get("created_at"))
    return data


def upload_report(scan_id: str, payload: Dict[str, Any]) -> None:
    client = _ensure_firestore_client()
    collection = client.collection(settings.FIRESTORE_COLLECTION)
    prepared = _prepare_payload(payload)
    collection.document(scan_id).set(prepared)


def get_report(scan_id: str, user_id: Optional[str] = None) -> Dict[str, Any]:
    client = _ensure_firestore_client()
    doc = client.collection(settings.FIRESTORE_COLLECTION).document(scan_id).get()
    if not doc.exists:
        raise FileNotFoundError(f"Report {scan_id} not found")

    report = _document_to_dict(doc)
    if user_id and report.get("user_id") != user_id:
        raise PermissionError("Report does not belong to the authenticated user")
    return report


def list_reports(
    limit: Optional[int] = None,
    offset: Optional[int] = None,
    user_id: Optional[str] = None,
) -> List[Dict[str, Any]]:
    client = _ensure_firestore_client()
    collection = client.collection(settings.FIRESTORE_COLLECTION)
    query = collection
    if user_id:
        query = query.where("user_id", "==", user_id)
    else:
        query = query.order_by("created_at", direction=firestore.Query.DESCENDING)
    if limit:
        query = query.limit(limit)
    docs = query.stream()
    reports = [_document_to_dict(doc) for doc in docs]

    reports.sort(key=lambda r: datetime.fromisoformat(r["created_at"]) if isinstance(r.get("created_at"), str) else datetime.min, reverse=True)

    start = offset or 0
    end = start + limit if limit is not None else None
    return reports[start:end]


def list_recent_reports(limit: int = 10, user_id: Optional[str] = None) -> List[Dict[str, Any]]:
    return list_reports(limit=limit, user_id=user_id)





def count_reports(user_id: Optional[str] = None) -> int:
    client = _ensure_firestore_client()
    collection = client.collection(settings.FIRESTORE_COLLECTION)
    query = collection
    if user_id:
        query = query.where("user_id", "==", user_id)
    return sum(1 for _ in query.stream())


FirebaseError = firebase_exceptions.FirebaseError
