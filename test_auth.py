import urllib.request
import json

BASE_URL = "http://localhost:8081/api/v1"
user_a = {"email": "owner4@example.com", "password": "Password123!", "displayName": "Owner User 4"}

req = urllib.request.Request(f"{BASE_URL}/auth/register", data=json.dumps(user_a).encode(), headers={"Content-Type": "application/json"})
urllib.request.urlopen(req)

req = urllib.request.Request(f"{BASE_URL}/auth/login", data=json.dumps(user_a).encode(), headers={"Content-Type": "application/json"})
with urllib.request.urlopen(req) as res:
    token = json.loads(res.read().decode())["accessToken"]

print("TOKEN:", token)

req = urllib.request.Request(f"{BASE_URL}/organizations", data=json.dumps({"name": "Test", "slug": "test-slug-4"}).encode(), headers={"Content-Type": "application/json", "Authorization": f"Bearer {token}"})
try:
    with urllib.request.urlopen(req) as res:
        print("ORG:", res.read().decode())
except Exception as e:
    print("ERR:", e, e.read().decode() if hasattr(e, 'read') else '')
