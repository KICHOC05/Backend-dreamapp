package team.dreamapp.com.infrastructure.repository

import io.javalin.http.BadRequestResponse
import io.javalin.http.InternalServerErrorResponse
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import org.slf4j.LoggerFactory
import team.dreamapp.com.domain.entity.auth.UserInfo
import team.dreamapp.com.domain.repository.account.UserAccountRepository
import team.dreamapp.com.domain.entity.account.UserAccount
import team.dreamapp.com.infrastructure.Util.dbErrorHandler
import team.dreamapp.com.infrastructure.Util.hashPwd
import team.dreamapp.com.infrastructure.Util.toMutableMap
import team.dreamapp.com.infrastructure.datasouce.authdatabase.AuthDataSource
import java.sql.SQLException

class UserAccountRepositoryImpl : UserAccountRepository {
    val logger = LoggerFactory.getLogger("UserAccountRepositoryImpl")

    private fun toUser(row: Row) = UserAccount(
        id = row.string("ID"),
        userName = row.string("USERNAME"),
        roles = row.string("USER_ROLES").split(","),
        firstName = row.string("FIRSTNAME"),
        lastName = row.string("LASTNAME"),
        mobilePhone = row.string("MOBILE_PHONE"),
        phoneOffice = row.string("PHONE_OFFICE"),
        phoneExt = row.string("PHONE_EXT"),
        email = row.string("EMAIL"),
        active = row.boolean("IS_ACTIVE")
    )

    override fun insert(userAccount: UserAccount): String {
        val accMap = toMutableMap(userAccount).apply { put("rolesToString", userAccount.rolesToStr()) }
        val qry = queryOf("""
            INSERT INTO USER_ACCOUNT(ID, USERNAME, FIRSTNAME, LASTNAME, USER_PASSWORD,
              USER_ROLES, MOBILE_PHONE, PHONE_OFFICE, PHONE_EXT, EMAIL, IS_ACTIVE)
            VALUES (CAST(:id AS UUID), :userName, :firstName, :lastName, :password, :rolesToString, :mobilePhone,
                :phoneOffice, :phoneExt, :email, :active)""".trimIndent(), accMap)
        var result = "failed"
        sessionOf(AuthDataSource.get()).use {
            try {
                val updateResult = it.run(qry.asUpdate)
                result = if (updateResult > 0) userAccount.id else "failed"
            } catch (ex: SQLException) {
                logger.error("SQLException al insertar usuario: ${ex.message}", ex)
                dbErrorHandler(logger, ex.message)
            } catch (ex: Exception) {
                logger.error("Exception al insertar usuario: ${ex.message}", ex)
            }
        }
        return result
    }

    override fun update(userAccount: UserAccount): String {
        val password: String = if (!userAccount.password.startsWith("*")) {
            userAccount.password =  hashPwd(userAccount.password)
            "USER_PASSWORD = :password,"
        } else ""
        val accMap = toMutableMap(userAccount).apply { put("rolesToString", userAccount.rolesToStr()) }
        val qry = queryOf("""
            UPDATE USER_ACCOUNT SET
                USERNAME = :userName, 
                FIRSTNAME = :firstName, 
                LASTNAME = :lastName, 
                $password
                USER_ROLES = :rolesToString, 
                MOBILE_PHONE = :mobilePhone,
                PHONE_OFFICE = :phoneOffice,
                PHONE_EXT = :phoneExt,
                EMAIL = :email, 
                IS_ACTIVE = :active
            WHERE ID = CAST(:id AS UUID)""".trimIndent(), accMap)
        var result = "failed"
        sessionOf(AuthDataSource.get()).use {
            try {
                result = if (it.run(qry.asUpdate) > 0) "success" else "failed"
            } catch (ex: SQLException) {
                dbErrorHandler(logger, ex.message)
            }
        }
        return  result
    }

    override fun delete(uuid: String): String {
        val qry = queryOf("DELETE FROM USER_ACCOUNT WHERE ID = CAST(? AS UUID)", uuid)
        var result: String
        sessionOf(AuthDataSource.get()).use {
            result = if (it.run(qry.asUpdate) > 0) "success" else "failed"
        }
        return result
    }

    override fun getAll(where: String): List<UserAccount> {
        val qry = queryOf("""
        SELECT CAST(ID AS VARCHAR) AS "ID", USERNAME, FIRSTNAME, LASTNAME, USER_ROLES, MOBILE_PHONE, PHONE_OFFICE,
            PHONE_EXT, EMAIL, IS_ACTIVE
        FROM USER_ACCOUNT
        $where
        ORDER BY FIRSTNAME, LASTNAME""".trimIndent())
            .map { row -> toUser(row) }.asList
        var accounts: List<UserAccount>
        sessionOf(AuthDataSource.get()).use {
            accounts = it.run(qry)
        }
        return accounts
    }

    override fun getByUUID(uuid: String): UserAccount? {
        val qry = queryOf("""
            SELECT CAST(ID AS VARCHAR) AS "ID", USERNAME, FIRSTNAME, LASTNAME, USER_ROLES, MOBILE_PHONE, PHONE_OFFICE,
                PHONE_EXT, EMAIL, IS_ACTIVE
            FROM USER_ACCOUNT
            WHERE ID = CAST(? AS UUID)""".trimIndent(), uuid)
            .map { row -> toUser(row) }.asSingle
        val account: UserAccount
        sessionOf(AuthDataSource.get()).use {
            account = it.run(qry) ?: throw InternalServerErrorResponse("No existe ese ID")
        }
        return account
    }

    override fun userInfoBy(type: String, param: String): UserInfo {
        val where = if (type == "ID") "WHERE ID = CAST(? AS UUID)" else "WHERE USERNAME = ?"
        val qry = queryOf("""
            SELECT CAST(ID AS VARCHAR) AS "ID", USERNAME, FIRSTNAME, LASTNAME, USER_PASSWORD,
                USER_ROLES, IS_ACTIVE, TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD') AS "CURRENT_DATE"
            FROM USER_ACCOUNT
            $where""".trimIndent(), param)
            .map { row -> UserInfo(
                id = row.string("ID"),
                userName = row.string("USERNAME"),
                password = row.string("USER_PASSWORD"),
                fullname = "${row.string("FIRSTNAME")} ${row.string("LASTNAME")}",
                roles = row.string("USER_ROLES").split(","),
                active = row.boolean("IS_ACTIVE"),
                currentDate = row.string("CURRENT_DATE")
            ) }.asSingle
        var account: UserInfo
        sessionOf(AuthDataSource.get()).use {
            account = it.run(qry) ?: throw BadRequestResponse("No existe esta cuenta")
        }
        return account
    }
}
