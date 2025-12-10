from sqlalchemy import Column, Integer, String, Text, Boolean, DateTime
from sqlalchemy.sql import func
from app.db.session import Base

class NetworkFinding(Base):
    __tablename__ = "network_findings"

    id = Column(Integer, primary_key=True, index=True)
    scan_id = Column(String, index=True) # ID to correlate with a specific scan session
    url = Column(Text, nullable=False)
    method = Column(String, nullable=False)
    request_headers = Column(Text, nullable=True) # Stored as JSON string or plain text
    response_headers = Column(Text, nullable=True)
    status_code = Column(Integer, nullable=True)
    is_secure = Column(Boolean, default=False) # True if HTTPS
    vulnerability_type = Column(String, nullable=True) # e.g., "Insecure HTTP", "Sensitive Data Leak"
    created_at = Column(DateTime(timezone=True), server_default=func.now())
