package  com.nhom15.app_dat_xe.common.domain

import jakarta.persistence.Embeddable

/**
 * Toạ độ (vĩ độ, kinh độ).
 *
 * Khi một entity có 2 GeoPoint (pickup và dropoff) phải đổi tên cột bằng @AttributeOverrides:
 *
 *   @Embedded
 *   @AttributeOverrides(
 *       AttributeOverride(name = "lat", column = Column(name = "pickup_lat")),
 *       AttributeOverride(name = "lng", column = Column(name = "pickup_lng"))
 *   )
 *   var pickup: GeoPoint
 */
@Embeddable
data class GeoPoint(
    val lat: Double,
    val lng: Double
)
