package com.anipen.androidauthenticationclient.viewbinders

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import com.android.volley.VolleyError
import com.anibear.andauthclientproject.AuthClientManager
import com.anibear.andauthclientproject.GuestLoginProcess
import com.anibear.andauthclientproject.LoginProcess
import com.anibear.andauthclientproject.PasswordLoginProcess
import com.anibear.andauthclientproject.interfaces.LoginProcessListener
import com.anibear.andauthclientproject.models.OAuth2Token
import com.anibear.andauthclientproject.models.UserInfo
import com.anibear.andauthclientproject.utils.AuthClientUtils
import com.anibear.andauthclientproject.utils.StringHelper
import com.anipen.androidauthenticationclient.MainActivity
import com.anipen.androidauthenticationclient.R
import com.anipen.androidauthenticationclient.fragmnets.SuccessfulFragment
import com.anipen.androidauthenticationclient.interfaces.ProgressDialogListener
import com.anipen.androidauthenticationclient.ui.SimpleProgressDialog
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_login.view.edit_password
import kotlinx.android.synthetic.main.fragment_login.view.txt_monitoring
import kotlinx.android.synthetic.main.fragment_new_successful.view.*
import net.openid.appauth.*
import org.jetbrains.annotations.NotNull
import java.util.concurrent.ExecutorService

/**
 * 로그인 뷰를 컨트롤합니다.
 */
class LoginViewBinder(@NotNull itemView: View) {
    private val context: Context = itemView.context
    private val loginBtn: AppCompatButton = itemView.btn_login
    private val userNameEdit: AppCompatEditText = itemView.edit_username
    private val passwordEdit: AppCompatEditText = itemView.edit_password
    private val passwordLoginBtn: AppCompatButton = itemView.btn_password_login
    private val loginStatus: AppCompatTextView = itemView.txt_login_status
    private var mExecutor: ExecutorService? = null
    var sharedPreferences: SharedPreferences? = null
    private var authorizationService: AuthorizationService
    private var authUserId: String? = null

    //Data view
    private val txtMoniter: AppCompatTextView = itemView.txt_monitoring

    init {
        sharedPreferences = AuthClientManager.setAuthStateSharedPreferences(context)
        mExecutor = (context as MainActivity).mExecutor
        context.isSuccessfulFragment = false
        authorizationService = AuthorizationService(this.context)
        //Initialize stored user ID
        AuthClientManager.clearAuthUserId(context)
        getGustLoginProcess()

        //Data View init setting
        txtMoniter.movementMethod = ScrollingMovementMethod()

        loginBtn.setOnClickListener {
            if (AuthClientManager.getGuestAccessToken(context) == null) {
                return@setOnClickListener
            }
            showProgressDialog()
            mExecutor?.execute {
                if (authUserId == null) {
                    dismissProgressDialog()
                    outputTextMonitor(" Guest User ID is Null")
                    return@execute
                }
                AuthClientManager.apply {
                    GuestLoginProcess.getGuestCode(
                        context,
                        getGuestAccessToken(context)!!,
                        authUserId!!,
                        getRandomPassword(context),
                        object : LoginProcessListener<OAuth2Token, VolleyError> {
                            override fun onResponse(t: OAuth2Token) {
                                t.apply {
                                    LoginProcess.apply {
                                        getProcess(
                                            context,
                                            guestCode,
                                            R.color.colorPrimary,
                                            HANDLE_LOGIN_RESPONSE,
                                            authorizationService
                                        )
                                    }
                                    dismissProgressDialog()
                                    outputTextMonitor("get guestCode : $guestCode, \nget expiresIn : $expiresIn \n")
                                }
                            }

                            override fun onError(error: VolleyError) {
                                dismissProgressDialog()
                                error.apply {
                                    outputTextMonitor("error : $this, \nstatus code : ${networkResponse.statusCode}")
                                }
                            }

                            override fun onNetworkResponse(statusCode: Int) {
                                dismissProgressDialog()
                                statusCode.let {
                                    outputTextMonitor("onNetworkResponse : $it")
                                }
                            }
                        }
                    )
                }
            }
        }
        passwordLoginBtn.setOnClickListener {
            if (userNameEdit.text.toString().toByteArray()
                    .isNotEmpty() && passwordEdit.text.toString().toByteArray().isNotEmpty()
            ) {
                showProgressDialog()
                mExecutor?.execute {
                    PasswordLoginProcess.getProcess(
                        context,
                        userNameEdit.text.toString(),
                        passwordEdit.text.toString(),
                        object : LoginProcessListener<OAuth2Token, VolleyError> {
                            override fun onResponse(t: OAuth2Token) {
                                dismissProgressDialog()
                                t.apply {
                                    dismissProgressDialog()
                                    Log.d(
                                        AuthClientUtils.getTag(context),
                                        "PasswordLoginProcess, \nget accessToke : $accessToken \n" +
                                                "get tokenType : $tokenType, \n" +
                                                "get expiresIn : $expiresIn, \n" +
                                                "get refreshToken : $refreshToken, \n" +
                                                "get idToken : $idToken \n"
                                    )
                                    val bundle = bundleOf(
                                        context.getString(R.string.key_accessToken) to accessToken,
                                        context.getString(R.string.key_refreshToken) to refreshToken,
                                        context.getString(R.string.key_idToken) to idToken,
                                        context.getString(R.string.key_accessTokenExpirationTime) to expiresIn.toString()
                                    )
                                    val successfulFragment =
                                        SuccessfulFragment.newInstance(
                                            context,
                                            this@LoginViewBinder,
                                            authorizationService
                                        )
                                    successfulFragment.arguments = bundle
                                    context.supportFragmentManager.beginTransaction()
                                        .replace(
                                            R.id.container,
                                            successfulFragment
                                        )
                                        .commit()
                                }
                            }

                            override fun onNetworkResponse(statusCode: Int) {
                                dismissProgressDialog()
                                outputTextMonitor("onNetworkResponse : $statusCode")
                            }

                            override fun onError(error: VolleyError) {
                                dismissProgressDialog()
                                error.apply {
                                    dismissProgressDialog()
                                    outputTextMonitor("error : $this , \nstatus code : ${networkResponse.statusCode}")
                                }
                            }
                        }
                    )
                }
            }
        }
        checkIntent(this.context.intent, this.context)
        showProgressDialog()
    }

    fun showProgressDialog() {
        dismissProgressDialog()
        (context as MainActivity).progressDialog =
            SimpleProgressDialog.setListener(object : ProgressDialogListener {
                override fun onStatusFromProgress(dialog: SimpleProgressDialog) {
                }
            }, context, null)
    }

    fun dismissProgressDialog() {
        if ((context as MainActivity).progressDialog != null) context.progressDialog!!.dismiss()
    }

    private fun checkIntent(@Nullable intent: Intent, context: Context) {
        when (intent.action) {
            LoginProcess.HANDLE_LOGIN_RESPONSE -> {
                val response = AuthorizationResponse.fromIntent(intent)
                val error = AuthorizationException.fromIntent(intent)
                val authState = AuthState(response, error)
                LoginProcess.handleAuthorizationResponse(
                    intent,
                    context,
                    authorizationService,
                    authState,
                    object : LoginProcessListener<TokenResponse, AuthorizationException> {
                        override fun onResponse(t: TokenResponse) {
                            t.apply {
                                AuthClientManager.persistAuthState(
                                    context,
                                    accessToken!!,
                                    refreshToken!!,
                                    idToken!!,
                                    accessTokenExpirationTime!!.toString()
                                )
                                Log.d(
                                    AuthClientUtils.getTag(context),
                                    "AuthStateResponse,\n" +
                                            "get accessToke : $accessToken \n" +
                                            "get tokenType : $tokenType, \n" +
                                            "get expiresIn : $accessTokenExpirationTime, \n" +
                                            "get refreshToken : $refreshToken, \n" +
                                            "get idToken : $idToken \n"
                                )
                                val bundle = bundleOf(
                                    context.getString(R.string.key_accessToken) to accessToken,
                                    context.getString(R.string.key_refreshToken) to refreshToken,
                                    context.getString(R.string.key_idToken) to idToken,
                                    context.getString(R.string.key_accessTokenExpirationTime) to accessTokenExpirationTime.toString()
                                )
                                val successfulFragment =
                                    SuccessfulFragment.newInstance(
                                        context,
                                        this@LoginViewBinder,
                                        authorizationService
                                    )
                                successfulFragment.arguments = bundle
                                (context as MainActivity).supportFragmentManager.beginTransaction()
                                    .replace(
                                        R.id.container,
                                        successfulFragment
                                    )
                                    .commit()
                            }
                        }

                        override fun onNetworkResponse(statusCode: Int) {
                            outputTextMonitor("onNetworkResponse : $statusCode")
                        }

                        override fun onError(error: AuthorizationException) {
                            outputTextMonitor("Token Exchange failed : , $error")
                        }
                    })
            }
            LoginProcess.HANDLE_RESIGN_RESPONSE -> {
                Log.d(AuthClientUtils.getTag(context), "Intent DATA : ${intent.data}")
                outputTextMonitor("Unsubscribe is Success")
            }
            LoginProcess.HANDLE_USERINFO_RESPONSE -> {
                Log.d(AuthClientUtils.getTag(context), "Intent DATA : ${intent.data}")
                val bundle = bundleOf(
                    context.getString(R.string.key_accessToken) to AuthClientManager.getAuthAccessToken(
                        context
                    ),
                    context.getString(R.string.key_refreshToken) to AuthClientManager.getAuthRefreshToken(
                        context
                    ),
                    context.getString(R.string.key_idToken) to AuthClientManager.getAuthIdToken(
                        context
                    ),
                    context.getString(R.string.key_accessTokenExpirationTime) to AuthClientManager.getAuthAccessTokenExpirationTime(
                        context
                    )
                )
                val successfulFragment =
                    SuccessfulFragment.newInstance(
                        context,
                        this@LoginViewBinder,
                        authorizationService
                    )
                successfulFragment.arguments = bundle
                (context as MainActivity).supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.container,
                        successfulFragment
                    )
                    .commit()
            }
            else -> {
            }
        }
    }

    fun signOutProcess(context: Context) {
        AuthClientManager.apply {
            clearAuthState(context)
            clearAuthUserId(context)
        }
    }

    private fun getGustLoginProcess() {
        AuthClientManager.apply {
            GuestLoginProcess.getProcess(
                context,
                object : LoginProcessListener<OAuth2Token, VolleyError> {
                    override fun onResponse(t: OAuth2Token) {
                        dismissProgressDialog()
                        t.apply {
                            persistGuestAuthState(
                                context,
                                accessToken,
                                refreshToken
                            )
                            loginStatus.text = ("GUEST")
                            startSetUserIdForGuestAccessToken(context, accessToken)
                            outputTextMonitor("accessToken : ${accessToken},\nrefreshToken : $refreshToken")
                        }
                    }

                    override fun onError(error: VolleyError) {
                        dismissProgressDialog()
                        error.apply {
                            loginStatus.text = ("NULL")
                            outputTextMonitor("error : $error , \nstatus code : ${networkResponse.statusCode}")
                        }
                    }

                    override fun onNetworkResponse(statusCode: Int) {
                        dismissProgressDialog()
                        statusCode.let {
                            outputTextMonitor("onNetworkResponse : $it")
                        }
                    }
                })
        }
    }

    private fun startSetUserIdForGuestAccessToken(context: Context, token: String) {
        AuthClientManager.apply {
            getUserInfo(
                context,
                token,
                object : LoginProcessListener<UserInfo, VolleyError> {
                    override fun onResponse(t: UserInfo) {
                        authUserId = t.sub
                        outputTextMonitor("Guest User ID->$authUserId")
                    }

                    override fun onNetworkResponse(statusCode: Int) {
                        outputTextMonitor("onResponse : $statusCode")
                    }

                    override fun onError(error: VolleyError) {
                        outputTextMonitor("onResponse : $error")
                    }
                })
        }
    }

    private fun outputTextMonitor(str: String) {
        val value = ("Response : \n$str")
        txtMoniter.text = value
        Log.d(AuthClientUtils.getTag(context), value)
        txtMoniter.scrollTo(0, 0)
    }
}