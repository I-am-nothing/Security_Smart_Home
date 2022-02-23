package com.sakun.security_home.fragmentPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.sakun.security_home.R
import com.sakun.security_home.method.Communicator
import com.sakun.security_home.databinding.FragmentPowerUsedPageBinding
import com.sakun.security_home.method.SecurityHomeServer
import com.sakun.security_home.method.`object`.ChapterList
import com.sakun.security_home.method.`object`.DevicePowerUsed
import com.sakun.security_home.method.power_used_card_view.DevicePowerUsedAdapter
import com.sakun.security_home.method.power_used_card_view.expandable_list_view.ExpandableListViewAdapter
import com.sakun.security_home.method.securtiyHome_server.SecurityHomeStatus
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PowerUsed_page : Fragment() {

    private lateinit var listViewAdapter: ExpandableListViewAdapter
    private lateinit var chapterList: List<ChapterList>
    private lateinit var topicList: HashMap<String, List<ChapterList>>
    private lateinit var communicator: Communicator

    private var _binding: FragmentPowerUsedPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPowerUsedPageBinding.inflate(inflater, container, false)
        val view = binding.root

        communicator = activity as Communicator

        @ColorInt val colorInt = ContextCompat.getColor(view.context, R.color.selectedBackground)
        binding.sparkView.lineColor = colorInt

        SecurityHomeServer(view.context).getDevicePowerUsed({
            when(it.status){
                SecurityHomeStatus.Failed.value -> Toast.makeText(view.context, "Get Power Used Failed!", Toast.LENGTH_SHORT).show()
                SecurityHomeStatus.Success.value -> {

                    Thread {
                        showList(it.dataList)
                    }.run()

                    Thread{
                        binding.allPowerUsedTv.text = "${"%.5f".format(sumPowerUsed(it.dataList))} kWh"
                    }.run()

                    Thread{
                        updateDisplayWithData(sparkViewValue(it.dataList as ArrayList<DevicePowerUsed>))
                    }.run()
                }
            }
        }, { error ->
            Toast.makeText(view.context, error, Toast.LENGTH_SHORT).show()
        })

        return view
    }

    private fun showList(devicePowerUsed: List<DevicePowerUsed>) {
        chapterList = ArrayList()
        topicList = HashMap()

        var i = 0
        var topic: MutableList<ChapterList> = ArrayList()
        var nowPowerUsed = 0f
        while(i < devicePowerUsed.size-1) {

            topic.add(ChapterList(devicePowerUsed[i].deviceName, devicePowerUsed[i].powerUsed.toString()))
            nowPowerUsed += devicePowerUsed[i].powerUsed

            if(devicePowerUsed[i].dateTime != devicePowerUsed[i+1].dateTime){
                (chapterList as ArrayList<ChapterList>).add(ChapterList(updateInfoForDate(devicePowerUsed[i]), nowPowerUsed.toString()))
                topicList[chapterList[chapterList.size - 1].title] = topic
                topic = ArrayList()
                nowPowerUsed = 0f
            }
            i++
        }
        if (devicePowerUsed[devicePowerUsed.size-1].dateTime == devicePowerUsed[devicePowerUsed.size-2].dateTime){
            topic.add(ChapterList(devicePowerUsed[i].deviceName, devicePowerUsed[i].powerUsed.toString()))
            nowPowerUsed += devicePowerUsed[i].powerUsed
        }
        else{
            (chapterList as ArrayList<ChapterList>).add(ChapterList(updateInfoForDate(devicePowerUsed[i]), nowPowerUsed.toString()))
            topicList[chapterList[chapterList.size - 1].title] = topic
            topic = ArrayList()
            topic.add(ChapterList(devicePowerUsed[devicePowerUsed.size-1].deviceName, devicePowerUsed[devicePowerUsed.size-1].powerUsed.toString()))
        }

        listViewAdapter = view?.let { ExpandableListViewAdapter(it.context, chapterList, topicList) }!!
        binding.eListView.setAdapter(listViewAdapter)



        /*(chapterList as ArrayList<String>).add("aaa")
        val topic1: MutableList<String> = ArrayList()
        topic1.add("a")
        topic1.add("b")
        topic1.add("c")
        topic1.add("d")
        topicList[chapterList[0]] = topic1*/
    }

    override fun onResume() {
        super.onResume()
    }

    private fun sumPowerUsed(devicePowerUsed: List<DevicePowerUsed>): Float{
        var sum = 0f
        devicePowerUsed.forEach {
            sum += it.powerUsed
        }

        return sum/1000
    }

    private fun sparkViewValue(devicePowerUsed: ArrayList<DevicePowerUsed>): List<DevicePowerUsed>{
        var i = 1
        while(i < devicePowerUsed.size){
            if(devicePowerUsed[i].dateTime == devicePowerUsed[i-1].dateTime){
                devicePowerUsed[i-1].powerUsed += devicePowerUsed[i].powerUsed
                devicePowerUsed.removeAt(i)
                i --
            }
            i++
        }

        for(i in 1 until devicePowerUsed.size){
            devicePowerUsed[i].powerUsed += devicePowerUsed[i-1].powerUsed
        }

        return devicePowerUsed
    }

    private fun updateDisplayWithData(devicePowerUsed: List<DevicePowerUsed>){
        val adapter = DevicePowerUsedAdapter(devicePowerUsed)
        binding.sparkView.adapter = adapter

        binding.radioButtonDay.isChecked = true
        updateInfoForDate(devicePowerUsed.last())
    }

    private fun updateInfoForDate(devicePowerUsed: DevicePowerUsed): String{
        val outputDateFormat = SimpleDateFormat("HH", Locale.TAIWAN)
        return outputDateFormat.format(devicePowerUsed.dateTime)
    }
}