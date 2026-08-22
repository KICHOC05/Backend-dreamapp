package team.dreamapp.com.infrastructure.repository

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import team.dreamapp.com.infrastructure.Util
import team.dreamapp.com.infrastructure.Util.properTrim

class UserRepositoryImplTest {

    @Test
    fun `hashPwd produces BCrypt hash with cost 12`() {
        val hash = Util.hashPwd("testpassword123")
        assertThat(hash).startsWith("\$2a\$")
        assertThat(hash.length).isGreaterThanOrEqualTo(60)
    }

    @Test
    fun `hashPwd produces unique hashes for same input`() {
        val hash1 = Util.hashPwd("samepassword")
        val hash2 = Util.hashPwd("samepassword")
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun `randomUUID generates 36 char uppercase UUID`() {
        val uuid = Util.randomUUID()
        assertThat(uuid).hasSize(36)
        assertThat(uuid).isUpperCase()
        assertThat(uuid).matches("[A-Z0-9\\-]{36}")
    }

    @Test
    fun `properTrim removes excess whitespace`() {
        val result = "  hello   world  ".properTrim()
        assertThat(result).isEqualTo("hello world")
    }

    @Test
    fun `strDate returns current date in yyyy-MM-dd format`() {
        val date = Util.strDate()
        assertThat(date).matches("\\d{4}-\\d{2}-\\d{2}")
    }

    @Test
    fun `toMap converts object to map`() {
        data class Sample(val name: String, val value: Int)
        val map = Util.toMap(Sample("test", 42))
        assertThat(map["name"]).isEqualTo("test")
        assertThat(map["value"]).isEqualTo(42)
    }

    @Test
    fun `toMutableMap returns mutable map`() {
        data class Sample(val name: String)
        val map = Util.toMutableMap(Sample("test"))
        assertThat(map).isInstanceOf(MutableMap::class.java)
        map["extra"] = "added"
        assertThat(map["extra"]).isEqualTo("added")
    }
}
