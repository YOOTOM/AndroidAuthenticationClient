package com.anipen.androidauthenticationclient.fragmnets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anipen.androidauthenticationclient.R
import com.anipen.androidauthenticationclient.viewbinders.LoginViewBinder
import com.anipen.androidauthenticationclient.viewbinders.SuccessfulViewBinder
import net.openid.appauth.AuthorizationService

class SuccessfulFragment : Fragment() {
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var idToken: String
    private lateinit var accessTokenExpirationTime: String

    companion object {
        private lateinit var loginViewBinder: LoginViewBinder
        private lateinit var context: Context
        private lateinit var authorizationService: AuthorizationService
        fun newInstance(
            context: Context,
            loginViewBinder: LoginViewBinder,
            authorizationService: AuthorizationService
        ): SuccessfulFragment {
            this.loginViewBinder = loginViewBinder
            this.context = context
            this.authorizationService = authorizationService
            return SuccessfulFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_new_successful, container, false)
        accessToken = arguments?.getString(this.context?.getString(R.string.key_accessToken))!!
        refreshToken = arguments?.getString(this.context?.getString(R.string.key_refreshToken))!!
        idToken = arguments?.getString(this.context?.getString(R.string.key_idToken))!!
        accessTokenExpirationTime =
            arguments?.getString(this.context?.getString(R.string.key_accessTokenExpirationTime))!!
        SuccessfulViewBinder(
            view,
            accessToken,
            refreshToken,
            idToken,
            accessTokenExpirationTime,
            loginViewBinder,
            authorizationService
        )
        return view
    }


}
