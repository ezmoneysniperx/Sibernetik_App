package com.example.sibernetik

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MalzemeAdapter(private val mList: List<MalzemeViewModel>,private val listener: OnItemClickListener) : RecyclerView.Adapter<MalzemeAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.malzeme_view_design, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val MalzemeViewModel = mList[position]

        holder.imageView.setImageResource(MalzemeViewModel.image)
        holder.name.text = MalzemeViewModel.adsoyad
        holder.title.text = MalzemeViewModel.malzemead
        holder.proje.text = MalzemeViewModel.proje
        holder.fiyat.text = MalzemeViewModel.fiyat

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView),
        View.OnClickListener {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val name: TextView = itemView.findViewById(R.id.adsoyad)
        val title: TextView = itemView.findViewById(R.id.malzemead)
        val proje: TextView = itemView.findViewById(R.id.proje)
        val fiyat: TextView = itemView.findViewById(R.id.fiyat)

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