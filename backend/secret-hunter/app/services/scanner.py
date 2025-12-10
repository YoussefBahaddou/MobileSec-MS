import re
import logging
import os
from app.utils.patterns import SECRET_PATTERNS

class SecretScannerService:
    
    @staticmethod
    def scan_text(content: str, source_name: str = "text_input"):
        """
        Scans a string buffer for secrets using regex patterns.
        """
        findings = []
        
        for name, pattern_str in SECRET_PATTERNS.items():
            try:
                # Find all matches (iterating to get positions if needed, but simple re.findall for now)
                # Using re.finditer to get line numbers or context is better
                matches = re.finditer(pattern_str, content)
                for match in matches:
                    matched_text = match.group()
                    # Redact part of the secret for safety
                    redacted = SecretScannerService._redact(matched_text)
                    
                    findings.append({
                        "type": name,
                        "match": redacted,
                        "source": source_name,
                        "start": match.start(),
                        "end": match.end()
                    })
            except Exception as e:
                logging.warning(f"Error checking pattern {name}: {e}")
                
        return findings

    @staticmethod
    def scan_file(file_path: str):
        """
        Reads a file and scans it. 
        """
        try:
            with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                content = f.read()
            return SecretScannerService.scan_text(content, source_name=os.path.basename(file_path))
        except Exception as e:
            logging.error(f"Failed to scan file {file_path}: {e}")
            raise e

    @staticmethod
    def _redact(text: str):
        if len(text) <= 8:
            return "***"
        return text[:4] + "***" + text[-4:]
