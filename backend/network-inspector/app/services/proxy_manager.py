import subprocess
import os
import signal
import logging
from app.core.config import get_settings

settings = get_settings()

class ProxyManager:
    _process: subprocess.Popen = None

    @classmethod
    def start_proxy(cls, scan_id: str):
        if cls._process:
            logging.warning("Proxy is already running.")
            return

        logging.info(f"Starting Mitmproxy for Scan ID: {scan_id}")
        
        # Path to the addon script
        addon_path = os.path.join(os.path.dirname(__file__), "mitm_addon.py")
        
        # Environment variables for the subprocess
        env = os.environ.copy()
        env["DATABASE_URL"] = settings.DATABASE_URL
        env["SCAN_ID"] = scan_id

        # Command: mitmweb --web-port 8086 --listen-port 8085 -s app/services/mitm_addon.py --no-web-open-browser
        cmd = [
            "mitmdump", # Use mitmdump for headless, or mitmweb for UI
            "--listen-port", str(settings.PROXY_PORT),
            "-s", addon_path,
            "--set", "block_global=false" 
        ]

        cls._process = subprocess.Popen(cmd, env=env, shell=True) # shell=True might be needed on Windows for path resolution
        logging.info(f"Mitmproxy started with PID: {cls._process.pid}")

    @classmethod
    def stop_proxy(cls):
        if cls._process:
            logging.info("Stopping Mitmproxy...")
            # On Windows, simple terminate() might not kill the tree.
            # taskkill is more robust.
            subprocess.call(["taskkill", "/F", "/T", "/PID", str(cls._process.pid)])
            cls._process = None
        else:
            logging.warning("No proxy process found to stop.")

    @classmethod
    def is_running(cls):
        return cls._process is not None and cls._process.poll() is None
