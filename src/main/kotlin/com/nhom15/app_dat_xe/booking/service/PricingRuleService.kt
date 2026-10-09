package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.booking.api.dto.request.CreatePricingRuleRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.UpdatePricingRuleRequest
import com.nhom15.app_dat_xe.booking.api.dto.response.PricingRuleResponse
import com.nhom15.app_dat_xe.booking.entity.PricingRule
import com.nhom15.app_dat_xe.booking.repository.PricingRuleRepository
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.security.AuthUser
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime

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
                PricingRuleResponse(
                    id = rule.id,
                    baseFare = rule.baseFare,
                    perKm = rule.perKm,
                    perMinuteWaiting = rule.perMinuteWaiting,
                    nightSurchargePercent = rule.nightSurchargePercent,
                    nightStart = rule.nightStart,
                    nightEnd = rule.nightEnd,
                    returnFeeForDriver = rule.returnFeeForDriver,
                    minFare = rule.minFare,
                    active = rule.active,
                    createdAt = rule.createdAt,
                    updatedAt = rule.updatedAt,
                ),
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
        return PricingRuleResponse(
            id = savedRule.id,
            baseFare = savedRule.baseFare,
            perKm = savedRule.perKm,
            perMinuteWaiting = savedRule.perMinuteWaiting,
            nightSurchargePercent = savedRule.nightSurchargePercent,
            nightStart = savedRule.nightStart,
            nightEnd = savedRule.nightEnd,
            returnFeeForDriver = savedRule.returnFeeForDriver,
            minFare = savedRule.minFare,
            active = savedRule.active,
            createdAt = savedRule.createdAt,
            updatedAt = savedRule.updatedAt,
        )
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
        return PricingRuleResponse(
            id = savedRule.id,
            baseFare = savedRule.baseFare,
            perKm = savedRule.perKm,
            perMinuteWaiting = savedRule.perMinuteWaiting,
            nightSurchargePercent = savedRule.nightSurchargePercent,
            nightStart = savedRule.nightStart,
            nightEnd = savedRule.nightEnd,
            returnFeeForDriver = savedRule.returnFeeForDriver,
            minFare = savedRule.minFare,
            active = savedRule.active,
            createdAt = savedRule.createdAt,
            updatedAt = savedRule.updatedAt,
        )
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
