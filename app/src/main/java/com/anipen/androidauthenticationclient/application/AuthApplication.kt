package com.anipen.androidauthenticationclient.application

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.anibear.andauthclientproject.AuthClientManager
import java.util.*

class AuthApplication : Application() {
    private lateinit var context: Context
    private lateinit var authCM: AuthClientManager.Companion
    override fun onCreate() {
        super.onCreate()
        context = this
        authCM = AuthClientManager.Companion
        authCM.apply {
            setLocales(context, Locale.getDefault().language)
            setBaseUrl(context, "")
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        newConfig.locales[0].apply {
            authCM.setLocales(context, this.toString())
        }
    }
}