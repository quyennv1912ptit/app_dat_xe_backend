package com.nhom15.app_dat_xe.booking.repository

import com.nhom15.app_dat_xe.booking.entity.HandoverPhoto
import org.springframework.data.jpa.repository.JpaRepository

interface HandoverPhotoRepository : JpaRepository<HandoverPhoto, Long> {
    fun findAllByReportIdOrderByCreatedAtAsc(reportId: Long): List<HandoverPhoto>

    fun findAllByReportIdOrderByCreatedAtAscIdAsc(reportId: Long): List<HandoverPhoto>
}
