package com.sakun.security_home.fragmentPage

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.sakun.security_home.R
import com.sakun.security_home.method.*
import com.sakun.security_home.method.`object`.UserLocalData
import com.sakun.security_home.method.securtiyHome_server.Internet
import com.sakun.security_home.method.securtiyHome_server.SecurityHomeDeviceLoginStatus
import com.sakun.security_home.method.securtiyHome_server.SecurityHomeStatus

class Hello_page : Fragment() {

    private lateinit var communicator: Communicator
    private lateinit var userLocalData: UserLocalData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hello_page, container, false)

        communicator = activity as Communicator
        userLocalData = communicator.getUserLocalData()!!

        Handler(Looper.getMainLooper()).postDelayed({
            checkStatus()
        }, 1000)

        return view
    }

    private fun checkStatus(){
        view?.let {
            SecurityHomeServer(it.context).checkVersion({ status, message ->
                when(status){
                    Internet.NoInternet.value -> communicator.msgBox("Internet", message, true)
                    SecurityHomeStatus.Failed.value -> communicator.msgBox("Check version failed", message, true)
                    SecurityHomeStatus.Success.value -> {

                        deviceLogin()
                    }
                    SecurityHomeStatus.Update.value -> {
                        communicator.msgBox("Version update", message, false)
                        deviceLogin()
                    }
                    else -> communicator.msgBox("Hack?", "Did you hack my app? :(", true)
                }
            },{ error ->
                communicator.msgBox("Internet", error, true)
            })
        }
    }

    private fun deviceLogin(){
        if (userLocalData.accountToken == ""){
            view?.let { Navigation.findNavController(it).navigate(R.id.action_hello_page_to_login_page) }
        }
        else{
            view?.let {
                SecurityHomeServer(it.context)
                    .deviceLogin(userLocalData.accountToken, userLocalData.androidId, { status, message ->
                        when(status){
                            Internet.NoInternet.value -> communicator.msgBox("Internet", message, true)
                            SecurityHomeDeviceLoginStatus.NewDevice.value -> communicator.msgBox("New device", message, false)
                            SecurityHomeDeviceLoginStatus.Success.value -> Navigation.findNavController(it).navigate(R.id.action_hello_page_to_fastLogin_page)
                        }
                     }, { error ->
                        Toast.makeText(it.context, error, Toast.LENGTH_LONG).show()
                     })
            }
        }
    }
}