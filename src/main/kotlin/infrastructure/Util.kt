package team.dreamapp.com.infrastructure

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.http.InternalServerErrorResponse
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object Util {

    /* Convert Timestamp to a format date */
    // "dd-MM-yyyy HH:mm"
    // "dd-MM-yyyy hh:mm a"
    fun parseTimestamp(timestamp: String, format: String = "dd-MM-yyyy hh:mm a"): String {
        val formatter = DateTimeFormatter.ofPattern(format)
        val ts = Timestamp.valueOf(timestamp)
        return ts.toLocalDateTime().format(formatter)
    }

    /* String Proper Trim */
    fun String.properTrim(separator: String = " ") = this.trim().split("\\s+".toRegex()).joinToString(separator)

    /* Generate a 36 char random UUID */
    fun randomUUID() = UUID.randomUUID().toString().uppercase()

    /* */
    fun hashPwd(password: String) = BCrypt.hashpw(password, BCrypt.gensalt())

    /* */
    //fun hashPwd(password: String) = BCrypt.hashpw(password, BCrypt.gensalt())

    /* Simple Date Format dd-MM-yy HH:mm:ss*/
    fun strDateTime(strDate: String, out: String = "dd-MM-yyyy HH:mm:ss"): String {
        val parse = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(strDate)
        return SimpleDateFormat(out).format(parse)
    }

    fun strDate(): String {
        val date = LocalDate.now()
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return date.format(format)
    }

    val mapper = jacksonObjectMapper()
    /* From T Object to Map<String, Any?> */
    fun <T: Any> toMap(obj: T): Map<String, Any?> {
        return mapper.convertValue(obj, object : TypeReference<Map<String, Any?>>() {})
    }
    //
    fun <T: Any> toMutableMap(obj: T): MutableMap<String, Any?> {
        return mapper.convertValue(obj, object : TypeReference<MutableMap<String, Any?>>() {})
    }

//    fun <T : Any> toMap(obj: T): MutableMap<String, Any?> {
//        return (obj::class as KClass<T>).memberProperties.associate { prop ->
//            prop.name to prop.get(obj)?.let { value ->
//                if (value::class.isData) toMap(value) else value
//            }
//        } as MutableMap<String, Any?>
//    }

    /* Database exception handler */
    fun dbErrorHandler(log: Logger, msg: String?) {
        msg?.apply {
            val err = split(";")
            log.error(this)
            throw InternalServerErrorResponse(err[2])
        }
    }

    fun validateDataSource(
        name: String,
        init: () -> Unit,
        isHealthy: () -> Boolean
    ): Boolean {
        val logger = LoggerFactory.getLogger("Main")

        init()

        if (!isHealthy()) {
            logger.error("✖ Could not connect to $name. Aborting...")
            return false
        }

        logger.info("✔ Connection to $name is successful. Continuing process...")
        return true
    }

}