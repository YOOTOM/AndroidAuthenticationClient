package com.anipen.androidauthenticationclient.interfaces

import com.anipen.androidauthenticationclient.ui.SimpleProgressDialog

interface ProgressDialogListener {

    fun onStatusFromProgress(dialog: SimpleProgressDialog)
}