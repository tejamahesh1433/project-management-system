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
    owner_email = f"project_owner_{timestamp}@example.com"
    member_email = f"project_member_{timestamp}@example.com"
    password = "Password123!"

    print_step("1. Registering Owner and Member")
    make_request(f"{BASE_URL}/auth/register", "POST", {"email": owner_email, "password": password, "displayName": "Project Owner"})
    make_request(f"{BASE_URL}/auth/register", "POST", {"email": member_email, "password": password, "displayName": "Project Member"})
    
    owner_res = make_request(f"{BASE_URL}/auth/login", "POST", {"email": owner_email, "password": password})
    member_res = make_request(f"{BASE_URL}/auth/login", "POST", {"email": member_email, "password": password})
    
    owner_token = owner_res["accessToken"]
    member_token = member_res["accessToken"]
    owner_headers = {"Authorization": f"Bearer {owner_token}"}
    member_headers = {"Authorization": f"Bearer {member_token}"}
    
    print_step("2. Setup Organization and Workspace")
    org = make_request(f"{BASE_URL}/organizations", "POST", {"name": "Phase3 Org", "slug": f"phase3-org-{timestamp}"}, owner_headers)
    ws = make_request(f"{BASE_URL}/workspaces", "POST", {
        "organizationId": org["id"],
        "name": "Phase3 WS",
        "slug": f"phase3-ws-{timestamp}"
    }, owner_headers)
    ws_id = ws["id"]
    
    # Invite the member to workspace so they can be added to projects
    invite = make_request(f"{BASE_URL}/workspaces/{ws_id}/invitations", "POST", {"email": member_email, "role": "MEMBER"}, owner_headers)
    make_request(f"{BASE_URL}/workspaces/invitations/accept", "POST", {"token": invite.get("token", invite.get("rawToken"))}, member_headers)

    print_step("3. Create Project")
    project = make_request(f"{BASE_URL}/projects", "POST", {
        "workspaceId": ws_id,
        "name": "Alpha Project",
        "slug": f"alpha-{timestamp}",
        "description": "First project"
    }, owner_headers)
    
    if not project:
        print("Failed to create project")
        return
        
    project_id = project["id"]

    print_step("4. List Projects")
    make_request(f"{BASE_URL}/projects?workspaceId={ws_id}", "GET", None, owner_headers)

    print_step("5. Update Project")
    make_request(f"{BASE_URL}/projects/{project_id}", "PUT", {
        "name": "Alpha Project v2",
        "slug": f"alpha-v2-{timestamp}",
        "status": "COMPLETED"
    }, owner_headers)

    print_step("6. Add Project Member")
    make_request(f"{BASE_URL}/projects/{project_id}/members", "POST", {
        "userId": member_res["user"]["id"],
        "role": "PROJECT_VIEWER"
    }, owner_headers)
    
    print_step("7. List Project Members")
    members = make_request(f"{BASE_URL}/projects/{project_id}/members", "GET", None, owner_headers)
    
    member_id = None
    for m in members:
        if m["email"] == member_email:
            member_id = m["id"]
            break

    if member_id:
        print_step("8. Update Project Member Role")
        make_request(f"{BASE_URL}/projects/{project_id}/members/{member_id}/role", "PATCH", {
            "role": "PROJECT_MEMBER"
        }, owner_headers)

        print_step("9. Remove Project Member")
        make_request(f"{BASE_URL}/projects/{project_id}/members/{member_id}", "DELETE", None, owner_headers)

    print_step("10. Archive and Restore Project")
    make_request(f"{BASE_URL}/projects/{project_id}/archive", "POST", None, owner_headers)
    make_request(f"{BASE_URL}/projects/{project_id}/restore", "POST", None, owner_headers)

    print_step("11. Delete Project")
    make_request(f"{BASE_URL}/projects/{project_id}", "DELETE", None, owner_headers)

    print_step("12. Verify Deletion")
    make_request(f"{BASE_URL}/projects/{project_id}", "GET", None, owner_headers)


if __name__ == "__main__":
    main()
