package com.nhom15.app_dat_xe.uploads

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/uploads")
class UploadSignatureController(private val signatureService: UploadSignatureService) {
    @GetMapping("/signature")
    fun getSignature(@CurrentUser actor: AuthUser): ApiResponse<UploadSignatureResponse> =
        ApiResponse.ok(signatureService.create(actor))
}
