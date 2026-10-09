package com.nhom15.app_dat_xe.booking.entity

import com.nhom15.app_dat_xe.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "handover_photos")
class HandoverPhoto(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    var report: VehicleHandoverReport,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var angle: HandoverPhotoAngle,

    @Column(name = "file_url", nullable = false, columnDefinition = "text")
    var fileUrl: String,
) : BaseEntity()
