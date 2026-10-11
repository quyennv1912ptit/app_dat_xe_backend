
package com.nhom15.app_dat_xe.common.port

data class BookingRatingInfo(
    val customerId: Long,
    val driverId: Long?,
    val status: String
)

interface BookingRatingPort {

    fun getRatingInfo(bookingId: Long): BookingRatingInfo?

    fun canRate(
        bookingId: Long,
        userId: Long
    ): Boolean
}