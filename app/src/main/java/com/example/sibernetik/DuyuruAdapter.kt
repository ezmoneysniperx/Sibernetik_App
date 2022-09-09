package com.example.sibernetik

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class DuyuruAdapter(private val mList: List<DuyuruViewModel>,private val listener: OnItemClickListener) : RecyclerView.Adapter<DuyuruAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.duyuru_view_design, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var selectedItemPosition = 0
        val DuyuruViewModel = mList[position]

        holder.imageView.setImageResource(DuyuruViewModel.image)
        holder.title.text = DuyuruViewModel.title
        holder.date.text = DuyuruViewModel.date
        holder.texts.text = DuyuruViewModel.text


    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView),
        View.OnClickListener {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val title: TextView = itemView.findViewById(R.id.titleDuyuru)
        val date: TextView = itemView.findViewById(R.id.dateDuyuru)
        val texts: TextView = itemView.findViewById(R.id.textDuyuru)

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