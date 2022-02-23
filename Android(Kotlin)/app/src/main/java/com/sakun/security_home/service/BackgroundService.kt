package com.sakun.security_home.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.*
import com.sakun.security_home.method.SecurityHomeServer
import java.util.*
import kotlin.concurrent.schedule


class BackgroundService: Service() {

    private var timerTask: TimerTask? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            //startMyOwnForeground()
        } else {
            //startForeground(1, Notification())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground(){
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)

        val notificationBuilder: NotificationCompat.Builder = Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimerTask()

        val broadcastIntent = Intent(this, BackgroundService::class.java).setAction("restartService")
        this.sendBroadcast(broadcastIntent)
    }



    private fun startTimer(){

        timerTask = Timer().schedule(1000, 5000){
            /*SecurityHomeServer(this@BackgroundService).checkVersion({ status, message ->
                Toast.makeText(this@BackgroundService, status.toString() + message, Toast.LENGTH_SHORT).show()
            },{ error ->
                Toast.makeText(this@BackgroundService, error, Toast.LENGTH_SHORT).show()
            })*/
        }
    }

    private fun stopTimerTask() {
        timerTask?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}