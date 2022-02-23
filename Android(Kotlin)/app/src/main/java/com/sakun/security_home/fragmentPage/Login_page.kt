package com.sakun.security_home.fragmentPage

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.sakun.security_home.R
import com.sakun.security_home.databinding.FragmentLoginPageBinding
import com.sakun.security_home.method.Communicator
import com.sakun.security_home.method.SecurityHomeServer
import com.sakun.security_home.method.securtiyHome_server.Encryption
import com.sakun.security_home.method.securtiyHome_server.Internet
import com.sakun.security_home.method.securtiyHome_server.SecurityHomeDeviceLoginStatus
import com.sakun.security_home.method.securtiyHome_server.SecurityHomeLoginStatus

class Login_page : Fragment(), View.OnClickListener {

    private var _binding: FragmentLoginPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var communicator: Communicator
    private var closeStatus = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginPageBinding.inflate(inflater, container, false)
        val view = binding.root
        communicator = activity as Communicator

        binding.loginBtn.setOnClickListener(this)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            closeActivity()
        }

        return view
    }

    private fun login(){
        binding.loginBtn.isEnabled = false
        binding.waitingBar.visibility = View.VISIBLE
        val thread = Thread{
            view?.let {
                SecurityHomeServer(it.context)
                    .login(binding.userIdBar.text.toString(), binding.userPasswordBar.text.toString(),{ status, message ->
                        when(status){
                            Internet.NoInternet.value -> Toast.makeText(it.context, "Please check your Internet network!", Toast.LENGTH_SHORT).show()
                            SecurityHomeLoginStatus.NoAccount.value -> {
                                binding.userIdBar.error = "Can't find your Security_home account!"
                                binding.userIdBar.requestFocus()
                                Toast.makeText(it.context, message, Toast.LENGTH_SHORT).show()
                            }
                            SecurityHomeLoginStatus.PasswordNotCorrect.value -> {
                                binding.userPasswordBar.error = "Password incorrect!"
                                Toast.makeText(it.context, message, Toast.LENGTH_SHORT).show()
                            }
                            SecurityHomeLoginStatus.Success.value -> {
                                val userLocalData = communicator.getUserLocalData()
                                userLocalData?.accountToken = Encryption().unEncryption(message)
                                communicator.updateUserLocalData(userLocalData!!)

                                deviceLogin(userLocalData.accountToken, userLocalData.androidId)
                            }
                        }
                        binding.loginBtn.isEnabled = true
                        binding.waitingBar.visibility = View.INVISIBLE
                    },{ error ->
                        Toast.makeText(it.context, error, Toast.LENGTH_LONG).show()
                    })
            }
        }
        thread.run()
    }

    private fun deviceLogin(accountToken: String, deviceId: String){
        view?.let {
            SecurityHomeServer(it.context)
                .deviceLogin(accountToken, deviceId, { status, message ->
                    when(status){
                        Internet.NoInternet.value -> Toast.makeText(it.context, "Please check your Internet network!", Toast.LENGTH_SHORT).show()
                        SecurityHomeDeviceLoginStatus.NewDevice.value -> communicator.msgBox("New device", message, false)
                        SecurityHomeDeviceLoginStatus.Success.value -> Navigation.findNavController(it).navigate(R.id.action_login_page_to_fastLogin_page)
                    }
                }, { error ->
                    Toast.makeText(it.context, error, Toast.LENGTH_LONG).show()
                })
        }
    }

    private fun checkData(): Boolean{
        var flag = true
        if(binding.userPasswordBar.text.length < 3){
            binding.userPasswordBar.error = "Please Enter Password"
            binding.userPasswordBar.requestFocus()
            flag = false
        }
        if(binding.userIdBar.text.length < 3) {
            binding.userIdBar.error = "Please Enter User Id"
            binding.userIdBar.requestFocus()
            flag = false
        }

        return flag
    }

    private fun closeActivity(){
        if(closeStatus){
            activity?.finish()
        }
        else{
            closeStatus = true
            Handler(Looper.getMainLooper()).postDelayed({
                closeStatus = false
            }, 1000)
            Toast.makeText(view?.context, "Press one more time to close!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        communicator.vibratePhone(50)
        if (v != null) {
            when(v.id){
                R.id.login_btn -> {
                    if(checkData()){
                        login()
                    }
                }
            }
        }
    }
}