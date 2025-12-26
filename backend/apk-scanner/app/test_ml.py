from app.ml_engine import predict_malware_probability
import logging

# Configure basic logging to console
logging.basicConfig(level=logging.INFO)

print("--- Starting ML Isolation Test ---")

# Test 1: Empty permissions
print("\nTest 1: Empty Permissions")
res1 = predict_malware_probability([])
print(f"Result 1: {res1}")

# Test 2: Common Permissions
print("\nTest 2: Common Permissions (INTERNET, ACCESS_NETWORK_STATE)")
perms = ["android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"]
res2 = predict_malware_probability(perms)
print(f"Result 2: {res2}")

# Test 3: Dangerous Permissions
print("\nTest 3: Dangerous Permissions (SEND_SMS, RECEIVE_SMS)")
perms_danger = ["android.permission.SEND_SMS", "android.permission.RECEIVE_SMS", "android.permission.READ_CONTACTS"]
res3 = predict_malware_probability(perms_danger)
print(f"Result 3: {res3}")

print("\n--- Test Complete ---")
