package com.nhom15.app_dat_xe.auth.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException
import java.io.InputStream

@Configuration
class FirebaseConfig {

    @Bean
    @Throws(IOException::class)
    fun firebaseApp(): FirebaseApp {

        val serviceAccount: InputStream? =
            javaClass.classLoader
                .getResourceAsStream("firebase-service-account.json")

        if (serviceAccount == null) {
            throw IOException("Không tìm thấy firebase-service-account.json")
        }

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        return FirebaseApp.initializeApp(options)
    }

    @Bean
    fun firebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth {
        return FirebaseAuth.getInstance(firebaseApp)
    }
}