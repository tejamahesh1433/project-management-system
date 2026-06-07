#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import time
import uuid
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
    email = f"user_{uuid.uuid4().hex[:8]}@example.com"
    payload = {
        "email": email,
        "password": "Password123!",
        "displayName": "Test User"
    }
    data = make_request(f"{BASE_URL}/auth/register", method="POST", json_data=payload)
    return data["accessToken"], data["user"]["id"]

def test_phase_9():
    print("=" * 50)
    print("1. Setup Users and Workspace")
    print("=" * 50)
    
    # User 1 (Owner)
    token1, user_id1 = create_user()
    headers1 = {"Authorization": f"Bearer {token1}"}
    
    # User 2 (Invitee)
    token2, user_id2 = create_user()
    headers2 = {"Authorization": f"Bearer {token2}"}
    
    # Org
    org_payload = {"name": "Test Org", "slug": f"org-{int(time.time())}"}
    org_res = make_request(f"{BASE_URL}/organizations", method="POST", json_data=org_payload, headers=headers1)
    org_id = org_res["id"]
    
    # Workspace
    ws_payload = {"organizationId": org_id, "name": "Test WS", "slug": f"ws-{int(time.time())}"}
    ws_res = make_request(f"{BASE_URL}/workspaces", method="POST", json_data=ws_payload, headers=headers1)
    ws_id = ws_res["id"]
    print(f"Created Org {org_id}, WS {ws_id} by User 1")

    # Invite User 2 to Workspace to trigger a Notification
    invite_payload = {"email": f"user_{uuid.uuid4().hex[:8]}@example.com", "role": "MEMBER"}
    # The workspace invite API is: POST /workspaces/{id}/invitations
    try:
        invite_res = make_request(f"{BASE_URL}/workspaces/{ws_id}/invitations", method="POST", json_data=invite_payload, headers=headers1)
        print("Invited User 2 to WS.")
    except Exception as e:
        print("Note: Invitation endpoint may use a different path or body, or User 2 email might be different in actual DB. Proceeding anyway...")
        
    # Project (Triggers project created notification)
    proj_payload = {"workspaceId": ws_id, "name": "Test Project", "slug": f"proj-{int(time.time())}"}
    proj_res = make_request(f"{BASE_URL}/projects", method="POST", json_data=proj_payload, headers=headers1)
    proj_id = proj_res["id"]
    print(f"Created Project {proj_id}")
    
    time.sleep(1) # Wait for Domain Events
    
    print("\n" + "=" * 50)
    print("2. Check Notifications for User 1")
    print("=" * 50)
    
    notifs = make_request(f"{BASE_URL}/notifications", headers=headers1)
    print(f"Found {len(notifs)} notifications.")
    content = notifs.get('content', notifs) if isinstance(notifs, dict) else notifs
    if len(content) > 0:
        print(f"Latest Notification ID: {content[0].get('id')}")
        latest_id = content[0].get('id')
        
        # Mark as read
        make_request(f"{BASE_URL}/notifications/{latest_id}/read", method="PATCH", headers=headers1)
        print(f"Marked Notification {latest_id} as read.")
        
    print("\n" + "=" * 50)
    print("3. Check Unread Count for User 1")
    print("=" * 50)
    unread_res = make_request(f"{BASE_URL}/notifications/unread", headers=headers1)
    print(f"Unread Count: {unread_res.get('count')}")
    
    print("\n" + "=" * 50)
    print("4. Update Notification Preferences")
    print("=" * 50)
    pref_payload = {"preferences": [{"type": "WORKSPACE_INVITATION", "emailEnabled": False, "pushEnabled": True, "inAppEnabled": True}]}
    pref_res = make_request(f"{BASE_URL}/notification-preferences", method="PUT", json_data=pref_payload, headers=headers1)
    print("Preferences updated.")
    
if __name__ == "__main__":
    test_phase_9()
