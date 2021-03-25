package com.anipen.androidauthenticationclient

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.anipen.androidauthenticationclient.fragmnets.LoginFragment
import com.anipen.androidauthenticationclient.ui.SimpleProgressDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    internal var isSuccessfulFragment: Boolean = false
    internal var authorizationService: AuthorizationService? = null
    internal var progressDialog: SimpleProgressDialog? = null
    internal var mExecutor: ExecutorService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        mExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onResume() {
        super.onResume()
        dismissProgressDialog()
        if (!isSuccessfulFragment) {
            //권한 체크
            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.INTERNET
                    , Manifest.permission.READ_PHONE_STATE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            if (report.areAllPermissionsGranted()) {
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.container, LoginFragment.newInstance())
                                    .commitNow()
                            } else {
                                if (!isSuccessfulFragment) {
                                    finish()
                                }
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                })
                .withErrorListener {
                    Log.e(MainActivity::class.java.simpleName, "error: $it")
                }
                .check()
        }
    }

    override fun onBackPressed() {
        CoroutineScope(Dispatchers.Main).launch {
            backEvent()
        }
    }

    private fun backEvent() {
        if (!isSuccessfulFragment) {
            finish()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LoginFragment.newInstance())
                .commitNow()
        }
    }

    private fun showProgressDialog() {
        dismissProgressDialog()
        progressDialog = SimpleProgressDialog.show(this, null)
    }

    private fun dismissProgressDialog() {
        if (progressDialog != null) progressDialog!!.dismiss()
    }

    override fun onStart() {
        super.onStart()
        if (mExecutor != null) {
            if (mExecutor!!.isShutdown) {
                mExecutor = Executors.newSingleThreadExecutor()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mExecutor?.shutdownNow()
        authorizationService?.dispose()
    }
}
