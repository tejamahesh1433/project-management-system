#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import time

BASE_URL = "http://localhost:8081/api/v1"

def print_step(step_name):
    print(f"\n{'='*50}\n{step_name}\n{'='*50}")

def make_request(url, method="GET", json_data=None, headers=None):
    if headers is None:
        headers = {}
    if json_data:
        data = json.dumps(json_data).encode("utf-8")
        headers["Content-Type"] = "application/json"
    else:
        data = None
        
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as response:
            res_body = response.read().decode("utf-8")
            if res_body:
                parsed = json.loads(res_body)
                print(json.dumps(parsed, indent=2))
                return parsed
            return None
    except urllib.error.HTTPError as e:
        print(f"HTTPError {e.code}: {e.read().decode('utf-8')}")
        return None
    except Exception as e:
        print(f"Error: {e}")
        return None

def main():
    timestamp = int(time.time())
    email = f"testuser_{timestamp}@example.com"
    password = "Password123!"

    print_step("1. Registering User")
    user_data = {"email": email, "password": password, "displayName": "Test User"}
    make_request(f"{BASE_URL}/auth/register", "POST", user_data)
    print("User registered.")

    print_step("2. Logging in")
    login_data = {"email": email, "password": password}
    res = make_request(f"{BASE_URL}/auth/login", "POST", login_data)
    
    if not res or not res.get("accessToken"):
        print("Failed to login.")
        return
        
    print("Login successful. Token retrieved.")

if __name__ == "__main__":
    main()
