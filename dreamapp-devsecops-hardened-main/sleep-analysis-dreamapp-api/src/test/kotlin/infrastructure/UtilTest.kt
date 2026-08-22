package team.dreamapp.com.infrastructure

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import team.dreamapp.com.infrastructure.Util.properTrim

class UtilTest {

    @Test
    fun `randomUUID returns 36 char string`() {
        val uuid = Util.randomUUID()
        assertThat(uuid).hasSize(36)
        assertThat(uuid).isUpperCase()
    }

    @Test
    fun `randomUUID is unique`() {
        val uuid1 = Util.randomUUID()
        val uuid2 = Util.randomUUID()
        assertThat(uuid1).isNotEqualTo(uuid2)
    }

    @Test
    fun `hashPwd produces BCrypt hash`() {
        val hash = Util.hashPwd("password123")
        assertThat(hash).startsWith("\$2a\$")
        assertThat(hash.length).isGreaterThanOrEqualTo(60)
    }

    @Test
    fun `hashPwd produces different hashes for same password`() {
        val hash1 = Util.hashPwd("password123")
        val hash2 = Util.hashPwd("password123")
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun `properTrim collapses whitespace`() {
        val result = "  hello   world  ".properTrim()
        assertThat(result).isEqualTo("hello world")
    }

    @Test
    fun `properTrim with custom separator`() {
        val result = "hello   world".properTrim("-")
        assertThat(result).isEqualTo("hello-world")
    }

    @Test
    fun `strDate returns current date in yyyy-MM-dd format`() {
        val date = Util.strDate()
        assertThat(date).matches("\\d{4}-\\d{2}-\\d{2}")
    }

    @Test
    fun `toMap converts object to map`() {
        data class TestObj(val name: String, val value: Int)
        val map = Util.toMap(TestObj("test", 42))
        assertThat(map["name"]).isEqualTo("test")
        assertThat(map["value"]).isEqualTo(42)
    }

    @Test
    fun `toMutableMap returns mutable map`() {
        data class TestObj(val name: String)
        val map = Util.toMutableMap(TestObj("test"))
        assertThat(map).isInstanceOf(MutableMap::class.java)
        map["extra"] = "value"
        assertThat(map["extra"]).isEqualTo("value")
    }
}
