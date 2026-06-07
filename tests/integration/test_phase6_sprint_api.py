import json
import urllib.request
import urllib.error
import time
from datetime import datetime, timedelta

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
    owner_email = f"sprint_owner_{timestamp}@example.com"
    password = "Password123!"

    print_step("1. Setup User, Workspace and Project")
    make_request(f"{BASE_URL}/auth/register", "POST", {"email": owner_email, "password": password, "displayName": "Sprint Owner"})
    owner_res = make_request(f"{BASE_URL}/auth/login", "POST", {"email": owner_email, "password": password})
    owner_token = owner_res["accessToken"]
    owner_headers = {"Authorization": f"Bearer {owner_token}"}
    
    org = make_request(f"{BASE_URL}/organizations", "POST", {"name": "Phase6 Org", "slug": f"phase6-org-{timestamp}"}, owner_headers)
    ws = make_request(f"{BASE_URL}/workspaces", "POST", {
        "organizationId": org["id"],
        "name": "Phase6 WS",
        "slug": f"phase6-ws-{timestamp}"
    }, owner_headers)
    ws_id = ws["id"]
    
    project = make_request(f"{BASE_URL}/projects", "POST", {
        "workspaceId": ws_id,
        "name": "Sprint Project",
        "slug": f"sprint-proj-{timestamp}"
    }, owner_headers)
    project_id = project["id"]

    print_step("2. Create Tasks for Sprint")
    task1 = make_request(f"{BASE_URL}/tasks", "POST", {
        "projectId": project_id,
        "title": "Task 1 for Sprint",
        "priority": "HIGH",
        "type": "STORY"
    }, owner_headers)
    task2 = make_request(f"{BASE_URL}/tasks", "POST", {
        "projectId": project_id,
        "title": "Task 2 for Sprint",
        "priority": "MEDIUM",
        "type": "BUG"
    }, owner_headers)

    print_step("3. Create Sprint")
    now = datetime.utcnow()
    start_date = now.strftime('%Y-%m-%d')
    end_date = (now + timedelta(days=14)).strftime('%Y-%m-%d')

    sprint = make_request(f"{BASE_URL}/sprints", "POST", {
        "projectId": project_id,
        "name": "Sprint 1",
        "goal": "Finish the main dashboard",
        "startDate": start_date,
        "endDate": end_date
    }, owner_headers)
    sprint_id = sprint["id"]

    print_step("4. List Sprints")
    make_request(f"{BASE_URL}/sprints?projectId={project_id}", "GET", None, owner_headers)

    print_step("5. Add Tasks to Sprint")
    make_request(f"{BASE_URL}/sprints/{sprint_id}/tasks", "POST", {
        "taskId": task1["id"]
    }, owner_headers)
    make_request(f"{BASE_URL}/sprints/{sprint_id}/tasks", "POST", {
        "taskId": task2["id"]
    }, owner_headers)

    print_step("6. List Sprint Tasks")
    make_request(f"{BASE_URL}/sprints/{sprint_id}/tasks", "GET", None, owner_headers)

    print_step("7. Start Sprint")
    make_request(f"{BASE_URL}/sprints/{sprint_id}/start", "POST", None, owner_headers)

    print_step("8. Check Sprint Metrics")
    make_request(f"{BASE_URL}/sprints/{sprint_id}/metrics", "GET", None, owner_headers)

    print_step("9. Update Task 1 to DONE (Simulate work)")
    make_request(f"{BASE_URL}/tasks/{task1['id']}", "PUT", {
        "title": "Task 1 for Sprint",
        "status": "DONE",
        "priority": "HIGH",
        "type": "STORY"
    }, owner_headers)

    print_step("10. Check Sprint Metrics Again")
    make_request(f"{BASE_URL}/sprints/{sprint_id}/metrics", "GET", None, owner_headers)

    print_step("11. Complete Sprint")
    make_request(f"{BASE_URL}/sprints/{sprint_id}/complete", "POST", None, owner_headers)

    print_step("12. Delete Sprint")
    make_request(f"{BASE_URL}/sprints/{sprint_id}", "DELETE", None, owner_headers)


if __name__ == "__main__":
    main()
