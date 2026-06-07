# Project Management SaaS

A self-hosted Project Management SaaS designed with a multi-tenant architecture and robust Role-Based Access Control (RBAC).

## Tech Stack

- Frontend: Next.js 15, TypeScript, Tailwind
- Backend: Spring Boot 3, Java 21, JPA/Hibernate
- Database: PostgreSQL
- Cache: Redis
- Deployment: Docker Compose
- Monitoring: Prometheus, Grafana, Loki

## Domain Hierarchy

```text
Organization (Billing & Top-Level Management)
└── Workspace (Dedicated areas for teams or projects)
    └── Project
        ├── Tasks
        │   ├── Comments
        │   ├── Labels
        │   └── Attachments
        ├── Boards
        ├── Sprints
        ├── Documents
        ├── Files
        ├── Reports
        ├── Analytics
        └── Team
```

## Project Status

The project is being built in phases. Currently completed:

- **Phase 1: User Authentication & Profile Module**
  - JWT-based authentication, user registration, and profile management.
- **Phase 2: Workspace Management Module**
  - Introduced Organizations and Workspaces, providing multi-tenancy.
  - Implemented secure invite system with RBAC (`OWNER`, `ADMIN`, `MEMBER`, `VIEWER`).
- **Phase 3: Project Management Module**
  - Created projects within workspaces.
  - Granular project-level roles (`PROJECT_OWNER`, `PROJECT_ADMIN`, `PROJECT_MEMBER`, `PROJECT_VIEWER`).
- **Phase 4: Task & Issue Management Module**
  - Entities for Tasks, Comments, and Labels.
  - Task assignment, status tracking, and sub-task hierarchies.
  - Integrated with project roles to enforce edit and management privileges.
- **Phase 5: Agile Kanban Boards Module**
  - Implemented kanban boards with templates (SCRUM, KANBAN).
  - Created customizable columns to manage status pipelines.
  - Full support for drag-and-drop task reordering and cross-column transitions.
- **Phase 6: Sprint Management Module**
  - Introduced sprint entities to group tasks within specific timeboxes.
  - Implemented sprint lifecycle management (PLANNED, ACTIVE, COMPLETED, CANCELLED).
  - Built sprint metrics calculations (completion percentages, story points tracking).
  - Added "storyPoints" field to Task entity to integrate seamlessly with Sprint velocity.
- **Phase 7: Document and File Management Module**
  - Designed folder hierarchies with parent/child relationships to structure content.
  - Implemented a Document entity supporting version control and multiple revisions.
  - Built a File Asset system to track physical file uploads, paths, and byte sizes.
  - Deployed Spring Events to broadcast document creations, version bumps, and file operations.
- **Phase 8: Activity and Audit Logs**
  - Implemented a central Activity feed mechanism capturing platform-wide interactions.
  - Subscribed to core Spring Domain Events (ProjectCreated, TaskUpdated, etc.) to trace lifecycle actions.
  - Built an Audit Log module enforcing strict security compliance by tracking IP addresses and user actions.
  - Provided endpoint querying for workspace, project, and entity-specific history feeds.
- **Phase 9: Real-Time Notifications**
  - Designed Notification models including unread statuses and multi-channel preferences.
  - Introduced granular settings (`NotificationPreference`) allowing users to toggle Push/Email/In-App toggles per event type.
  - Integrated Spring WebSockets to emit push events directly to active UI clients.
  - Subscribed to internal domain events to dispatch targeted alerts (e.g., Workspace Invitations).

## Top-Level Structure

```text
frontend/        Next.js 15 application structure
backend/         Spring Boot 3 application structure
database/        PostgreSQL and Redis structure
infrastructure/  Docker Compose and monitoring structure
docs/            Project documentation
scripts/         Development, deployment, and maintenance scripts
tests/           Integration and API testing suites
```
