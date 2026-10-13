
package com.nhom15.app_dat_xe.notification.api

sealed interface Recipient {
    val id: Long
    val key: String
    val role: Role

    data class Driver(
        override val id: Long
    ) : Recipient {
        override val role: Role
            get() = Role.DRIVER

        override val key: String
            get() = principalName(role, id)
    }

    data class User(
        override val id: Long
    ) : Recipient {
        override val role: Role
            get() = Role.USER

        override val key: String
            get() = principalName(role, id)
    }

    enum class Role(val prefix: String) {
        DRIVER("driver"),
        USER("user")
    }

    companion object {
        fun principalName(role: Role, id: Long): String =
            "${role.prefix}:$id"
    }
}
