#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import time
import sys

BASE_URL = "http://localhost:8081/api/v1"

def print_json(data):
    print(json.dumps(data, indent=2))

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
            body = response.read().decode("utf-8")
            if body:
                return json.loads(body)
            return None
    except urllib.error.HTTPError as e:
        print(f"HTTPError {e.code} on {url}: {e.read().decode('utf-8')}")
        sys.exit(1)

def create_user():
    email = f"user_{int(time.time())}@example.com"
    payload = {
        "email": email,
        "password": "Password123!",
        "displayName": "Test User"
    }
    data = make_request(f"{BASE_URL}/auth/register", method="POST", json_data=payload)
    return data["accessToken"], data["user"]["id"]

def test_phase_8():
    print("=" * 50)
    print("1. Setup User and Hierarchy")
    print("=" * 50)
    token, user_id = create_user()
    headers = {"Authorization": f"Bearer {token}"}
    
    org_payload = {"name": "Test Org", "slug": f"org-{int(time.time())}"}
    org_res = make_request(f"{BASE_URL}/organizations", method="POST", json_data=org_payload, headers=headers)
    org_id = org_res["id"]
    
    ws_payload = {"organizationId": org_id, "name": "Test WS", "slug": f"ws-{int(time.time())}"}
    ws_res = make_request(f"{BASE_URL}/workspaces", method="POST", json_data=ws_payload, headers=headers)
    ws_id = ws_res["id"]
    
    proj_payload = {"workspaceId": ws_id, "name": "Test Project", "slug": f"proj-{int(time.time())}"}
    proj_res = make_request(f"{BASE_URL}/projects", method="POST", json_data=proj_payload, headers=headers)
    proj_id = proj_res["id"]
    print(f"Created Org {org_id}, WS {ws_id}, Proj {proj_id}")
    
    task_payload = {"projectId": proj_id, "title": "Implement Audit Log", "type": "TASK"}
    task_res = make_request(f"{BASE_URL}/tasks", method="POST", json_data=task_payload, headers=headers)
    task_id = task_res["id"]
    print(f"Created Task {task_id}")
    
    time.sleep(1)
    
    print("\n" + "=" * 50)
    print("2. Check Activity Log - Workspace Feed")
    print("=" * 50)
    activities = make_request(f"{BASE_URL}/activity/workspaces/{ws_id}", headers=headers)
    print(f"Found {len(activities)} activities in workspace feed")
    if len(activities) > 0:
        print(f"Latest activity action: {activities[0].get('action')}")
    
    print("\n" + "=" * 50)
    print("3. Check Activity Log - Project Feed")
    print("=" * 50)
    proj_activities = make_request(f"{BASE_URL}/activity/projects/{proj_id}", headers=headers)
    print(f"Found {len(proj_activities)} activities in project feed")
    if len(proj_activities) > 0:
        print(f"Latest activity action: {proj_activities[0].get('action')}")
        
    print("\n" + "=" * 50)
    print("4. Check Audit Log - Task Entity")
    print("=" * 50)
    audits = make_request(f"{BASE_URL}/audit/Task/{task_id}", headers=headers)
    print(f"Found {len(audits)} audit logs for Task {task_id}")
    if len(audits) > 0:
        print(f"Latest audit action: {audits[0].get('action')}")
        
    print("\n" + "=" * 50)
    print("5. Check Audit Log - User")
    print("=" * 50)
    user_audits = make_request(f"{BASE_URL}/audit", headers=headers)
    print(f"Found {len(user_audits)} audit logs for current user")
    
if __name__ == "__main__":
    test_phase_8()
