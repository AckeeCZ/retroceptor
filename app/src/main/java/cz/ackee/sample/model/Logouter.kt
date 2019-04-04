package cz.ackee.sample.model

import android.content.Context
import android.content.Intent
import cz.ackee.sample.login.LoginActivity

/**
 * Logouter that performs all steps for logout
 */
class Logouter(val app: Context) {

    fun logout() {
        app.startActivity(Intent(app, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }
}