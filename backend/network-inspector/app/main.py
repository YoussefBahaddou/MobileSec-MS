from fastapi import FastAPI
from app.core.config import get_settings
from app.api.endpoints import scan
from app.db.session import engine, Base

settings = get_settings()

# Create tables on startup (Simple approach, Alembic recommended for prod)
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title=settings.PROJECT_NAME,
    openapi_url=f"{settings.API_V1_STR}/openapi.json"
)

app.include_router(scan.router, prefix="/api/network", tags=["network"])

@app.get("/health")
def health_check():
    return {"status": "ok"}
