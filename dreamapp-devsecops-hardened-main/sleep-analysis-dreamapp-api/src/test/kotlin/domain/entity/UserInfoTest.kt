package team.dreamapp.com.domain.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.domain.entity.auth.UserInfo
import io.javalin.http.BadRequestResponse
import org.assertj.core.api.Assertions.assertThat

class UserInfoTest {

    private fun createUserInfo(
        roles: List<String> = listOf("SysAdmin"),
        active: Boolean = true
    ) = UserInfo(
        id = "550e8400-e29b-41d4-a716-446655440000",
        userName = "testuser",
        password = "secret123",
        fullname = "Test User",
        roles = roles,
        active = active,
        currentDate = "2025-01-15"
    )

    @Test
    fun `mapRole sets SYSADMIN correctly`() {
        val userInfo = createUserInfo(roles = listOf("SysAdmin"))
        userInfo.mapRole("SysAdmin")
        assertThat(userInfo.role).isEqualTo(Role.SYSADMIN)
        assertThat(userInfo.password).isEqualTo("**************")
    }

    @Test
    fun `mapRole sets ADMIN correctly`() {
        val userInfo = createUserInfo(roles = listOf("Admin"))
        userInfo.mapRole("Admin")
        assertThat(userInfo.role).isEqualTo(Role.ADMIN)
    }

    @Test
    fun `mapRole sets CLIENT correctly`() {
        val userInfo = createUserInfo(roles = listOf("Cliente"))
        userInfo.mapRole("Cliente")
        assertThat(userInfo.role).isEqualTo(Role.CLIENT)
    }

    @Test
    fun `mapRole throws BadRequestResponse for unknown role`() {
        val userInfo = createUserInfo(roles = listOf("SysAdmin"))
        assertThrows<BadRequestResponse> {
            userInfo.mapRole("UnknownRole")
        }
    }

    @Test
    fun `mapRole is case insensitive`() {
        val userInfo = createUserInfo(roles = listOf("sysadmin"))
        userInfo.mapRole("SYSADMIN")
        assertThat(userInfo.role).isEqualTo(Role.SYSADMIN)
    }

    @Test
    fun `photoUrl contains user id`() {
        val userInfo = createUserInfo()
        assertThat(userInfo.photoUrl).contains(userInfo.id)
    }
}
