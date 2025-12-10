from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "APK Scanner Microservice"
    API_V1_STR: str = "/api"
    # SQLite Database as per requirements
    DATABASE_URL: str = "sqlite:///./apk_metadata.db"

    class Config:
        env_file = ".env"

settings = Settings()
