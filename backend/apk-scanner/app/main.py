from fastapi import FastAPI
from contextlib import asynccontextmanager
import py_eureka_client.eureka_client as eureka_client
from app.api.endpoints import scan, dashboard
from app.api.endpoints import analysis 
from app.db.session import engine
from app.models import metadata
from fastapi.middleware.cors import CORSMiddleware

# Create tables
metadata.Base.metadata.create_all(bind=engine)

@asynccontextmanager
async def lifespan(app: FastAPI):
import os
    eureka_url = os.getenv("EUREKA_SERVER_URL", "http://localhost:8761/eureka")
    await eureka_client.init_async(eureka_server=eureka_url,
                                   app_name="apk-scanner",
                                   instance_port=8088)
    yield
    await eureka_client.stop_async()

app = FastAPI(title="APK Scanner Service", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(scan.router, prefix="/api/scan", tags=["scan"])
app.include_router(dashboard.router, prefix="/api/dashboard", tags=["dashboard"])
app.include_router(analysis.router, prefix="/api/analysis", tags=["analysis"])

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "APKScanner"}
