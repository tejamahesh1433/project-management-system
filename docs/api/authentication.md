# Phase 1 Authentication API

Base path: `/api/v1/auth`

## Capabilities

- User registration
- User login
- JWT access token issuance
- JWT refresh token persistence and rotation
- Role based access control through Spring Security authorities
- Redis-backed access token blacklist for logout
- Forgot password token generation
- Reset password

Workspace functionality is intentionally not included in Phase 1.

## Roles

```text
USER
ADMIN
```

JWT access tokens include a `roles` claim. Spring Security maps persisted roles to authorities using the `ROLE_` prefix.

## Endpoints

### Register

`POST /api/v1/auth/register`

Request:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "displayName": "Example User"
}
```

Response:

```json
{
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "displayName": "Example User",
    "roles": ["USER"]
  },
  "accessToken": "jwt",
  "refreshToken": "opaque-token",
  "tokenType": "Bearer",
  "expiresInSeconds": 900
}
```

### Login

`POST /api/v1/auth/login`

Request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response: same shape as register.

### Refresh Token

`POST /api/v1/auth/refresh`

Request:

```json
{
  "refreshToken": "opaque-token"
}
```

Response: new access token and rotated refresh token.

### Logout

`POST /api/v1/auth/logout`

Headers:

```text
Authorization: Bearer <access-token>
```

Request:

```json
{
  "refreshToken": "opaque-token"
}
```

Response:

```json
{
  "message": "Logged out"
}
```

### Forgot Password

`POST /api/v1/auth/forgot-password`

Request:

```json
{
  "email": "user@example.com"
}
```

Development response:

```json
{
  "message": "Password reset token generated",
  "resetToken": "opaque-token"
}
```

For unknown emails, the API returns a generic message and no token.

### Reset Password

`POST /api/v1/auth/reset-password`

Request:

```json
{
  "token": "opaque-token",
  "newPassword": "newPassword123"
}
```

Response:

```json
{
  "message": "Password reset successful"
}
```

## Persistence

PostgreSQL tables:

- `users`
- `roles`
- `user_roles`
- `refresh_tokens`
- `password_reset_tokens`

Redis keys:

- `auth:blacklist:<access-token>`

## Environment

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/project_management_saas
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=change-me-change-me-change-me-change-me-change-me-change-me
JWT_ACCESS_TOKEN_TTL_MINUTES=15
JWT_REFRESH_TOKEN_TTL_DAYS=30
PASSWORD_RESET_TOKEN_TTL_MINUTES=30
JWT_ISSUER=project-management-saas
```
