from fastapi import FastAPI
from contextlib import asynccontextmanager
import py_eureka_client.eureka_client as eureka_client
from app.core.config import get_settings
from app.api.endpoints import scan
from app.db.session import engine, Base

settings = get_settings()

# Create tables
Base.metadata.create_all(bind=engine)

@asynccontextmanager
async def lifespan(app: FastAPI):
import os
    eureka_url = os.getenv("EUREKA_SERVER_URL", "http://localhost:8761/eureka")
    await eureka_client.init_async(eureka_server=eureka_url,
                                   app_name="network-inspector",
                                   instance_port=8092)
    yield
    await eureka_client.stop_async()

app = FastAPI(
    title=settings.PROJECT_NAME,
    openapi_url=f"{settings.API_V1_STR}/openapi.json",
    lifespan=lifespan
)

app.include_router(scan.router, prefix="/api/network", tags=["network"])

@app.get("/health")
def health_check():
    return {"status": "ok"}
