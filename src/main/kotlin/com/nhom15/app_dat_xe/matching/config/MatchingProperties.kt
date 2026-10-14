package com.nhom15.app_dat_xe.matching.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("matching")
data class MatchingProperties(
    val searchRadiiKm: List<Double> = listOf(2.0, 4.0, 7.0, 10.0),
    val maxCandidatesPerRound: Int = 20,
) {
    init {
        require(searchRadiiKm.isNotEmpty()) { "matching.search-radii-km không được để trống" }
        require(searchRadiiKm.all { it.isFinite() && it > 0.0 }) {
            "matching.search-radii-km phải chứa các bán kính hữu hạn và lớn hơn 0"
        }
        require(searchRadiiKm.zipWithNext().all { (current, next) -> current < next }) {
            "matching.search-radii-km phải tăng dần"
        }
        require(maxCandidatesPerRound > 0) { "matching.max-candidates-per-round phải lớn hơn 0" }
    }
}
