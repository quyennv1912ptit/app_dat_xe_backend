package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.exception.AppException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Service
class MapboxDirectionsClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${mapbox.api.navigation.url}") baseUrl: String,
    @Value("\${mapbox.api.navigation.key}") private val accessToken: String,
) : DrivingRouteProvider {

    private val restClient = restClientBuilder
        .baseUrl(baseUrl.trimEnd('/'))
        .build()

    override fun findRoute(pickup: GeoPoint, dropoff: GeoPoint): DrivingRoute {
        try {
            val response = restClient.get()
                .uri { builder ->
                    builder
                        .path("/directions/v5/mapbox/driving/{pickup};{dropoff}")
                        .queryParam("alternatives", false)
                        .queryParam("overview", "false")
                        .queryParam("steps", false)
                        .queryParam("access_token", accessToken)
                        .build(pickup.asMapboxCoordinate(), dropoff.asMapboxCoordinate())
                }
                .retrieve()
                .body(MapboxDirectionsResponse::class.java)

            val route = response?.routes?.firstOrNull()
            if (response?.code != "Ok" || route == null) {
                throw routeUnavailable()
            }
            if (!route.distance.isFinite() || route.distance < 0.0 ||
                !route.duration.isFinite() || route.duration < 0.0
            ) {
                throw routeUnavailable()
            }

            return DrivingRoute(
                distanceMeters = route.distance,
                durationSeconds = route.duration,
            )
        } catch (exception: RestClientException) {
            // Không gắn exception gốc vì request URL có chứa access token.
            throw routeUnavailable()
        }
    }

    private fun GeoPoint.asMapboxCoordinate(): String = "$lng,$lat"

    private fun routeUnavailable() = AppException(ErrorCode.ROUTE_UNAVAILABLE)

    private data class MapboxDirectionsResponse(
        val code: String? = null,
        val routes: List<MapboxRoute> = emptyList(),
    )

    private data class MapboxRoute(
        val distance: Double = Double.NaN,
        val duration: Double = Double.NaN,
    )
}
