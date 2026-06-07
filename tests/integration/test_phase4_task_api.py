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
    owner_email = f"task_owner_{timestamp}@example.com"
    member_email = f"task_member_{timestamp}@example.com"
    password = "Password123!"

    print_step("1. Setup Users, Workspace and Project")
    make_request(f"{BASE_URL}/auth/register", "POST", {"email": owner_email, "password": password, "displayName": "Task Owner"})
    make_request(f"{BASE_URL}/auth/register", "POST", {"email": member_email, "password": password, "displayName": "Task Member"})
    
    owner_res = make_request(f"{BASE_URL}/auth/login", "POST", {"email": owner_email, "password": password})
    member_res = make_request(f"{BASE_URL}/auth/login", "POST", {"email": member_email, "password": password})
    
    owner_token = owner_res["accessToken"]
    member_token = member_res["accessToken"]
    owner_headers = {"Authorization": f"Bearer {owner_token}"}
    member_headers = {"Authorization": f"Bearer {member_token}"}
    
    org = make_request(f"{BASE_URL}/organizations", "POST", {"name": "Phase4 Org", "slug": f"phase4-org-{timestamp}"}, owner_headers)
    ws = make_request(f"{BASE_URL}/workspaces", "POST", {
        "organizationId": org["id"],
        "name": "Phase4 WS",
        "slug": f"phase4-ws-{timestamp}"
    }, owner_headers)
    ws_id = ws["id"]
    
    invite = make_request(f"{BASE_URL}/workspaces/{ws_id}/invitations", "POST", {"email": member_email, "role": "MEMBER"}, owner_headers)
    make_request(f"{BASE_URL}/workspaces/invitations/accept", "POST", {"token": invite.get("token", invite.get("rawToken"))}, member_headers)

    project = make_request(f"{BASE_URL}/projects", "POST", {
        "workspaceId": ws_id,
        "name": "Task Project",
        "slug": f"task-proj-{timestamp}"
    }, owner_headers)
    project_id = project["id"]

    make_request(f"{BASE_URL}/projects/{project_id}/members", "POST", {
        "userId": member_res["user"]["id"],
        "role": "PROJECT_MEMBER"
    }, owner_headers)

    print_step("2. Create Label")
    label = make_request(f"{BASE_URL}/labels", "POST", {
        "projectId": project_id,
        "name": "Bug",
        "color": "#FF0000"
    }, owner_headers)
    label_id = label["id"]

    print_step("3. Create Task")
    task = make_request(f"{BASE_URL}/tasks", "POST", {
        "projectId": project_id,
        "title": "Fix login bug",
        "description": "Users cannot login",
        "priority": "HIGH",
        "type": "BUG",
        "assigneeId": member_res["user"]["id"]
    }, owner_headers)
    task_id = task["id"]

    print_step("4. List Tasks")
    make_request(f"{BASE_URL}/tasks?projectId={project_id}", "GET", None, owner_headers)

    print_step("5. Update Task")
    make_request(f"{BASE_URL}/tasks/{task_id}", "PUT", {
        "title": "Fix login bug (Critical)",
        "priority": "URGENT",
        "status": "IN_PROGRESS"
    }, owner_headers)

    print_step("6. Assign Task")
    make_request(f"{BASE_URL}/tasks/{task_id}/assignee", "PATCH", {
        "assigneeId": owner_res["user"]["id"]
    }, owner_headers)

    print_step("7. Change Status")
    make_request(f"{BASE_URL}/tasks/{task_id}/status", "PATCH", {
        "status": "IN_REVIEW"
    }, member_headers)

    print_step("8. Add Label")
    make_request(f"{BASE_URL}/tasks/{task_id}/labels", "POST", {
        "labelId": label_id
    }, owner_headers)

    print_step("9. Add Comment")
    comment = make_request(f"{BASE_URL}/tasks/{task_id}/comments", "POST", {
        "body": "I found the issue!"
    }, member_headers)
    comment_id = comment["id"]

    print_step("10. List Comments")
    make_request(f"{BASE_URL}/tasks/{task_id}/comments", "GET", None, owner_headers)

    print_step("11. Update Comment")
    make_request(f"{BASE_URL}/tasks/{task_id}/comments/{comment_id}", "PUT", {
        "body": "I found the issue! It is a typo."
    }, member_headers)

    print_step("12. Remove Label")
    make_request(f"{BASE_URL}/tasks/{task_id}/labels/{label_id}", "DELETE", None, owner_headers)

    print_step("13. Delete Task")
    make_request(f"{BASE_URL}/tasks/{task_id}", "DELETE", None, owner_headers)

    print_step("14. Verify Task Deletion")
    make_request(f"{BASE_URL}/tasks/{task_id}", "GET", None, owner_headers)


if __name__ == "__main__":
    main()
