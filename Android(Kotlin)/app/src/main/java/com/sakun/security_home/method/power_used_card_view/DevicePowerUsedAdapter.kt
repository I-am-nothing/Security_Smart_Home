package com.sakun.security_home.method.power_used_card_view

import com.robinhood.spark.SparkAdapter
import com.sakun.security_home.method.`object`.DevicePowerUsed

class DevicePowerUsedAdapter(private val powerUsedData: List<DevicePowerUsed>): SparkAdapter() {
    override fun getCount(): Int = powerUsedData.size

    override fun getItem(index: Int): Any = powerUsedData[index]

    override fun getY(index: Int): Float {
        val chosenDayData = powerUsedData[index]

        return chosenDayData.powerUsed
    }

}
