package com.nhom15.app_dat_xe.ride.service

import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.ride.dto.request.CreatePricingRuleRequest
import com.nhom15.app_dat_xe.ride.dto.request.UpdatePricingRuleRequest
import com.nhom15.app_dat_xe.ride.dto.response.PricingRuleResponse
import com.nhom15.app_dat_xe.ride.entity.PricingRule
import com.nhom15.app_dat_xe.ride.repository.PricingRuleRepository
import java.time.LocalTime
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PricingRuleService(
    private val pricingRuleRepository: PricingRuleRepository,
) {
    @Transactional(readOnly = true)
    fun listRules(actor: AuthUser): List<PricingRuleResponse> {
        if (actor.role != Role.ADMIN) {
            throw ForbiddenException(message = "Chỉ admin mới có thể xem danh sách bảng giá")
        }

        val sort = Sort.by(Sort.Direction.DESC, "createdAt", "id")
        val rules = pricingRuleRepository.findAll(sort)
        val responses = mutableListOf<PricingRuleResponse>()
        for (rule in rules) {
            responses.add(
                PricingRuleResponseMapper.toResponse(rule),
            )
        }
        return responses
    }

    @Transactional
    fun createRule(actor: AuthUser, request: CreatePricingRuleRequest): PricingRuleResponse {
        if (actor.role != Role.ADMIN) {
            throw ForbiddenException(message = "Chỉ admin mới có thể tạo bảng giá")
        }
        validateCreate(request)

        val rule = PricingRule(
            baseFare = request.baseFare,
            perKm = request.perKm,
            perMinuteWaiting = request.perMinuteWaiting,
            nightSurchargePercent = request.nightSurchargePercent,
            nightStart = request.nightStart,
            nightEnd = request.nightEnd,
            returnFeeForDriver = request.returnFeeForDriver,
            minFare = request.minFare,
            active = request.active,
        )
        val savedRule = pricingRuleRepository.save(rule)
        return PricingRuleResponseMapper.toResponse(savedRule)
    }

    @Transactional
    fun updateRule(actor: AuthUser, ruleId: Long, request: UpdatePricingRuleRequest): PricingRuleResponse {
        if (actor.role != Role.ADMIN) {
            throw ForbiddenException(message = "Chỉ admin mới có thể sửa bảng giá")
        }
        if (ruleId <= 0) {
            throw BadRequestException(message = "ID bảng giá phải lớn hơn 0")
        }
        validateUpdate(request)

        val rule = pricingRuleRepository.findById(ruleId).orElse(null)
        if (rule == null) {
            throw NotFoundException(ErrorCode.PRICING_RULE_NOT_FOUND, message = "Bảng giá không tồn tại")
        }

        rule.baseFare = request.baseFare
        rule.perKm = request.perKm
        rule.perMinuteWaiting = request.perMinuteWaiting
        rule.nightSurchargePercent = request.nightSurchargePercent
        rule.nightStart = request.nightStart
        rule.nightEnd = request.nightEnd
        rule.returnFeeForDriver = request.returnFeeForDriver
        rule.minFare = request.minFare
        rule.active = request.active

        // Flush để JPA auditing cập nhật updatedAt trước khi tạo response.
        val savedRule = pricingRuleRepository.saveAndFlush(rule)
        return PricingRuleResponseMapper.toResponse(savedRule)
    }

    fun validateCreate(request: CreatePricingRuleRequest) {
        validateValues(
            baseFare = request.baseFare,
            perKm = request.perKm,
            perMinuteWaiting = request.perMinuteWaiting,
            nightSurchargePercent = request.nightSurchargePercent,
            nightStart = request.nightStart,
            nightEnd = request.nightEnd,
            returnFeeForDriver = request.returnFeeForDriver,
            minFare = request.minFare,
        )
    }

    fun validateUpdate(request: UpdatePricingRuleRequest) {
        validateValues(
            baseFare = request.baseFare,
            perKm = request.perKm,
            perMinuteWaiting = request.perMinuteWaiting,
            nightSurchargePercent = request.nightSurchargePercent,
            nightStart = request.nightStart,
            nightEnd = request.nightEnd,
            returnFeeForDriver = request.returnFeeForDriver,
            minFare = request.minFare,
        )
    }

    private fun validateValues(
        baseFare: Long,
        perKm: Long,
        perMinuteWaiting: Long,
        nightSurchargePercent: Int,
        nightStart: LocalTime?,
        nightEnd: LocalTime?,
        returnFeeForDriver: Long,
        minFare: Long,
    ) {
        if (baseFare < 0) {
            throw BadRequestException(message = "Giá cơ bản không được âm")
        }
        if (perKm < 0) {
            throw BadRequestException(message = "Giá mỗi km không được âm")
        }
        if (perMinuteWaiting < 0) {
            throw BadRequestException(message = "Giá mỗi phút chờ không được âm")
        }
        if (returnFeeForDriver < 0) {
            throw BadRequestException(message = "Phí quay về cho tài xế không được âm")
        }
        if (minFare < 0) {
            throw BadRequestException(message = "Giá tối thiểu không được âm")
        }
        if (nightSurchargePercent < 0 || nightSurchargePercent > 100) {
            throw BadRequestException(message = "Phần trăm phụ thu đêm phải từ 0 đến 100")
        }
        if (nightStart == null && nightEnd == null) {
            if (nightSurchargePercent > 0) {
                throw BadRequestException(message = "Phụ thu đêm cần có giờ bắt đầu và giờ kết thúc")
            }
            return
        }
        if (nightStart == null || nightEnd == null) {
            throw BadRequestException(message = "Giờ bắt đầu và giờ kết thúc phụ thu đêm phải đi cùng nhau")
        }
        if (nightStart == nightEnd) {
            throw BadRequestException(message = "Giờ bắt đầu và giờ kết thúc phụ thu đêm phải khác nhau")
        }
    }
}
