package com.sakun.security_home.fragmentPage

import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import com.sakun.security_home.method.Communicator
import com.sakun.security_home.R
import com.sakun.security_home.method.`object`.UserLocalData
import com.sakun.security_home.databinding.FragmentMainPageBinding
import com.sakun.security_home.method.SecurityHomeServer
import com.sakun.security_home.method.securtiyHome_server.SecurityHomeStatus

class Main_page : Fragment(), View.OnClickListener {

    private lateinit var userLocalData: UserLocalData
    private lateinit var communicator: Communicator

    private var _binding: FragmentMainPageBinding? = null
    private val binding get() = _binding!!

    private var closeStatus = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainPageBinding.inflate(inflater, container, false)
        val view = binding.root
        communicator = activity as Communicator

        communicator.actionBarShow()

        userLocalData = communicator.getUserLocalData()!!

        binding.aboutUsBtn.setOnClickListener(this)
        binding.doorLockBtn.setOnClickListener(this)
        binding.powerUsedBtn.setOnClickListener(this)
        binding.roomControlBtn.setOnClickListener(this)
        binding.settingBtn.setOnClickListener(this)

        binding.doorLockBtn.setOnLongClickListener {
            openDoor()
            true
        }

        requireActivity().onBackPressedDispatcher.addCallback(this){
            closeActivity()
        }

        return view
    }

    override fun onClick(v: View?) {
        communicator.vibratePhone(10)
        val fragment = when(v?.id){
            R.id.aboutUs_btn -> R.id.action_main_page_to_aboutUs_page
            R.id.powerUsed_btn -> R.id.action_main_page_to_powerUsed_page
            R.id.roomControl_btn -> R.id.action_main_page_to_roomControl_page
            R.id.setting_btn -> R.id.action_main_page_to_setting_page
            R.id.doorLock_btn ->{
                Toast.makeText(v.context, "Please long click to unlock the door!", Toast.LENGTH_SHORT).show()
                null
            }
            else -> null
        }
        if(fragment != null){
            view.let {
                if (it != null) {
                    Navigation.findNavController(it).navigate(fragment)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(communicator.loginTimeout()){
            view.let {
                if(it != null){
                    Navigation.findNavController(it).navigate(R.id.action_main_page_to_fastLogin_page)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun openDoor(){
        view?.let {
            SecurityHomeServer(it.context).openMainDoor({ status, message ->
                when(status){
                    SecurityHomeStatus.Success.value -> {
                        binding.doorLockIv.setImageResource(R.drawable.ic_door_open)
                        Toast.makeText(it.context, "Door Unlock", Toast.LENGTH_SHORT).show()
                        communicator.vibratePhone(500)

                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.doorLockIv.setImageResource(R.drawable.ic_door_close)
                        }, 3000)
                    }
                    SecurityHomeStatus.Failed.value -> communicator.msgBox("Need setting!", "You need to set your main door!", false)
                }
            },{ error ->
                Toast.makeText(it.context, error, Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun closeActivity(){
        communicator.vibratePhone(50)
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
}