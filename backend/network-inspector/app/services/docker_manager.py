import logging
import subprocess
from app.core.config import get_settings

settings = get_settings()


class DockerManager:
    image_name = "budtmo/docker-android:emulator_9.0"
    container_name = "mobilesec_avd"

    @classmethod
    def _docker_available(cls):
        try:
            subprocess.run(["docker", "--version"], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            return True
        except (subprocess.CalledProcessError, FileNotFoundError) as exc:
            logging.error("Docker is not available: %s", exc)
            return False

    @classmethod
    def is_container_running(cls):
        if not cls._docker_available():
            return False
        try:
            result = subprocess.run(
                ["docker", "inspect", "-f", "{{.State.Running}}", cls.container_name],
                check=True,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True
            )
            return result.stdout.strip().lower() == "true"
        except subprocess.CalledProcessError:
            return False

    @classmethod
    def ensure_android_container(cls):
        if cls.is_container_running():
            logging.info("AVD container already running.")
            return {"status": "running", "container": cls.container_name, "vnc_url": "http://localhost:6080"}
        return cls.start_android_container()

    @classmethod
    def start_android_container(cls):
        if not cls._docker_available():
            raise Exception("Docker must be installed and available in PATH.")

        logging.info(f"Attempting to start AVD container: {cls.container_name}")
        cmd = [
            "docker", "run", "-d",
            "--name", cls.container_name,
            "--privileged",
            "-p", "6080:6080",  # Web VNC
            "-p", "5555:5555",  # ADB
            "-e", "DEVICE=Samsung Galaxy S10",
            cls.image_name
        ]

        try:
            subprocess.run(["docker", "rm", "-f", cls.container_name],
                           stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            subprocess.run(cmd, check=True)
            return {"status": "started", "container": cls.container_name, "vnc_url": "http://localhost:6080"}
        except subprocess.CalledProcessError as exc:
            logging.error("Failed to start Docker container: %s", exc)
            raise Exception(
                f"Failed to start Android Docker container. Ensure Virtualization/KVM is enabled. Error: {str(exc)}"
            )

    @classmethod
    def stop_android_container(cls):
        try:
            subprocess.run(["docker", "rm", "-f", cls.container_name],
                           stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False)
            return {"status": "stopped", "container": cls.container_name}
        except subprocess.CalledProcessError as exc:
            logging.warning("Failed to stop container: %s", exc)
            raise
