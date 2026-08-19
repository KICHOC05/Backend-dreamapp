package team.dreamapp.com.domain.repository.account

import team.dreamapp.com.domain.entity.account.UserAccount
import team.dreamapp.com.domain.entity.auth.UserInfo

interface UserAccountRepository {
    fun insert(userAccount: UserAccount): String
    fun update(userAccount: UserAccount): String
    fun delete(uuid: String): String
    fun getAll(where: String): List<UserAccount>
    fun getByUUID(uuid: String): UserAccount?
    fun userInfoBy(type: String, param: String): UserInfo
}