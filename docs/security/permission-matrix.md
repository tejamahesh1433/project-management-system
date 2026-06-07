# Permission Matrix

| Area | OWNER | ADMIN | MEMBER | VIEWER |
| --- | --- | --- | --- | --- |
| Workspace read | Yes | Yes | Yes | Yes |
| Workspace update | Yes | Yes | No | No |
| Workspace delete | Yes | No | No | No |
| Invite workspace members | Yes | Yes | No | No |
| Create projects | Yes | Yes | No | No |

| Project Area | PROJECT_OWNER | PROJECT_ADMIN | PROJECT_MEMBER | PROJECT_VIEWER |
| --- | --- | --- | --- | --- |
| Project read | Yes | Yes | Yes | Yes |
| Project update | Yes | Yes | No | No |
| Manage project members | Yes | Limited | No | No |
| Task read | Yes | Yes | Yes | Yes |
| Task write | Yes | Yes | Yes | No |
| Board write | Yes | Yes | Yes | No |
| Sprint management | Yes | Yes | No | No |
| Documents/files write | Yes | Yes | Yes | No |
| Reports/analytics read | Yes | Yes | Yes | Yes |
| AI assistant read/search | Yes | Yes | Yes | Yes |
| Integrations manage | Workspace membership required | Workspace membership required | Workspace membership required | Workspace membership required |

RC tests cover unauthenticated access, cross-workspace access, cross-project task access, and AI isolation.
