package com.sakun.security_home.fragmentPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sakun.security_home.method.Communicator
import com.sakun.security_home.R
import com.sakun.security_home.method.SecurityHomeServer
import com.sakun.security_home.method.`object`.UserLocalData
import com.sakun.security_home.method.main_card_view_control.AlphaChar
import com.sakun.security_home.method.main_card_view_control.AlphaAdapters
import com.sakun.security_home.method.securtiyHome_server.SecurityHomeDeviceDetail
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class RoomControl_page : Fragment() {

    private lateinit var recycleView: RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    private var arrayList: ArrayList<AlphaChar>? = null
    private lateinit var alphaAdapters: AlphaAdapters
    private lateinit var communicator: Communicator
    private lateinit var userLocalData: UserLocalData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_room_control_page, container, false)
        communicator = activity as Communicator

        userLocalData = communicator.getUserLocalData()!!

        recycleView = view.findViewById(R.id.recycler_view)
        gridLayoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)
        recycleView.layoutManager = gridLayoutManager
        recycleView.setHasFixedSize(true)

        Thread {
            Timer().schedule(0, 500) {
                SecurityHomeServer(view.context).getDevice({ getHomeDevice ->

                    val items: ArrayList<AlphaChar> = ArrayList()

                    getHomeDevice.dataList.forEach {
                        if (it.status == 0) {
                            items.add(AlphaChar(when(it.deviceDetailId){
                                SecurityHomeDeviceDetail.Door.value -> R.drawable.ic_door_close
                                SecurityHomeDeviceDetail.Light.value -> R.drawable.ic_light_off
                                SecurityHomeDeviceDetail.Socket.value -> R.drawable.ic_power_off
                                else -> R.drawable.ic_password_null
                            }, it.deviceName))
                        } else {
                            items.add(AlphaChar(when(it.deviceDetailId){
                                SecurityHomeDeviceDetail.Door.value -> R.drawable.ic_door_open
                                SecurityHomeDeviceDetail.Light.value -> R.drawable.ic_light_on
                                SecurityHomeDeviceDetail.Socket.value -> R.drawable.ic_power_on
                                else -> R.drawable.sample_picture
                            }, it.deviceName))
                        }
                    }

                    if (arrayList != items) {
                        arrayList = items
                        alphaAdapters = AlphaAdapters(view?.context!!, arrayList!!)
                        recycleView.adapter = alphaAdapters
                    }
                }, { error ->
                    Toast.makeText(view.context, error, Toast.LENGTH_SHORT).show()
                })
            }
        }.run()

        return view
    }

    override fun onResume() {
        super.onResume()
    }
}