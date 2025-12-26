import re

# Dictionary of high-confidence patterns
# These simulate what tools like GitLeaks do.
SECRET_PATTERNS = {
    "AWS Access Key": r"(A3T[A-Z0-9]|AKIA|AGPA|AIDA|AROA|AIPA|ANPA|ANVA|ASIA)[A-Z0-9]{16}",
    "AWS Secret Key": r"(?i)aws(.{0,20})?['\"][0-9a-zA-Z\/+]{40}['\"]",
    "Google API Key": r"AIza[0-9A-Za-z\\-_]{35}",
    "Firebase URL": r".*firebaseio\.com",
    "Slack Token": r"(xox[pgar]-[a-zA-Z0-9]{10,48})",
    "RSA Private Key": r"-----BEGIN RSA PRIVATE KEY-----",
    "SSH Private Key": r"-----BEGIN OPENSSH PRIVATE KEY-----",
    "Facebook Access Token": r"EAACEdEose0cBA[0-9A-Za-z]+",
    "Generic API Key": r"(?i)(api_key|apikey|access_token|secret_key)(.{0,20})?['\"][0-9a-zA-Z]{16,60}['\"]",
    "Hardcoded Password": r"(?i)(password|passwd|pwd)(.{0,20})?['\"][0-9a-zA-Z@#$%]{6,60}['\"]",
    "JWT Token": r"eyJ[a-zA-Z0-9\-_]+\.eyJ[a-zA-Z0-9\-_]+\.[a-zA-Z0-9\-_]+",
    "Stripe Publishable Key": r"pk_live_[0-9a-zA-Z]{24}",
    "Stripe Secret Key": r"sk_live_[0-9a-zA-Z]{24}",
    "GitHub Token": r"(ghp|gho|ghu|ghs|ghr)_[a-zA-Z0-9]{36,255}"
}

# Add more complex YARA-like logic here if needed
