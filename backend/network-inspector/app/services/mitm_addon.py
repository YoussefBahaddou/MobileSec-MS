import logging
from mitmproxy import http
import json
import os
import datetime
# We need to render this script standalone usually, but here we can try to import models if path is set correctly.
# However, mitmproxy runs in its own process. It's safer to use raw SQL or a separate API call to save results.
# For simplicity and performance in this Clean Arch structure, we will use a direct DB connection here using SQLAlchemy,
# assuming the virtualenv packages are available to mitmproxy.

from sqlalchemy import create_engine, text

# Configuration (Hardcoded or Env Var because this script runs inside mitmproxy)
DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql://postgres.hwhnkggjuazakmkfpxzj:306542zhf@aws-1-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require"
)

# Setup simple DB engine
engine = create_engine(DATABASE_URL)

class NetworkInterceptor:
    def __init__(self):
        self.scan_id = os.getenv("SCAN_ID", "default_session")
        logging.info(f"NetworkInterceptor started for Scan ID: {self.scan_id}")

    def request(self, flow: http.HTTPFlow):
        # Inspect Request
        pass

    def response(self, flow: http.HTTPFlow):
        # Inspect Response
        try:
            url = flow.request.url
            method = flow.request.method
            status_code = flow.response.status_code
            is_secure = flow.request.scheme == "https"
            
            req_headers = dict(flow.request.headers)
            res_headers = dict(flow.response.headers)
            
            # Simple Vulnerability Check 1: Plain HTTP
            vuln_type = None
            if not is_secure:
                vuln_type = "Insecure Data Transmission (HTTP)"
            
            # Simple Vulnerability Check 2: Sensitive Data in URL
            if "api_key" in url or "token" in url or "password" in url:
                vuln_type = "Sensitive Data in URL"

            # Check 3: Missing HSTS Header (for HTTPS)
            if is_secure and "Strict-Transport-Security" not in dict(flow.response.headers):
                if not vuln_type:
                    vuln_type = "Missing HSTS Header"

            # Check 4: Sensitive Data in Request Body
            if flow.request.content:
                try:
                    body_str = flow.request.content.decode('utf-8')
                    if any(sensitive in body_str.lower() for sensitive in ["password", "secret", "apikey", "access_token", "private_key"]):
                         vuln_type = "Sensitive Data in Request Body"
                except:
                    pass # Binary content

            # Check 5: Potential Bad SSL/TLS (Simulated check, as mitmproxy handles handshake)
            # If we wanted to check the UPSTREAM certificate, we'd need to inspect flow.server_conn.cert
            if flow.server_conn and flow.server_conn.cert:
                # Example: Check if cert is self-signed or expired (mitmproxy does this, but we can log specific findings)
                pass 

            # Save to DB
            self.save_finding(
                url=url,
                method=method,
                req_headers=json.dumps(req_headers),
                res_headers=json.dumps(res_headers),
                status_code=status_code,
                is_secure=is_secure,
                vuln_type=vuln_type
            )
            
        except Exception as e:
            logging.error(f"Error processing flow: {e}")

    def save_finding(self, url, method, req_headers, res_headers, status_code, is_secure, vuln_type):
        # Use raw SQL for minimal dependency friction inside mitmproxy's loop
        # Or utilize the engine
        if not vuln_type and is_secure:
            return # Skip benign valid HTTPS traffic to save DB space if desired, or keep for logs. 
                   # Let's keep everything for now but prioritize vulns.
        
        insert_query = text("""
            INSERT INTO network_findings (scan_id, url, method, request_headers, response_headers, status_code, is_secure, vulnerability_type, created_at)
            VALUES (:scan_id, :url, :method, :req_headers, :res_headers, :status_code, :is_secure, :vuln_type, NOW())
        """)
        
        with engine.connect() as conn:
            conn.execute(insert_query, {
                "scan_id": self.scan_id,
                "url": url,
                "method": method,
                "req_headers": req_headers,
                "res_headers": res_headers,
                "status_code": status_code,
                "is_secure": is_secure,
                "vuln_type": vuln_type
            })
            conn.commit()

addons = [
    NetworkInterceptor()
]
