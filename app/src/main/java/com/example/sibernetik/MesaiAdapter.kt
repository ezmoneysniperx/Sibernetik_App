package com.example.sibernetik

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MesaiAdapter(private val mList: List<MesaiViewModel>,private val listener: OnItemClickListener) : RecyclerView.Adapter<MesaiAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mesai_view_design, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val MesaiViewModel = mList[position]

        holder.imageView.setImageResource(MesaiViewModel.image)
        holder.name.text = MesaiViewModel.adsoyad
        holder.tarih.text = MesaiViewModel.mesaiTar
        holder.timeBas.text = MesaiViewModel.timeBas
        holder.timeBit.text = MesaiViewModel.timeBit
        holder.sebeb.text = MesaiViewModel.sebeb
        holder.yonetici1.text = MesaiViewModel.yonetici1onay
        holder.yonetici2.text = MesaiViewModel.yonetici2onay

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView),
        View.OnClickListener {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val name: TextView = itemView.findViewById(R.id.adSoyadMesai)
        val tarih: TextView = itemView.findViewById(R.id.dateMesai)
        val timeBas: TextView = itemView.findViewById(R.id.timeBasMesai)
        val timeBit: TextView = itemView.findViewById(R.id.timeBitMesai)
        val sebeb: TextView = itemView.findViewById(R.id.descMesai)
        val yonetici1: TextView = itemView.findViewById(R.id.yonetici1onayMesai)
        val yonetici2: TextView = itemView.findViewById(R.id.yonetici2onayMesai)

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