import json
import urllib.request
import urllib.error

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
    print_step("1. Registering Users A and B")
    user_a = {"email": "owner3@example.com", "password": "Password123!", "displayName": "Owner User 3"}
    user_b = {"email": "invitee3@example.com", "password": "Password123!", "displayName": "Invitee User 3"}
    
    make_request(f"{BASE_URL}/auth/register", "POST", user_a)
    make_request(f"{BASE_URL}/auth/register", "POST", user_b)
    print("Users registered.")

    print_step("2. Logging in")
    res_a = make_request(f"{BASE_URL}/auth/login", "POST", user_a)
    res_b = make_request(f"{BASE_URL}/auth/login", "POST", user_b)
    
    token_a = res_a.get("accessToken") if res_a else None
    token_b = res_b.get("accessToken") if res_b else None
    
    if not token_a or not token_b:
        print("Failed to get tokens. Ensure server is running.")
        return

    headers_a = {"Authorization": f"Bearer {token_a}"}
    headers_b = {"Authorization": f"Bearer {token_b}"}
    print("Tokens retrieved.")

    print_step("3. Create Organization (User A)")
    org_res = make_request(f"{BASE_URL}/organizations", "POST", {"name": "Test Org 3", "slug": "test-org-3"}, headers_a)
    if not org_res: return
    org_id = org_res["id"]

    print_step("4. Fetch Organizations (User A)")
    make_request(f"{BASE_URL}/organizations", "GET", None, headers_a)

    print_step("5. Create Workspace (User A)")
    ws_res = make_request(f"{BASE_URL}/workspaces", "POST", {
        "organizationId": org_id,
        "name": "Design Team 3",
        "slug": "design-team-3",
        "description": "Design workspace"
    }, headers_a)
    if not ws_res: return
    ws_id = ws_res["id"]

    print_step("6. Fetch Workspaces (User A)")
    make_request(f"{BASE_URL}/workspaces", "GET", None, headers_a)

    print_step("7. Invite User B (User A)")
    invite_res = make_request(f"{BASE_URL}/workspaces/{ws_id}/invitations", "POST", {
        "email": "invitee3@example.com",
        "role": "MEMBER"
    }, headers_a)
    if not invite_res: return
    raw_token = invite_res.get("token", invite_res.get("rawToken"))

    print_step("8. Fetch Pending Invitations (User A)")
    make_request(f"{BASE_URL}/workspaces/{ws_id}/invitations", "GET", None, headers_a)

    print_step("9. Accept Invitation (User B)")
    make_request(f"{BASE_URL}/workspaces/invitations/accept", "POST", {
        "token": raw_token
    }, headers_b)

    print_step("10. Fetch Workspace Members (User A)")
    make_request(f"{BASE_URL}/workspaces/{ws_id}/members", "GET", None, headers_a)

if __name__ == "__main__":
    main()
