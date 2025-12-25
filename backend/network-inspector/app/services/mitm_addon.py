import logging
import json
import os
from datetime import datetime

import mitmproxy.http as http
from OpenSSL import crypto
from mitmproxy import ctx
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
        logging.info(f"NetworkInterceptor initialized for Scan ID: {self.scan_id}")

    def request(self, flow: http.HTTPFlow):
        flow.request_summary = self.extract_request(flow)

    def response(self, flow: http.HTTPFlow):
        try:
            request_meta = getattr(flow, "request_summary", None)
            url = flow.request.url
            method = flow.request.method
            status_code = flow.response.status_code
            is_secure = flow.request.scheme.lower() == "https"

            req_headers = dict(flow.request.headers)
            res_headers = dict(flow.response.headers)

            tls = self.extract_tls_info(flow)
            analysis = self.summarize_flow(
                url=url,
                is_secure=is_secure,
                req_headers=req_headers,
                res_headers=res_headers,
                tls=tls,
                request_meta=request_meta
            )

            self.save_finding(
                url=url,
                method=method,
                req_headers=json.dumps(req_headers),
                res_headers=json.dumps(res_headers),
                status_code=status_code,
                is_secure=is_secure,
                vuln_type=analysis.get("vulnerability_type"),
                tls_version=tls.get("tls_version"),
                cipher_suite=tls.get("cipher_suite"),
                cert_subject=tls.get("cert_subject"),
                cert_issuer=tls.get("cert_issuer"),
                cert_expiry=tls.get("cert_expiry"),
                cert_fingerprint=tls.get("cert_fingerprint"),
                analysis_summary=analysis.get("summary")
            )
        except Exception as e:
            logging.error("Error processing flow: %s", e)

    def extract_request(self, flow: http.HTTPFlow):
        request_data = {
            "body": None,
            "has_sensitive": False,
            "sensitive_fields": []
        }
        keywords = ["password", "secret", "apikey", "token", "access_token", "private_key"]
        if flow.request.content:
            try:
                decoded = flow.request.content.decode("utf-8", errors="ignore")
                lower = decoded.lower()
                matches = [kw for kw in keywords if kw in lower]
                if matches:
                    request_data["has_sensitive"] = True
                    request_data["sensitive_fields"] = matches
                request_data["body"] = decoded
            except Exception:
                pass
        return request_data

    def extract_tls_info(self, flow: http.HTTPFlow):
        tls_info = {
            "tls_version": None,
            "cipher_suite": None,
            "cert_subject": None,
            "cert_issuer": None,
            "cert_expiry": None,
            "cert_fingerprint": None,
        }
        server_conn = getattr(flow, "server_conn", None)
        if not server_conn:
            return tls_info

        tls_info["tls_version"] = getattr(server_conn, "tls_version", None)
        tls_info["cipher_suite"] = getattr(server_conn, "cipher", None)

        cert = getattr(server_conn, "cert", None)
        if cert:
            tls_info["cert_subject"] = self._format_name(cert.get_subject())
            tls_info["cert_issuer"] = self._format_name(cert.get_issuer())
            try:
                not_after = datetime.strptime(cert.get_notAfter().decode(), "%Y%m%d%H%M%SZ")
                tls_info["cert_expiry"] = not_after.isoformat()
            except Exception:
                tls_info["cert_expiry"] = None
            tls_info["cert_fingerprint"] = cert.digest("sha256").decode()
        return tls_info

    def _format_name(self, name_obj):
        try:
            components = name_obj.get_components()
            return ", ".join(f"{comp[0].decode()}={comp[1].decode()}" for comp in components)
        except Exception:
            return None

    def summarize_flow(self, url, is_secure, req_headers, res_headers, tls, request_meta):
        findings = []
        vuln_type = None

        if not is_secure:
            findings.append("Request is over HTTP, no TLS protection.")
            vuln_type = vuln_type or "Insecure HTTP"
        else:
            if "Strict-Transport-Security" not in res_headers:
                findings.append("Missing Strict-Transport-Security header.")
                vuln_type = vuln_type or "Missing HSTS"
            set_cookie = res_headers.get("Set-Cookie", "")
            if isinstance(set_cookie, str) and "secure" not in set_cookie.lower():
                findings.append("Cookie without Secure flag.")
                vuln_type = vuln_type or "Insecure Cookie Flags"

        sensitive_keys = ["password", "secret", "apikey", "token", "access_token", "private_key"]
        sensitive_detected = []
        lower_url = url.lower()
        for key in sensitive_keys:
            if key in lower_url:
                sensitive_detected.append(f"Leak via URL parameter: {key}")
                vuln_type = vuln_type or "Sensitive Data in URL"
        if request_meta and request_meta.get("has_sensitive"):
            findings.append(f"Sensitive payload detected: {', '.join(request_meta.get('sensitive_fields', []))}")
            vuln_type = vuln_type or "Sensitive Data in Request Body"

        if tls.get("cert_fingerprint") and tls.get("cert_expiry"):
            exp_date = datetime.fromisoformat(tls["cert_expiry"])
            if exp_date < datetime.utcnow():
                findings.append("Upstream TLS certificate is expired.")
                vuln_type = vuln_type or "Expired Certificate"
        if tls.get("cert_subject") and tls.get("cert_issuer") and tls["cert_subject"] == tls["cert_issuer"]:
            findings.append("TLS certificate appears to be self-signed.")
            vuln_type = vuln_type or "Self-Signed Certificate"

        findings.extend(sensitive_detected)

        summary = "; ".join(findings) if findings else "No issues detected."
        return {"summary": summary, "vulnerability_type": vuln_type}

    def save_finding(
        self,
        url,
        method,
        req_headers,
        res_headers,
        status_code,
        is_secure,
        vuln_type,
        tls_version,
        cipher_suite,
        cert_subject,
        cert_issuer,
        cert_expiry,
        cert_fingerprint,
        analysis_summary
    ):
        insert_query = text("""
            INSERT INTO network_findings (
                scan_id,
                url,
                method,
                request_headers,
                response_headers,
                status_code,
                is_secure,
                vulnerability_type,
                tls_version,
                cipher_suite,
                cert_subject,
                cert_issuer,
                cert_expiry,
                cert_fingerprint,
                analysis_summary,
                created_at
            ) VALUES (
                :scan_id,
                :url,
                :method,
                :req_headers,
                :res_headers,
                :status_code,
                :is_secure,
                :vuln_type,
                :tls_version,
                :cipher_suite,
                :cert_subject,
                :cert_issuer,
                :cert_expiry,
                :cert_fingerprint,
                :analysis_summary,
                NOW()
            )
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
                "vuln_type": vuln_type,
                "tls_version": tls_version,
                "cipher_suite": cipher_suite,
                "cert_subject": cert_subject,
                "cert_issuer": cert_issuer,
                "cert_expiry": cert_expiry,
                "cert_fingerprint": cert_fingerprint,
                "analysis_summary": analysis_summary
            })
            conn.commit()

addons = [
    NetworkInterceptor()
]
