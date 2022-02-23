package com.sakun.security_home.fragmentPage

import android.Manifest
import android.content.pm.PackageManager
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sakun.security_home.R
import com.sakun.security_home.databinding.FragmentModePageBinding
import com.sakun.security_home.databinding.FragmentOutDoorModePageBinding
import com.sakun.security_home.method.Communicator
import com.sakun.security_home.method.SecurityHomeServer
import com.sakun.security_home.method.securtiyHome_server.SecurityHomeStatus
import io.fotoapparat.Fotoapparat
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import io.fotoapparat.selector.front
import java.io.File
import java.util.*
import java.util.concurrent.Executor
import kotlin.concurrent.schedule

class OutDoorMode_page : Fragment(){

    private var _binding: FragmentOutDoorModePageBinding? = null
    private val binding get() = _binding!!

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var communicator: Communicator
    private lateinit var fotoapparat: Fotoapparat

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentOutDoorModePageBinding.inflate(inflater, container, false)
        val view = binding.root

        communicator = activity as Communicator

        communicator.actionBarHide()

        Handler(Looper.getMainLooper()).postDelayed({
            createFotoapparat()
            while(communicator.hasNoPermissions()){
                communicator.requestPermission()
            }
            fotoapparat.start()

            Timer().schedule(1000, 5000){
                takePhoto()
            }
        }, 100)

        executor = ContextCompat.getMainExecutor(view.context)
        biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int,
                                                       errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)

                        if(errorCode != 10 && errorCode != 13) {
                            Toast.makeText(view.context, "$errorCode $errString", Toast.LENGTH_SHORT)
                                    .show()
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        openDoor()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                })

        binding.doorOpenBtn.setOnClickListener{
            useBiometricLogin()
        }

        binding.doorRingBtn.setOnClickListener{
            doorRing()
        }

        return view
    }

    private fun doorRing(){
        view?.let {
            SecurityHomeServer(it.context).doorRing({ status, message ->
                when(status){
                    SecurityHomeStatus.Success.value -> {
                        binding.ringImg.setImageResource(R.drawable.ic_ring_on)
                        Toast.makeText(it.context, "Ring Success", Toast.LENGTH_SHORT).show()
                        communicator.vibratePhone(500)

                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.ringImg.setImageResource(R.drawable.ic_ring_off)
                        }, 3000)
                    }
                    SecurityHomeStatus.Failed.value -> communicator.msgBox("Need setting!", "You need to set your main door!", false)
                }
            },{ error ->
                Toast.makeText(it.context, error, Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun useBiometricLogin() {
        if(communicator.getUserLocalData()!!.biometricStatus) {
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for DoorLock")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("Use fast password?")
                    .build()
            biometricPrompt.authenticate(promptInfo)
        }
        else{
            communicator.msgBox("Need Set Up", "You need to set up biometric login!", false)
        }
    }

    private fun takePhoto() {
        val filename = "test"
        val sd = Environment.DIRECTORY_DCIM
        val dest = File(sd, filename)
        if (communicator.hasNoPermissions()) {
            communicator.requestPermission()
        }else{
            fotoapparat.takePicture().saveToFile(dest)
        }
    }

    private fun openDoor(){
        view?.let {
            SecurityHomeServer(it.context).openMainDoor({ status, message ->
                when(status){
                    SecurityHomeStatus.Success.value -> {
                        binding.doorLockImg.setImageResource(R.drawable.ic_door_open)
                        Toast.makeText(it.context, "Door Unlock", Toast.LENGTH_SHORT).show()
                        communicator.vibratePhone(500)

                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.doorLockImg.setImageResource(R.drawable.ic_door_close)
                        }, 3000)
                    }
                    SecurityHomeStatus.Failed.value -> communicator.msgBox("Need setting!", "You need to set your main door!", false)
                }
            },{ error ->
                Toast.makeText(it.context, error, Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun createFotoapparat(){

        fotoapparat = view?.let {
            Fotoapparat(
                context = it.context,
                view = binding.cameraView,
                scaleType = ScaleType.CenterCrop,
                lensPosition = front(),
                logger = loggers(
                    logcat()
                ),
                cameraErrorCallback = { error ->
                    Toast.makeText(view?.context, error.message, Toast.LENGTH_SHORT).show()
                }
            )
        }!!
    }
}