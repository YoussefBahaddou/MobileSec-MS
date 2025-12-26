import os
import sys

# Ensure we can import from 'app'
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.ml_engine import predict_score

def test_scenario(name, permissions):
    print(f"--- TESTING: {name} ---")
    print(f"Permissions: {len(permissions)}")
    result = predict_score(permissions)
    
    if result:
        print(f"Score: {result['probability']}%")
        print(f"Label: {result['label']}")
        print(f"Color: {result['color']}")
        print("--------------------------\n")
    else:
        print("FAILED: Model returned None (Check logs)\n")

if __name__ == "__main__":
    print("=== XGBoost 'God Tier' Model Verification ===\n")

    # 1. The "Clean" App (e.g. Calculator)
    # Typically few or no dangerous permissions
    clean_perms = [
        "android.permission.INTERNET", 
        "android.permission.ACCESS_NETWORK_STATE"
    ]
    test_scenario("Clean App (Calculator)", clean_perms)

    # 2. The "Suspicious" App (e.g. Flashlight requesting Location)
    # Some dangerous perms, but maybe not enough to be critical
    suspicious_perms = [
        "android.permission.CAMERA",
        "android.permission.FLASHLIGHT",
        "android.permission.INTERNET",
        "android.permission.ACCESS_FINE_LOCATION" # Why does a flashlight need this?
    ]
    test_scenario("Suspicious App (Flashlight)", suspicious_perms)

    # 3. The "Malware" (Spyware)
    # Heavy combination of critical permissions
    malware_perms = [
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_SMS",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_CALL_LOG",
        "android.permission.RECORD_AUDIO",
        "android.permission.CAMERA",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.RECEIVE_BOOT_COMPLETED",
        "android.permission.INTERNET"
    ]
    test_scenario("Spyware Simulation (Critical)", malware_perms)
