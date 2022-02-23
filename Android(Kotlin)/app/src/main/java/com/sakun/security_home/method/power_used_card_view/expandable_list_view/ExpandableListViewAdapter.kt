package com.sakun.security_home.method.power_used_card_view.expandable_list_view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.get
import com.sakun.security_home.R
import com.sakun.security_home.method.`object`.ChapterList

class ExpandableListViewAdapter internal constructor(private val context: Context, private val chapterList: List<ChapterList>, private val topicsList: HashMap<String, List<ChapterList>>):
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return chapterList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return this.topicsList[this.chapterList[groupPosition].title]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return  chapterList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return this.topicsList[this.chapterList[groupPosition].title]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        val chapterObject = getGroup(groupPosition) as ChapterList

        if(convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.chapter_list, null)
        }

        val chapterTv: TextView = convertView!!.findViewById(R.id.chapter_tv)
        val powerUsedTv: TextView = convertView.findViewById(R.id.powerUsed_tv)

        chapterTv.text = chapterObject.title
        powerUsedTv.text = "${chapterObject.powerUsed} Wh"

        return convertView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        val topicObject = getChild(groupPosition, childPosition) as ChapterList

        if(convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.topics_list, null)
        }

        val topicTv: TextView = convertView!!.findViewById(R.id.topics_tv)
        val topicPowerUsedTv: TextView = convertView.findViewById(R.id.topicPowerUsed_tv)
        topicTv.text = "${topicObject.powerUsed} Wh"
        topicPowerUsedTv.text = topicObject.title

        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}