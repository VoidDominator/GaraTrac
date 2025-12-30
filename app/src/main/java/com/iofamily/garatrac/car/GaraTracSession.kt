package com.iofamily.garatrac.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class GaraTracSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return CarMapScreen(carContext)
    }
}

