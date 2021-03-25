package com.anipen.androidauthenticationclient.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import com.anipen.androidauthenticationclient.R
import com.anipen.androidauthenticationclient.interfaces.ProgressDialogListener
import com.github.lzyzsd.circleprogress.DonutProgress

class SimpleProgressDialog(context: Context?) :
    Dialog(context!!, R.style.SimpleProgressDialog) {
    private var progressBar: View? = null
    private var progress = 0
    fun setProgress(activity: Activity?, progress: Int) {
        if (progressBar is ContentLoadingProgressBar) {
            if (progress != this.progress) {
                this.progress = progress
                UpdateAsyncTask().execute(progress)
            }
        }
    }

    private inner class UpdateAsyncTask : AsyncTask<Int?, Void?, Int>() {
        override fun doInBackground(vararg progress: Int?): Int {
            return progress[0]!!
        }

        override fun onPostExecute(result: Int) {
            (progressBar as ContentLoadingProgressBar?)!!.progress = progress
        }

    }

    companion object {
        private var progressListener: ProgressDialogListener? = null

        @JvmOverloads
        fun show(
            context: Context?,
            title: CharSequence?,
            indeterminate: Boolean = true,
            cancelable: Boolean = false,
            cancelListener: DialogInterface.OnCancelListener? = null
        ): SimpleProgressDialog? {
            return try {
                val dialog = SimpleProgressDialog(context)
                dialog.setTitle(title)
                dialog.setCancelable(cancelable)
                dialog.setOnCancelListener(cancelListener)
                if (indeterminate) {
                    dialog.progressBar = ProgressBar(context)
                } else {
                    val donutProgress = DonutProgress(context)
                    donutProgress.finishedStrokeColor = ContextCompat.getColor(
                        context!!,
                        R.color.colorAccent
                    )
                    donutProgress.textColor = ContextCompat.getColor(context, R.color.colorAccent)
                    dialog.progressBar = donutProgress
                }
                dialog.addContentView(
                    dialog.progressBar!!,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                )
                dialog.show()
                if (dialog.isShowing) {
                    progressListener?.onStatusFromProgress(dialog)
                }
                dialog
            } catch (e: Exception) {
                null
            }
        }

        @JvmStatic
        fun setListener(
            authProgressListener: ProgressDialogListener,
            context: Context?,
            title: CharSequence?
        ): SimpleProgressDialog? {
            this.progressListener = authProgressListener
            return show(context, title)
        }
    }
}