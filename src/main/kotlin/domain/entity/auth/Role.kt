package team.dreamapp.com.domain.entity.auth

import io.javalin.security.RouteRole

// Roles to user account
enum class Role : RouteRole {
    SYSADMIN, ADMIN, CLIENT, UNAUTHENTICATED
}