package com.sakun.security_home.method.main_card_view_control

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sakun.security_home.R
import com.sakun.security_home.method.SecurityHomeServer
import com.sakun.security_home.method.securtiyHome_server.SecurityHomeStatus

class AlphaAdapters(var context: Context, var arrayList: ArrayList<AlphaChar>): RecyclerView.Adapter<AlphaAdapters.ItemHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {

        val itemHolder = LayoutInflater.from(parent.context).inflate(R.layout.grid_layout_list_item, parent, false)

        return ItemHolder(itemHolder)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {

        val alphaChar: AlphaChar = arrayList[position]

        holder.icons.setImageResource(alphaChar.iconsChar!!)
        holder.alphas.text = alphaChar.alphaChar

        holder.itemView.setOnClickListener{
            SecurityHomeServer(context).setValue(position, { status, message ->
                when(status){
                    SecurityHomeStatus.Failed.value -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    SecurityHomeStatus.Success.value -> {
                        
                    }
                }
            }, { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            })
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class ItemHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var icons: ImageView = itemView.findViewById(R.id.icons_image)
        var alphas: TextView = itemView.findViewById(R.id.alpha_text_view)
    }
}