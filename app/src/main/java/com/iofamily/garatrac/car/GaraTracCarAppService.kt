package com.iofamily.garatrac.car

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class GaraTracCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.Builder(applicationContext)
            .addAllowedHost("androidx.car.app", "template")
            .build()
    }


    override fun onCreateSession(): Session {
        return GaraTracSession()
    }
}

