from fastapi import FastAPI
from app.api.endpoints import scan, dashboard
from app.db.session import engine
from app.models import metadata

# Create tables
metadata.Base.metadata.create_all(bind=engine)

app = FastAPI(title="APK Scanner Service")

app.include_router(scan.router, prefix="/api/scan", tags=["scan"])
app.include_router(dashboard.router, prefix="/api/dashboard", tags=["dashboard"])

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "APKScanner"}
