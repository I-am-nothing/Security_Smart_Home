package com.sakun.security_home.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import android.widget.Toast




class Reciver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(Intent(context, BackgroundService::class.java))
        } else {
            context?.startService(Intent(context, BackgroundService::class.java))
        }
    }
}