package com.example.sibernetik

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.card_view_design.*
import kotlinx.android.synthetic.main.malzeme_activity.*
import kotlinx.android.synthetic.main.malzeme_admin_activity.*
import kotlinx.android.synthetic.main.malzeme_admin_activity.nameTxt
import kotlinx.android.synthetic.main.malzeme_admin_activity.recyclerview
import kotlinx.android.synthetic.main.register_activity.*
import org.w3c.dom.Text

class MalzemeAdminActivity : AppCompatActivity(), MalzemeAdapter.OnItemClickListener {
    val db = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = db.getReference("Malzeme")

    val data = ArrayList<MalzemeViewModel>()
    val adapter = MalzemeAdapter(data, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.malzeme_admin_activity)
        showCommentsAdmin()

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        val araBtn = findViewById<Button>(R.id.araBtn)
        val anasayfaAdminBtn = findViewById<ImageButton>(R.id.anasayfaMalzemeAdminBtn)

        araBtn.setOnClickListener {
            val adSoyad = nameTxt.text.toString()
            searchMalzemeAdmin(adSoyad)
        }

        anasayfaAdminBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun showCommentsAdmin() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                recyclerview.adapter = adapter
                adapter.notifyDataSetChanged()
                for (postSnapshot in dataSnapshot.children) {
                    var value = postSnapshot.getValue<MalzemeModel>()
                    data.add(
                        MalzemeViewModel(
                            R.drawable.tools2,
                            value!!.adSoyad.toString(),
                            value!!.malzemead.toString(),
                            value!!.proje.toString(),
                            value!!.fiyat.toString(),
                            ""
                        )
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun searchMalzemeAdmin(name: String) {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                recyclerview.adapter = adapter
                adapter.notifyDataSetChanged()
                for (postSnapshot in dataSnapshot.children) {
                    var value = postSnapshot.getValue<MalzemeModel>()
                    if (value!!.adSoyad == name) {
                        data.add(
                            MalzemeViewModel(
                                R.drawable.tools2,
                                value!!.adSoyad.toString(),
                                value!!.malzemead.toString(),
                                value!!.proje.toString(),
                                value!!.fiyat.toString(),
                                ""
                            )
                        )
                    } else {
                        Toast.makeText(this@MalzemeAdminActivity, "Bulunamadi", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onItemClick(position: Int) {
    }
}