package com.anipen.androidauthenticationclient.fragmnets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anipen.androidauthenticationclient.R
import com.anipen.androidauthenticationclient.viewbinders.LoginViewBinder

class LoginFragment : Fragment() {
    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        LoginViewBinder(view)
        return view
    }
}
