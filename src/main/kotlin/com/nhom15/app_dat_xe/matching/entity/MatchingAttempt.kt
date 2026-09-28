package com.nhom15.app_dat_xe.matching.entity

data class MatchingAttempt(
    val id: Long,
    val bookingId: Long,
    val round: Int,
    val radiusKm: Double,
    val candidateCount: Int,
    val result: String
)