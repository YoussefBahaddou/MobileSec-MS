from fastapi import FastAPI
from app.core.config import settings
from app.db.session import engine, Base
from app.api.endpoints import scan

# Create tables
Base.metadata.create_all(bind=engine)

app = FastAPI(title=settings.PROJECT_NAME)

app.include_router(scan.router, prefix="/api/scan", tags=["scan"])

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "APKScanner"}
