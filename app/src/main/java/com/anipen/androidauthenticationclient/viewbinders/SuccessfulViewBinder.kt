package com.anipen.androidauthenticationclient.viewbinders

import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.android.volley.VolleyError
import com.anibear.andauthclientproject.AuthClientManager
import com.anibear.andauthclientproject.interfaces.LoginProcessListener
import com.anibear.andauthclientproject.models.OAuth2Token
import com.anibear.andauthclientproject.models.UserInfo
import com.anibear.andauthclientproject.utils.AuthClientUtils
import com.anibear.andauthclientproject.utils.StringHelper
import com.anipen.androidauthenticationclient.MainActivity
import com.anipen.androidauthenticationclient.R
import kotlinx.android.synthetic.main.fragment_new_successful.view.*
import net.openid.appauth.AuthorizationService
import org.jetbrains.annotations.NotNull
import java.util.concurrent.ExecutorService

/**
 * 로그인 성공뷰를 컨트롤합니다.
 */
class SuccessfulViewBinder(
    @NotNull itemView: View,
    private var accessToken: String,
    private var refreshToken: String,
    private var idToken: String,
    private var accessTokenExpirationTime: String,
    private val loginViewBinder: LoginViewBinder,
    private val authorizationService: AuthorizationService
) {
    private val context: Context = itemView.context
    private var mExecutor: ExecutorService? = null

    //Data view
    private val txtMoniter: AppCompatTextView = itemView.txt_monitoring

    //Edit data
    private val editPassword: AppCompatEditText = itemView.edit_password
    private val editNickname: AppCompatEditText = itemView.edit_nickname

    //Control data
    private val btnRevokeToken: AppCompatButton = itemView.btn_revoke_token
    private val btnRefreshToken: AppCompatButton = itemView.btn_refresh_token
    private val btnUserInfo: AppCompatButton = itemView.btn_user_info
    private val btnChangePassword: AppCompatButton = itemView.btn_change_password
    private val btnCheckPassword: AppCompatButton = itemView.btn_check_password
    private val btnCheckNickname: AppCompatButton = itemView.btn_check_nickname
    private val btnChangeNickname: AppCompatButton = itemView.btn_change_nickname
    private val btnRequestUserProfile: AppCompatButton = itemView.btn_request_user_profile
    private val btnRequestUploadURL: AppCompatButton = itemView.btn_request_upload_url_user_profile
    private val btnCallCompleteUploadUserProfile: AppCompatButton =
        itemView.btn_call_complete_upload_user_profile
    private val btnDeleteUserProfile: AppCompatButton = itemView.btn_delete_user_profile
    private val btnGetAccessCode: AppCompatButton =
        itemView.btn_get_access_code
    private val btnMembershipUnsubscribe: AppCompatButton = itemView.btn_membership_unsubscribe

    private val scrollViewMain: ScrollView = itemView.scrollView_main

    private var accessCode: String = StringHelper.EMPTY

    init {
        if (context is MainActivity) {
            context.isSuccessfulFragment = true
            mExecutor = context.mExecutor
        }
        btnRevokeToken.setOnClickListener { btnController(it) }
        btnRefreshToken.setOnClickListener { btnController(it) }
        btnUserInfo.setOnClickListener { btnController(it) }
        btnChangePassword.setOnClickListener { btnController(it) }
        btnCheckPassword.setOnClickListener { btnController(it) }
        btnChangeNickname.setOnClickListener { btnController(it) }
        btnCheckNickname.setOnClickListener { btnController(it) }
        btnRequestUserProfile.setOnClickListener { btnController(it) }
        btnRequestUploadURL.setOnClickListener { btnController(it) }
        btnCallCompleteUploadUserProfile.setOnClickListener { btnController(it) }
        btnDeleteUserProfile.setOnClickListener { btnController(it) }
        btnGetAccessCode.setOnClickListener { btnController(it) }
        btnMembershipUnsubscribe.setOnClickListener { btnController(it) }

        txtMoniter.movementMethod = ScrollingMovementMethod()
        txtMoniter.setTextIsSelectable(true)
        txtMoniter.apply {
            this.text = ("Login Success")
            //text Copy
            setOnLongClickListener {
                setClipboardCopied(this)
                return@setOnLongClickListener true
            }
            setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> scrollViewMain.requestDisallowInterceptTouchEvent(
                            true
                        )
                    }
                    return false
                }
            })

        }
    }

    private fun btnController(view: View) {
        loginViewBinder.showProgressDialog()
        mExecutor?.execute {
            when (view) {
                btnRevokeToken -> {
                    AuthClientManager.getRevokeToken(
                        context,
                        accessToken,
                        object : LoginProcessListener<String, VolleyError> {
                            override fun onResponse(t: String) {
                                loginViewBinder.dismissProgressDialog()
                                outputTextMonitor("Result -> $t")
                                logOutButtonClick(loginViewBinder, context)
                            }

                            override fun onNetworkResponse(statusCode: Int) {
                                loginViewBinder.dismissProgressDialog()
                                getStatusCode(statusCode)
                            }

                            override fun onError(error: VolleyError) {
                                loginViewBinder.dismissProgressDialog()
                                getStatusCode(error.networkResponse.statusCode)
                            }
                        })
                }

                btnRefreshToken -> {
                    AuthClientManager.apply {
                        getRefreshToken(
                            context,
                            refreshToken,
                            object : LoginProcessListener<OAuth2Token, VolleyError> {
                                override fun onResponse(t: OAuth2Token) {
                                    loginViewBinder.dismissProgressDialog()
                                    accessToken = t.accessToken
                                    accessTokenExpirationTime = t.expiresIn.toString()
                                    refreshToken = t.refreshToken
                                    idToken = t.idToken
                                    clearAuthState(context)
                                    persistAuthState(
                                        context,
                                        accessToken,
                                        refreshToken,
                                        idToken,
                                        accessTokenExpirationTime
                                    )
                                    val result =
                                        "Access_token : ${accessToken}" +
                                                "\nToken_type : ${t.tokenType}" +
                                                "\nExpiresIng : ${accessTokenExpirationTime}" +
                                                "\nRefresh_token : ${refreshToken}" +
                                                "\nId_token : ${idToken}\n"
                                    outputTextMonitor(result)
                                }

                                override fun onNetworkResponse(statusCode: Int) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(statusCode)
                                }

                                override fun onError(error: VolleyError) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(error.networkResponse.statusCode)
                                }
                            })
                    }
                }

                btnUserInfo -> {
                    AuthClientManager.apply {
                        getUserInfo(
                            context,
                            idToken,
                            object : LoginProcessListener<UserInfo, VolleyError> {
                                override fun onResponse(t: UserInfo) {
                                    loginViewBinder.dismissProgressDialog()
                                    var addStr: String = StringHelper.EMPTY
                                    t.scope.forEach { str ->
                                        addStr += "$str/"
                                    }
                                    val result =
                                        "sub : ${t.sub}\n" +
                                                "aud : ${t.aud}\n" +
                                                "scope : ${addStr}\n" +
                                                "getGuestCodeiss : ${t.iss}\n" +
                                                "nickname : ${t.nickname}\n" +
                                                "rid : ${t.rid}\n" +
                                                "exp : ${t.exp}\n" +
                                                "userId : ${t.userId}\n" +
                                                "iat : ${t.iat}\n" +
                                                "jti : ${t.jti}"
                                    outputTextMonitor(result)
                                    setAuthUserId(context, t.sub)
                                }

                                override fun onNetworkResponse(statusCode: Int) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(statusCode)
                                }

                                override fun onError(error: VolleyError) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(error.networkResponse.statusCode)
                                }
                            })
                    }
                }

                btnChangePassword -> {
                    if (this.accessCode != StringHelper.EMPTY) {
                        AuthClientManager.apply {
                            callPasswordChangePage(context, authorizationService, accessCode,R.color.colorPrimary)
                        }
                        this.accessCode = StringHelper.EMPTY
                    } else {
                        outputTextMonitor("accessCode is null")
                        loginViewBinder.dismissProgressDialog()
                    }
                }

                btnCheckPassword -> {
                    if (editPassword.text.toString() == StringHelper.EMPTY) {
                        loginViewBinder.dismissProgressDialog()
                        outputTextMonitor("Edit text is null")
                        return@execute
                    }
                    AuthClientManager.apply {
                        setCheckPassword(
                            context,
                            accessToken,
                            "me",
                            editPassword.text.toString(),
                            object : LoginProcessListener<String, VolleyError> {
                                override fun onResponse(t: String) {
                                    loginViewBinder.dismissProgressDialog()
                                    // outputTextMonitor("Result -> $t")
                                }

                                override fun onNetworkResponse(statusCode: Int) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(statusCode)
                                }

                                override fun onError(error: VolleyError) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(error.networkResponse.statusCode)
                                }
                            })
                    }

                }
                btnChangeNickname -> {
                    if (accessCode != StringHelper.EMPTY) {
                        AuthClientManager.apply {
                            callNicknameChangePage(context, authorizationService, accessCode, R.color.colorPrimary)
                        }
                        //accessCode = StringHelper.EMPTY
                    } else {
                        outputTextMonitor("accessCode is null")
                        loginViewBinder.dismissProgressDialog()
                    }
                }

                btnCheckNickname -> {
                    if (editNickname.text.toString() == StringHelper.EMPTY) {
                        loginViewBinder.dismissProgressDialog()
                        outputTextMonitor("Edit text is null")
                        return@execute
                    }
                    AuthClientManager.setCheckNickname(
                        context,
                        accessToken,
                        editNickname.text.toString(),
                        object : LoginProcessListener<String, VolleyError> {
                            override fun onResponse(t: String) {
                                loginViewBinder.dismissProgressDialog()
                                //outputTextMonitor("Result -> $t")
                            }

                            override fun onNetworkResponse(statusCode: Int) {
                                loginViewBinder.dismissProgressDialog()
                                getStatusCode(statusCode)
                            }

                            override fun onError(error: VolleyError) {
                                loginViewBinder.dismissProgressDialog()
                                getStatusCode(error.networkResponse.statusCode)
                            }
                        })
                }

                btnRequestUserProfile -> {
                    AuthClientManager.apply {
                        if (getAuthUserId(context) == null) {
                            loginViewBinder.dismissProgressDialog()
                            outputTextMonitor("UserID is null")
                            return@execute
                        }
                        getRequestUserProfile(
                            context,
                            accessToken,
                            getAuthUserId(context)!!,
                            object : LoginProcessListener<UserInfo, VolleyError> {
                                override fun onResponse(t: UserInfo) {
                                    loginViewBinder.dismissProgressDialog()
                                    val result =
                                        "userId : ${t.userId}\n" +
                                                "nickname : ${t.nickname}\n" +
                                                "email : ${t.email}\n" +
                                                "regDate : ${t.regDate}\n" +
                                                "lang : ${t.lang}\n" +
                                                "level: ${t.level}\n" +
                                                "authorized : ${t.authorized}\n"
                                    outputTextMonitor(result)
                                }

                                override fun onNetworkResponse(statusCode: Int) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(statusCode)
                                }

                                override fun onError(error: VolleyError) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(error.networkResponse.statusCode)
                                }
                            })
                    }
                }

                btnRequestUploadURL -> {
                    AuthClientManager.apply {
                        if (getAuthUserId(context) == null) {
                            loginViewBinder.dismissProgressDialog()
                            outputTextMonitor("UserID is null")
                            return@execute
                        }
                        getRequestUserProfileUploadURL(
                            context,
                            accessToken,
                            getAuthUserId(context)!!,
                            object : LoginProcessListener<UserInfo, VolleyError> {
                                override fun onResponse(t: UserInfo) {
                                    loginViewBinder.dismissProgressDialog()
                                    val result =
                                        "userId : ${t.userId}\n" +
                                                "url : ${t.url}\n" +
                                                "expireAt : ${t.expireAt}\n"
                                    outputTextMonitor(result)
                                }

                                override fun onNetworkResponse(statusCode: Int) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(statusCode)
                                }

                                override fun onError(error: VolleyError) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(error.networkResponse.statusCode)
                                }
                            })
                    }
                }

                btnCallCompleteUploadUserProfile -> {
                    loginViewBinder.dismissProgressDialog()
                    /* AuthClientManager.apply {
                         if (getAuthUserId(context) == null) {
                             loginViewBinder.dismissProgressDialog()
                             outputTextMonitor("UserID is null")
                             return@execute
                         }
                         setCallCompleteUploadUserProfile(
                             context,
                             accessToken,
                             getAuthUserId(context)!!,
                             object : LoginProcessListener<String, VolleyError> {
                                 override fun onResponse(t: String) {
                                     loginViewBinder.dismissProgressDialog()
                                     outputTextMonitor("Result -> $t")
                                 }

                                 override fun onNetworkResponse(statusCode: Int) {
                                     loginViewBinder.dismissProgressDialog()
                                     getStatusCode(statusCode)
                                 }

                                 override fun onError(error: VolleyError) {
                                     loginViewBinder.dismissProgressDialog()
                                     getStatusCode(error.networkResponse.statusCode)
                                 }
                             })
                     }*/

                }

                btnDeleteUserProfile -> {
                    AuthClientManager.apply {
                        if (getAuthUserId(context) == null) {
                            loginViewBinder.dismissProgressDialog()
                            outputTextMonitor("UserID is null")
                            return@execute
                        }
                        setDeleteUserProfile(
                            context,
                            accessToken,
                            getAuthUserId(context)!!,
                            object : LoginProcessListener<String, VolleyError> {
                                override fun onResponse(t: String) {
                                    loginViewBinder.dismissProgressDialog()
                                    //outputTextMonitor("Result -> $t")
                                }

                                override fun onNetworkResponse(statusCode: Int) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(statusCode)
                                }

                                override fun onError(error: VolleyError) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(error.networkResponse.statusCode)
                                }
                            })
                    }

                }

                btnGetAccessCode -> {
                    AuthClientManager.apply {
                        if (getAuthUserId(context) == null) {
                            loginViewBinder.dismissProgressDialog()
                            outputTextMonitor("UserID is null")
                            return@execute
                        }
                        getAccessCode(context, accessToken, getAuthUserId(context)!!,
                            object : LoginProcessListener<UserInfo, VolleyError> {
                                override fun onResponse(t: UserInfo) {
                                    loginViewBinder.dismissProgressDialog()
                                    val result =
                                        "guestCode : ${t.guestCode}\n" +
                                                "expiresIn : ${t.expiresIn}\n"
                                    accessCode = t.guestCode
                                    outputTextMonitor(result)
                                }

                                override fun onNetworkResponse(statusCode: Int) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(statusCode)
                                }

                                override fun onError(error: VolleyError) {
                                    loginViewBinder.dismissProgressDialog()
                                    getStatusCode(error.networkResponse.statusCode)
                                }
                            })
                    }

                }

                btnMembershipUnsubscribe -> {
                    if (this.accessCode != StringHelper.EMPTY) {
                        AuthClientManager.callMembershipUnsubscribePage(
                            context,
                            authorizationService,
                            accessCode,
                            R.color.colorPrimary
                        )
                        this.accessCode = StringHelper.EMPTY
                    } else {
                        outputTextMonitor("accessCode is null")
                        loginViewBinder.dismissProgressDialog()
                    }
                }
            }
        }
    }

    private fun setClipboardCopied(textView: AppCompatTextView) {
        val clipboard = this.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val getString: String = textView.text.toString()
        val clip = ClipData.newPlainText("text", getString)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this.context, "Successfully copied.", Toast.LENGTH_LONG).show()
    }

    private fun logOutButtonClick(loginViewBinder: LoginViewBinder, context: Context) {
        loginViewBinder.signOutProcess(context)
        if (context is MainActivity) {
            context.onBackPressed()
        }
    }

    private fun outputTextMonitor(str: String) {
        val value = ("Response : \n$str")
        txtMoniter.text = value
        Log.d(AuthClientUtils.getTag(context), value)
        txtMoniter.scrollTo(0, 0)
    }

    private fun getStatusCode(code: Int) {
        when (code) {
            200 -> outputTextMonitor("Available nickname.")
            401 -> outputTextMonitor("Token is not valid.")
            403 -> outputTextMonitor("The token is valid, but blocked on the server.")
            404 -> outputTextMonitor("page not found.")
            409 -> outputTextMonitor("This is a nickname that already exists.")
            500 -> outputTextMonitor("Server error")
            else -> outputTextMonitor("statusCode : $code")
        }
    }
}