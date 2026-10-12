package com.nhom15.app_dat_xe.account.service

import com.nhom15.app_dat_xe.account.entity.Drivers
import com.nhom15.app_dat_xe.account.entity.Users
import com.nhom15.app_dat_xe.account.repository.DriversRepository
import com.nhom15.app_dat_xe.account.repository.UsersRepository
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import org.springframework.stereotype.Service

/** Góc nhìn chung của Users (CUSTOMER) và Drivers (DRIVER) để service khỏi phải rẽ nhánh theo role. */
data class Account(
    val id: Long,
    val firebaseUid: String,
    val fullName: String?,
    val email: String?,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val role: Role
) {
    val hasPhone: Boolean get() = !phoneNumber.isNullOrBlank()
}

/** Nơi duy nhất biết bảng nào ứng với role nào. AuthService, ProfileService, filter đều đi qua đây. */
@Service
class AccountService(
    private val usersRepository: UsersRepository,
    private val driversRepository: DriversRepository
) {

    fun findByUid(uid: String, role: Role): Account? = when (role) {
        Role.CUSTOMER -> usersRepository.findByFirebaseUid(uid).orElse(null)?.toAccount()
        Role.DRIVER -> driversRepository.findByFirebaseUid(uid).orElse(null)?.toAccount()
        Role.ADMIN -> null
    }

    /** Tìm theo uid ở cả hai bảng (hoặc chỉ [only] nếu có). Dùng cho filter khi chưa biết role. */
    fun findAllByUid(uid: String, only: Role? = null): List<Account> =
        listOf(Role.CUSTOMER, Role.DRIVER)
            .filter { only == null || it == only }
            .mapNotNull { findByUid(uid, it) }

    fun getById(id: Long, role: Role): Account = when (role) {
        Role.CUSTOMER -> usersRepository.findById(id).orElse(null)?.toAccount()
        Role.DRIVER -> driversRepository.findById(id).orElse(null)?.toAccount()
        Role.ADMIN -> null
    } ?: throw notFound(role)

    fun create(uid: String, role: Role, fullName: String?, email: String?, phoneNumber: String): Account =
        when (role) {
            Role.CUSTOMER -> usersRepository.save(Users().also {
                it.firebaseUid = uid
                it.fullName = fullName
                it.email = email
                it.phoneNumber = phoneNumber
            }).toAccount()

            Role.DRIVER -> driversRepository.save(Drivers().also {
                it.firebaseUid = uid
                it.fullName = fullName
                it.email = email
                it.phoneNumber = phoneNumber
            }).toAccount()

            Role.ADMIN -> unsupported(role)
        }

    fun updatePhone(id: Long, role: Role, phoneNumber: String): Account = when (role) {
        Role.CUSTOMER -> {
            val user = usersRepository.findById(id).orElseThrow { notFound(role) }
            user.phoneNumber = phoneNumber
            usersRepository.save(user).toAccount()
        }

        Role.DRIVER -> {
            val driver = driversRepository.findById(id).orElseThrow { notFound(role) }
            driver.phoneNumber = phoneNumber
            driversRepository.save(driver).toAccount()
        }

        Role.ADMIN -> unsupported(role)
    }

    fun updateAvatar(id: Long, role: Role, avatarUrl: String): Account = when (role) {
        Role.CUSTOMER -> {
            val user = usersRepository.findById(id).orElseThrow { notFound(role) }
            user.avatarUrl = avatarUrl
            usersRepository.save(user).toAccount()
        }

        Role.DRIVER -> {
            val driver = driversRepository.findById(id).orElseThrow { notFound(role) }
            driver.avatarUrl = avatarUrl
            driversRepository.save(driver).toAccount()
        }

        Role.ADMIN -> unsupported(role)
    }

    private fun notFound(role: Role) =
        NotFoundException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy tài khoản $role")

    private fun unsupported(role: Role): Nothing =
        throw BadRequestException(ErrorCode.VALIDATION_ERROR, "Role $role không được hỗ trợ")

    private fun Users.toAccount() = Account(
        id = requireNotNull(id), firebaseUid = firebaseUid.orEmpty(), fullName = fullName,
        email = email, phoneNumber = phoneNumber, avatarUrl = avatarUrl, role = Role.CUSTOMER
    )

    private fun Drivers.toAccount() = Account(
        id = requireNotNull(id), firebaseUid = firebaseUid.orEmpty(), fullName = fullName,
        email = email, phoneNumber = phoneNumber, avatarUrl = avatarUrl, role = Role.DRIVER
    )
}
