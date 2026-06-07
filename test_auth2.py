import urllib.request
import json
import traceback

BASE_URL = "http://localhost:8081/api/v1"
user_a = {"email": "owner5@example.com", "password": "Password123!", "displayName": "Owner User 5"}

req = urllib.request.Request(f"{BASE_URL}/auth/register", data=json.dumps(user_a).encode(), headers={"Content-Type": "application/json"})
urllib.request.urlopen(req)

req = urllib.request.Request(f"{BASE_URL}/auth/login", data=json.dumps(user_a).encode(), headers={"Content-Type": "application/json"})
with urllib.request.urlopen(req) as res:
    token = json.loads(res.read().decode())["accessToken"]

print("TOKEN:", token)

req = urllib.request.Request(f"{BASE_URL}/workspaces", headers={"Authorization": f"Bearer {token}"})
try:
    with urllib.request.urlopen(req) as res:
        print("WORKSPACES:", res.read().decode())
except Exception as e:
    print("ERR:", e)
    if hasattr(e, 'read'):
        print("BODY:", e.read().decode())
        print("HEADERS:", e.headers)
