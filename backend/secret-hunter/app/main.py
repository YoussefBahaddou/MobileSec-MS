from fastapi import FastAPI
from contextlib import asynccontextmanager
import py_eureka_client.eureka_client as eureka_client
from app.api.endpoints import scan

@asynccontextmanager
async def lifespan(app: FastAPI):
import os
    eureka_url = os.getenv("EUREKA_SERVER_URL", "http://localhost:8761/eureka")
    await eureka_client.init_async(eureka_server=eureka_url,
                                   app_name="secret-hunter",
                                   instance_port=8089)
    yield
    await eureka_client.stop_async()

app = FastAPI(title="SecretHunter Microservice", version="1.0.0", lifespan=lifespan)

# Include Routers
app.include_router(scan.router, prefix="/api/secrets", tags=["secrets"])

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "SecretHunter"}
