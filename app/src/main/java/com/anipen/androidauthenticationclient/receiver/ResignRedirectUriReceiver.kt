package com.anipen.androidauthenticationclient.receiver

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anibear.andauthclientproject.LoginProcess
import com.anipen.androidauthenticationclient.MainActivity

class ResignRedirectUriReceiver : AppCompatActivity() {

    override fun onCreate(savedInstanceBundle: Bundle?) {
        super.onCreate(savedInstanceBundle)

        startActivity(createResponseHandlingIntent(this, intent.data!!))
        finish()
    }
    private fun createResponseHandlingIntent(
        context: Context,
        responseUri: Uri
    ): Intent? {
        val intent: Intent = createBaseIntent(context)
        intent.action = LoginProcess.HANDLE_RESIGN_RESPONSE
        intent.data = responseUri
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
    private fun createBaseIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java)
    }

}