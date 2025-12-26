from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache
import os

class Settings(BaseSettings):
    API_V1_STR: str = "/api/v1"
    PROJECT_NAME: str = "Network Inspector"
    
    # Database
    # Default to localhost if not set, but user provided Supabase details previously
    DATABASE_URL: str = os.getenv(
        "DATABASE_URL", 
        "postgresql://postgres.hwhnkggjuazakmkfpxzj:306542zhf@aws-1-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require"
    )

    # Mitmproxy
    PROXY_HOST: str = "0.0.0.0"
    PROXY_PORT: int = 8085
    PROXY_WEB_PORT: int = 8086
    
    model_config = SettingsConfigDict(case_sensitive=True)

@lru_cache
def get_settings():
    return Settings()
