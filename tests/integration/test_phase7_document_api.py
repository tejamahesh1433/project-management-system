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
    owner_email = f"doc_owner_{timestamp}@example.com"
    password = "Password123!"

    print_step("1. Setup User, Workspace and Project")
    make_request(f"{BASE_URL}/auth/register", "POST", {"email": owner_email, "password": password, "displayName": "Doc Owner"})
    owner_res = make_request(f"{BASE_URL}/auth/login", "POST", {"email": owner_email, "password": password})
    owner_token = owner_res["accessToken"]
    owner_headers = {"Authorization": f"Bearer {owner_token}"}
    
    org = make_request(f"{BASE_URL}/organizations", "POST", {"name": "Phase7 Org", "slug": f"phase7-org-{timestamp}"}, owner_headers)
    ws = make_request(f"{BASE_URL}/workspaces", "POST", {
        "organizationId": org["id"],
        "name": "Phase7 WS",
        "slug": f"phase7-ws-{timestamp}"
    }, owner_headers)
    ws_id = ws["id"]
    
    project = make_request(f"{BASE_URL}/projects", "POST", {
        "workspaceId": ws_id,
        "name": "Doc Project",
        "slug": f"doc-proj-{timestamp}"
    }, owner_headers)
    project_id = project["id"]

    print_step("2. Create Root Folder")
    folder1 = make_request(f"{BASE_URL}/folders", "POST", {
        "projectId": project_id,
        "name": "Engineering Docs"
    }, owner_headers)
    folder1_id = folder1["id"]

    print_step("3. Create Sub Folder")
    folder2 = make_request(f"{BASE_URL}/folders", "POST", {
        "projectId": project_id,
        "parentFolderId": folder1_id,
        "name": "Architecture"
    }, owner_headers)

    print_step("4. Create Document in Root Folder")
    doc = make_request(f"{BASE_URL}/documents", "POST", {
        "projectId": project_id,
        "folderId": folder1_id,
        "title": "API Guidelines",
        "content": "Use RESTful practices."
    }, owner_headers)
    doc_id = doc["id"]

    print_step("5. Update Document (Creates a new Version)")
    make_request(f"{BASE_URL}/documents/{doc_id}", "PUT", {
        "title": "API Guidelines V2",
        "content": "Use RESTful practices. Use JSON."
    }, owner_headers)

    print_step("6. Get Document Versions")
    make_request(f"{BASE_URL}/documents/{doc_id}/versions", "GET", None, owner_headers)

    make_request(f"{BASE_URL}/documents/{doc_id}", "GET", None, owner_headers)

    print_step("9. Upload File Asset in Sub Folder")
    file = make_request(f"{BASE_URL}/files", "POST", {
        "projectId": project_id,
        "folderId": folder2["id"],
        "fileName": "diagram.png",
        "contentType": "image/png",
        "sizeBytes": 10245
    }, owner_headers)
    file_id = file["id"]

    make_request(f"{BASE_URL}/files/{file_id}", "DELETE", None, owner_headers)

    print_step("12. Delete Document")
    make_request(f"{BASE_URL}/documents/{doc_id}", "DELETE", None, owner_headers)

    print_step("13. Delete Root Folder (Should cascade/delete subfolders)")
    make_request(f"{BASE_URL}/folders/{folder1_id}", "DELETE", None, owner_headers)


if __name__ == "__main__":
    main()
