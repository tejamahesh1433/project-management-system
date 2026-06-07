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
        print(f"HTTPError {e.code} on {url}: {e.read().decode('utf-8')}")
        return None
    except Exception as e:
        print(f"Error: {e}")
        return None

def main():
    timestamp = int(time.time())
    owner_email = f"board_owner_{timestamp}@example.com"
    member_email = f"board_member_{timestamp}@example.com"
    password = "Password123!"

    print_step("1. Setup Users, Workspace and Project")
    make_request(f"{BASE_URL}/auth/register", "POST", {"email": owner_email, "password": password, "displayName": "Board Owner"})
    make_request(f"{BASE_URL}/auth/register", "POST", {"email": member_email, "password": password, "displayName": "Board Member"})
    
    owner_res = make_request(f"{BASE_URL}/auth/login", "POST", {"email": owner_email, "password": password})
    member_res = make_request(f"{BASE_URL}/auth/login", "POST", {"email": member_email, "password": password})
    
    owner_token = owner_res["accessToken"]
    member_token = member_res["accessToken"]
    owner_headers = {"Authorization": f"Bearer {owner_token}"}
    member_headers = {"Authorization": f"Bearer {member_token}"}
    
    org = make_request(f"{BASE_URL}/organizations", "POST", {"name": "Phase5 Org", "slug": f"phase5-org-{timestamp}"}, owner_headers)
    ws = make_request(f"{BASE_URL}/workspaces", "POST", {
        "organizationId": org["id"],
        "name": "Phase5 WS",
        "slug": f"phase5-ws-{timestamp}"
    }, owner_headers)
    ws_id = ws["id"]
    
    invite = make_request(f"{BASE_URL}/workspaces/{ws_id}/invitations", "POST", {"email": member_email, "role": "MEMBER"}, owner_headers)
    make_request(f"{BASE_URL}/workspaces/invitations/accept", "POST", {"token": invite.get("token", invite.get("rawToken"))}, member_headers)

    project = make_request(f"{BASE_URL}/projects", "POST", {
        "workspaceId": ws_id,
        "name": "Board Project",
        "slug": f"board-proj-{timestamp}"
    }, owner_headers)
    project_id = project["id"]

    make_request(f"{BASE_URL}/projects/{project_id}/members", "POST", {
        "userId": member_res["user"]["id"],
        "role": "PROJECT_MEMBER"
    }, owner_headers)

    print_step("2. Create Task")
    task = make_request(f"{BASE_URL}/tasks", "POST", {
        "projectId": project_id,
        "title": "Build Kanban Board",
        "description": "Implement drag and drop",
        "priority": "HIGH",
        "type": "STORY",
        "assigneeId": member_res["user"]["id"]
    }, owner_headers)
    task_id = task["id"]

    print_step("3. Create Board")
    board = make_request(f"{BASE_URL}/boards", "POST", {
        "projectId": project_id,
        "name": "Sprint 1 Board",
        "template": "KANBAN"
    }, owner_headers)
    board_id = board["id"]

    print_step("4. List Boards")
    make_request(f"{BASE_URL}/boards?projectId={project_id}", "GET", None, owner_headers)

    print_step("5. Create Custom Column")
    custom_col = make_request(f"{BASE_URL}/boards/{board_id}/columns", "POST", {
        "name": "QA Testing",
        "position": 2
    }, owner_headers)
    custom_col_id = custom_col["id"]

    print_step("6. Get Board (To see new columns)")
    full_board = make_request(f"{BASE_URL}/boards/{board_id}", "GET", None, owner_headers)
    
    # KANBAN template creates 3 default columns, so we should move to one of them
    target_column_id = custom_col_id

    print_step("7. Move Task to Board Column")
    make_request(f"{BASE_URL}/boards/{board_id}/tasks/move", "PATCH", {
        "taskId": task_id,
        "columnId": target_column_id,
        "position": 0
    }, member_headers)

    print_step("8. Update Board")
    make_request(f"{BASE_URL}/boards/{board_id}", "PUT", {
        "name": "Sprint 1 Board (Updated)",
        "description": "This board tracks Sprint 1 progress"
    }, owner_headers)

    print_step("9. Update Custom Column")
    make_request(f"{BASE_URL}/boards/{board_id}/columns/{custom_col_id}", "PUT", {
        "name": "QA Review",
        "position": 3
    }, owner_headers)

    print_step("10. Delete Custom Column")
    make_request(f"{BASE_URL}/boards/{board_id}/columns/{custom_col_id}", "DELETE", None, owner_headers)

    print_step("11. Delete Board")
    make_request(f"{BASE_URL}/boards/{board_id}", "DELETE", None, owner_headers)

    print_step("12. Verify Board Deletion")
    make_request(f"{BASE_URL}/boards/{board_id}", "GET", None, owner_headers)


if __name__ == "__main__":
    main()
