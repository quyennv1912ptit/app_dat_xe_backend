package com.nhom15.app_dat_xe.booking.controller

import com.nhom15.app_dat_xe.booking.api.dto.request.CreatePricingRuleRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.UpdatePricingRuleRequest
import com.nhom15.app_dat_xe.booking.api.dto.response.PricingRuleResponse
import com.nhom15.app_dat_xe.booking.service.PricingRuleService
import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.AppException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/pricing-rules")
class AdminPricingRuleController(
    private val pricingRuleService: PricingRuleService,
) {
    @GetMapping
    fun listRules(@CurrentUser actor: AuthUser?): ApiResponse<List<PricingRuleResponse>> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(pricingRuleService.listRules(actor))
    }

    @PostMapping
    fun createRule(
        @CurrentUser actor: AuthUser?,
        @Valid @RequestBody request: CreatePricingRuleRequest,
    ): ResponseEntity<ApiResponse<PricingRuleResponse>> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        val response = pricingRuleService.createRule(actor, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response))
    }

    @PutMapping
    fun updateRule(
        @CurrentUser actor: AuthUser?,
        @RequestParam("ruleId") ruleId: Long,
        @Valid @RequestBody request: UpdatePricingRuleRequest,
    ): ApiResponse<PricingRuleResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(pricingRuleService.updateRule(actor, ruleId, request))
    }
}
