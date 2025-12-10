from fastapi import FastAPI
from app.api.endpoints import scan

app = FastAPI(title="SecretHunter Microservice", version="1.0.0")

# Include Routers
app.include_router(scan.router, prefix="/api/secrets", tags=["secrets"])

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "SecretHunter"}
