import logging
import subprocess
from app.core.config import get_settings

settings = get_settings()

class DockerManager:
    @staticmethod
    def start_android_container():
        """
        Attempts to start an Android Virtual Device (AVD) using Docker.
        Uses a popular image like 'budtmo/docker-android' as a reference.
        """
        image_name = "budtmo/docker-android:emulator_9.0"
        container_name = "mobilesec_avd"
        
        # Check if docker is installed
        try:
            subprocess.run(["docker", "--version"], check=True, stdout=subprocess.PIPE)
        except (subprocess.CalledProcessError, FileNotFoundError):
            raise Exception("Docker is not installed or not found in PATH.")

        # Command to run the container
        # Note: This requires --privileged and KVM usually.
        # We perform a 'best effort' command here.
        cmd = [
            "docker", "run", "-d",
            "--name", container_name,
            "--privileged",
            "-p", "6080:6080", # Web VNC
            "-p", "5555:5555", # ADB
            "-e", "DEVICE=Samsung Galaxy S10",
            image_name
        ]

        logging.info(f"Attempting to start AVD container: {container_name}")
        try:
            # Check if already running
            subprocess.run(["docker", "rm", "-f", container_name], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            
            subprocess.run(cmd, check=True)
            return {"status": "started", "container": container_name, "vnc_url": "http://localhost:6080"}
        except subprocess.CalledProcessError as e:
            logging.error(f"Failed to start Docker container: {e}")
            raise Exception(f"Failed to start Android Docker container. Ensure Virtualization/KVM is enabled. Error: {str(e)}")
