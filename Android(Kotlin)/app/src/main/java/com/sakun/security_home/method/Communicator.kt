package com.sakun.security_home.method

import com.sakun.security_home.method.`object`.UserLocalData

interface Communicator {
    fun updateUserLocalData(userLocalData: UserLocalData)
    fun getUserLocalData(): UserLocalData?
    fun loginTimeout(): Boolean
    fun resetAppTimeout()
    fun actionBarHide()
    fun actionBarShow()
    fun msgBox(title: String, description: String, close: Boolean)
    fun vibratePhone(millis: Long)
    fun hasNoPermissions(): Boolean
    fun requestPermission()
}