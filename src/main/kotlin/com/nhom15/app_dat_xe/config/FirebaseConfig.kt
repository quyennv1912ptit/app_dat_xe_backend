package com.nhom15.app_dat_xe.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class FirebaseConfig {

    @Bean
    fun firebaseApp(): FirebaseApp {
        // Tránh lỗi "FirebaseApp name [DEFAULT] already exists" khi devtools restart
        FirebaseApp.getApps().firstOrNull()?.let { return it }

        val resource = ClassPathResource("firebase-service-account.json")
        check(resource.exists()) { "Không tìm thấy firebase-service-account.json trong resources" }

        val options = resource.inputStream.use { stream ->
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build()
        }
        return FirebaseApp.initializeApp(options)
    }

    @Bean
    fun firebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth = FirebaseAuth.getInstance(firebaseApp)
}