from sqlalchemy import Column, String, Boolean, Text, Integer, JSON, DateTime
from app.db.session import Base
import datetime
import uuid

class APKMetadata(Base):
    __tablename__ = "apk_metadata"

    scan_id = Column(String, primary_key=True, index=True)
    file_name = Column(String)
    package_name = Column(String)
    version_code = Column(String)
    
    # Store lists as JSON
    permissions = Column(JSON) 
    exported_activities = Column(JSON)
    exported_services = Column(JSON)
    exported_receivers = Column(JSON)
    exported_providers = Column(JSON)
    
    # Security Flags
    is_debuggable = Column(Boolean, default=False)
    allow_backup = Column(Boolean, default=True) # Default is True in Android unless set to false
    uses_cleartext_traffic = Column(Boolean, default=False)
    
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
