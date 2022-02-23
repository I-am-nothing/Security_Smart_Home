package com.sakun.security_home.method

import android.content.Context
import android.os.*
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sakun.security_home.method.gson.GetDevicePowerUsed
import com.sakun.security_home.method.gson.GetHomeDevice
import com.sakun.security_home.method.securtiyHome_server.Encryption
import org.json.JSONObject

class SecurityHomeServer(private var context: Context): Encryption() {


    private var timeout: Long = 5000

    companion object {
        private const val IP_ADDRESS = "192.168.0.147"
        private const val VERSION = "2.0"

        @JvmStatic
        private var AccessStatic = false
        @JvmStatic
        private var AccountToken = ""
        @JvmStatic
        private var DeviceId = ""
        @JvmStatic
        private var HomeDeviceList: GetHomeDevice? = null
    }

    fun checkVersion(response: (Int, String) -> Unit, error: (String) -> Unit) {
        val url = "http://$IP_ADDRESS/security_home/status"
        val jsonObject = JSONObject().put("appVersion", VERSION)
        var status = false

        val queue = Volley.newRequestQueue(context)
        val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,
                {
                    status = true
                    AccessStatic = true
                    response(it.getInt("status"), it.getString("message"))
                }, {
                    if(!status){
                        status = true
                        error("Please check your Internet network \n[${it.message}]")
                    }
                }
        )
        queue.add(request)

        Handler(Looper.getMainLooper()).postDelayed({
            if(!status){
                status = true
                response(-1, "Please check your Internet network")
            }
        }, timeout)
    }

    fun login(userId: String, password: String, response: (Int, String) -> Unit, error: (String) -> Unit){
        if(AccessStatic) {
            val url = "http://$IP_ADDRESS/security_home/account/login"
            val jsonObject = JSONObject()
                .put("userId", super.encryption(userId))
                .put("password", super.encryption(password))
            var status = false

            val queue = Volley.newRequestQueue(context)
            val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,
                {
                    status = true
                    response(it.getInt("status"), it.getString("message"))
                }, {
                    if (!status) {
                        status = true
                        error(it.message.toString())
                    }
                }
            )
            queue.add(request)

            Handler(Looper.getMainLooper()).postDelayed({
                if (!status) {
                    status = true
                    response(-1, "Please check your Internet network")
                }
            }, timeout)
        }
        else{
            error("Did you hack my App??? :(")
        }
    }

    fun deviceLogin(accountToken: String, deviceId: String, response: (Int, String) -> Unit, error: (String) -> Unit){
        if(AccessStatic) {
            val url = "http://$IP_ADDRESS/security_home/account/deviceLogin"
            val jsonObject = JSONObject()
                .put("accountToken", super.encryption(accountToken))
                .put("deviceId", super.encryption(deviceId))
            var status = false

            val queue = Volley.newRequestQueue(context)
            val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,
                {
                    AccountToken = accountToken
                    DeviceId = deviceId
                    status = true
                    response(it.getInt("status"), it.getString("message"))
                }, {
                    if (!status) {
                        status = true
                        error(it.message.toString())
                    }
                }
            )
            queue.add(request)

            Handler(Looper.getMainLooper()).postDelayed({
                if (!status) {
                    status = true
                    response(-1, "Please check your Internet network")
                }
            }, timeout)
        }
        else{
            error("Did you hack my App??? :(")
        }
    }

    fun openMainDoor(response: (Int, String) -> Unit, error: (String) -> Unit){
        if(AccessStatic) {
            val url = "http://$IP_ADDRESS/security_home/account/openDoor"
            val jsonObject = JSONObject()
                    .put("accountToken", super.encryption(AccountToken))
                    .put("deviceId", super.encryption(DeviceId))

            var status = false

            val queue = Volley.newRequestQueue(context)
            val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,{
                status = true
                response(it.getInt("status"), it.getString("message"))
            }, {
                if (!status) {
                    status = true
                    error(it.message.toString())
                }
            })
            queue.add(request)

            Handler(Looper.getMainLooper()).postDelayed({
                if (!status) {
                    status = true
                    response(-1, "Please check your Internet network")
                }
            }, timeout)
        }
        else{
            error("Did you hack my App??? :(")
        }
    }

    fun doorRing(response: (Int, String) -> Unit, error: (String) -> Unit){
        if(AccessStatic) {
            val url = "http://$IP_ADDRESS/security_home/account/openDoor"
            val jsonObject = JSONObject()
                    .put("accountToken", super.encryption(AccountToken))
                    .put("deviceId", super.encryption(DeviceId))
                    .put("deviceStatus", 2)

            var status = false

            val queue = Volley.newRequestQueue(context)
            val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,{
                status = true
                response(it.getInt("status"), it.getString("message"))
            }, {
                if (!status) {
                    status = true
                    error(it.message.toString())
                }
            })
            queue.add(request)

            Handler(Looper.getMainLooper()).postDelayed({
                if (!status) {
                    status = true
                    response(-1, "Please check your Internet network")
                }
            }, timeout)
        }
        else{
            error("Did you hack my App??? :(")
        }
    }

    fun getDevice(response: (GetHomeDevice) -> Unit, error: (String) -> Unit){
        if(AccessStatic) {
            val url = "http://$IP_ADDRESS/security_home/app/getDevice"
            val jsonObject = JSONObject()
                    .put("accountToken", super.encryption(AccountToken))
                    .put("deviceId", super.encryption(DeviceId))

            var status = false

            val queue = Volley.newRequestQueue(context)
            val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,{ it ->
                val homeDeviceList = Gson().fromJson(it.toString(), GetHomeDevice::class.java)
                HomeDeviceList = homeDeviceList
                status = true
                response(homeDeviceList)
            }, {
                if (!status) {
                    status = true
                    error(it.message.toString())
                }
            })
            queue.add(request)

            Handler(Looper.getMainLooper()).postDelayed({
                if (!status) {
                    status = true
                    error("Please check your Internet network")
                }
            }, timeout)
        }
        else{
            error("Did you hack my App??? :(")
        }
    }

    fun setValue(position: Int, response: (Int, String) -> Unit, error: (String) -> Unit){
        if(AccessStatic) {
            val url = "http://$IP_ADDRESS/security_home/app/setValue"
            val jsonObject = JSONObject()
                    .put("accountToken", super.encryption(AccountToken))
                    .put("deviceId", super.encryption(DeviceId))
                    .put("homeDeviceId", super.encryption(HomeDeviceList!!.dataList[position].deviceId.toString()))
                    .put("deviceStatus",
                            if(HomeDeviceList!!.dataList[position].status == 0){
                                1
                            }
                            else{
                                0
                            })

            var status = false

            val queue = Volley.newRequestQueue(context)
            val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,{
                status = true
                response(it.getInt("status"), it.getString("message"))
            }, {
                if (!status) {
                    status = true
                    error(it.message.toString())
                }
            })
            queue.add(request)

            Handler(Looper.getMainLooper()).postDelayed({
                if (!status) {
                    status = true
                    error("Please check your Internet network")
                }
            }, timeout)
        }
        else{
            error("Did you hack my App??? :(")
        }
    }

    fun getDevicePowerUsed(response: (GetDevicePowerUsed) -> Unit, error: (String) -> Unit){
        if(AccessStatic) {
            val url = "http://$IP_ADDRESS/security_home/app/getDevicePowerUsed"
            val jsonObject = JSONObject()
                .put("accountToken", super.encryption(AccountToken))
                .put("deviceId", super.encryption(DeviceId))

            var status = false

            val queue = Volley.newRequestQueue(context)
            val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,{ it ->
                val devicePowerUsed = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create().fromJson(it.toString(), GetDevicePowerUsed::class.java)
                status = true
                response(devicePowerUsed)
            }, {
                if (!status) {
                    status = true
                    error(it.message.toString())
                }
            })
            queue.add(request)

            Handler(Looper.getMainLooper()).postDelayed({
                if (!status) {
                    status = true
                    error("Please check your Internet network")
                }
            }, timeout)
        }
        else{
            error("Did you hack my App??? :(")
        }
    }
}