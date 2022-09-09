package com.example.sibernetik

import android.app.Notification
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val mList: List<ItemsViewModel>, private val listener: OnItemClickListener) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_design, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]

        holder.imageView.setImageResource(ItemsViewModel.image)
        holder.textView.text = ItemsViewModel.text
        holder.saat.text = ItemsViewModel.time
        holder.date.text = ItemsViewModel.date
        holder.desc.text = ItemsViewModel.nedeni
        holder.yonetici1onay.text = ItemsViewModel.yonetici1onay
        holder.yonetici2onay.text = ItemsViewModel.yonetici2onay
        holder.mesaj.text = ItemsViewModel.mesaj
        holder.id.text = ItemsViewModel.id
        holder.day.text = ItemsViewModel.day
        holder.tip.text = ItemsViewModel.tip
        holder.maz.text = ItemsViewModel.mazeret

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView),
        View.OnClickListener{
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val saat: TextView = itemView.findViewById(R.id.time)
        val date: TextView = itemView.findViewById(R.id.date)
        val desc: TextView = itemView.findViewById(R.id.desc)
        val yonetici1onay: TextView = itemView.findViewById(R.id.yonetici1onay)
        val yonetici2onay: TextView = itemView.findViewById(R.id.yonetici2onay)
        val mesaj: TextView = itemView.findViewById(R.id.mesaj)
        val id: TextView = itemView.findViewById(R.id.id)
        val day: TextView = itemView.findViewById(R.id.day)
        val tip: TextView = itemView.findViewById(R.id.tip)
        val maz: TextView = itemView.findViewById(R.id.maz)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}