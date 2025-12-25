from typing import Optional

from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "APK Scanner Microservice"
    API_V1_STR: str = "/api"
    DATABASE_URL: str = "sqlite:///./apk_metadata.db"
    SUPABASE_JWT_SECRET: str = ""  # User must provide this in .env
    FIREBASE_PROJECT_ID: Optional[str] = None
    FIREBASE_CREDENTIALS_PATH: Optional[str] = None
    FIRESTORE_COLLECTION: str = "apk_reports"

    class Config:
        env_file = ".env"

settings = Settings()
