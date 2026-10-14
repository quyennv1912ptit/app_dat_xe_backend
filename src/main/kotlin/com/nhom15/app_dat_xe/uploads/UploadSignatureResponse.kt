package com.nhom15.app_dat_xe.uploads

data class UploadSignatureResponse(
    val signature: String,
    val timestamp: Long,
    val apiKey: String,
    val cloudName: String,
    val folder: String,
)
