package com.sakun.security_home

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.gson.Gson
import com.sakun.security_home.databinding.ActivityMainBinding
import com.sakun.security_home.method.Communicator
import com.sakun.security_home.method.`object`.UserLocalData
import com.sakun.security_home.service.BackgroundService
import com.sakun.security_home.service.Reciver


class MainActivity : AppCompatActivity(), Communicator {

    private lateinit var binding: ActivityMainBinding
    private lateinit var backgroundService: BackgroundService
    private lateinit var serviceIntent: Intent
    private lateinit var appBarConfiguration: AppBarConfiguration

    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)

    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.main_page, R.id.powerUsed_page, R.id.roomControl_page
                , R.id.setting_page, R.id.aboutUs_page, R.id.mode_page), binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        actionBarHide()

        if(getUserLocalData() == null) {
            updateUserLocalData(
                UserLocalData(
                    "sakun", "", false,
                    false, 0, 5000, androidId, ""
                )
            )
        }

        getBiometricPermission()

        backgroundService = BackgroundService()
        serviceIntent = Intent(this, backgroundService.javaClass)
        if (!isMyServiceRunning(backgroundService.javaClass)) {
            startService(serviceIntent);
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun isMyServiceRunning(serviceClass: Class<Any>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.runningAppProcesses) {
            if (serviceClass.name == service.javaClass.name) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            val userLocalData = getUserLocalData()
            if (userLocalData != null) {
                userLocalData.appPauseTime = System.currentTimeMillis()
                updateUserLocalData(userLocalData)
            }
        }
    }

    override fun onDestroy() {
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartService"
        broadcastIntent.setClass(this, Reciver::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }



    override fun hasNoPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissions,0)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getBiometricPermission(){
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                messageBox(
                    "Need Permission!",
                    "This app need some important permission, please open that."
                )
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                    )
                }
                startActivityForResult(enrollIntent, 87)
            }
        }
    }

    private fun messageBox(title: String, message: String){
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton("OK"){ _, _ -> }
        alertDialog.show()
    }

    override fun getUserLocalData(): UserLocalData? {
        return try {
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            val json = sharedPref.getString("UserLocalData", "")
            Gson().fromJson(json, UserLocalData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun loginTimeout(): Boolean {

        val userLocalData = getUserLocalData()!!
        val resumeTime = System.currentTimeMillis();
        return if(userLocalData.appPauseTime != 0L) {
            resumeTime - userLocalData.appPauseTime > userLocalData.loginTimeout
        } else{
            false
        }
    }

    override fun updateUserLocalData(userLocalData: UserLocalData) {
        val prefsEditor = getPreferences(Context.MODE_PRIVATE).edit()
        val json = Gson().toJson(userLocalData)
        prefsEditor.putString("UserLocalData", json)
        prefsEditor.apply()
    }

    override fun resetAppTimeout() {
        val userLocalData = getUserLocalData()
        if(userLocalData != null) {
            userLocalData.appPauseTime = 0
            updateUserLocalData(userLocalData)
        }
    }

    override fun actionBarHide() {
        supportActionBar?.hide()
    }

    override fun actionBarShow() {
        supportActionBar?.show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun vibratePhone(millis: Long) {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun msgBox(title: String, description: String, close: Boolean){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(title)
        alertDialog.setMessage(description)
        alertDialog.setIcon(R.mipmap.ic_launcher)
        alertDialog.setPositiveButton("OK"){ _, _ ->
            if(close){
                finish()
            }
        }
        alertDialog.show()
    }
}