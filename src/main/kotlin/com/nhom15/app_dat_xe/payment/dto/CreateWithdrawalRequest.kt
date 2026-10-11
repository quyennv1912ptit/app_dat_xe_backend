
package com.nhom15.app_dat_xe.payment.dto

import java.math.BigDecimal

data class CreateWithdrawalRequest(
    val amount: BigDecimal
)