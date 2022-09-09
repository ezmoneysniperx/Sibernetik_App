package com.example.sibernetik

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.duyuru_activity.*
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.duyuru_activity.recyclerview
import kotlinx.android.synthetic.main.izin_admin_activity.*
import java.util.*
import kotlin.collections.ArrayList

class DuyuruActivity : AppCompatActivity(), DuyuruAdapter.OnItemClickListener {
    var email = ""
    var gorev = ""

    lateinit var sharedPreferences: SharedPreferences


    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Duyuru")
    val myRefUser = database.getReference("Users")

    val data = ArrayList<DuyuruViewModel>()
    val adapter = DuyuruAdapter(data,this)
    var dbaslik = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.duyuru_activity)

        getGorev()

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        showDuyuru()

        val yeniDuyuruBtn = findViewById<Button>(R.id.btnYeniDuyuru)
        val silDuyuruBtn = findViewById<Button>(R.id.btnDuyuruSil)
        val anasayfaDuyuruBtn = findViewById<ImageButton>(R.id.anasayfaDuyuru)

        anasayfaDuyuruBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        yeniDuyuruBtn.setOnClickListener {
            val intent = Intent(this,DuyuruAdminActivity::class.java)
            startActivity(intent)
            finish()
        }
        silDuyuruBtn.setOnClickListener {
            val intent = Intent(this,DuyuruActivity::class.java)
            startActivity(intent)
            silDuyuru(dbaslik)
            finish()
        }
    }
    override fun onItemClick(position: Int) {
        val clickedItem:DuyuruViewModel = data[position]
        dbaslik = clickedItem.title
        duyuruDisplay.setText(dbaslik)
    }

    fun showDuyuru() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                recyclerview.adapter = adapter
                for (postSnapshot in dataSnapshot.children){
                    var value = postSnapshot.getValue<DuyuruModel>()
                    data.add( DuyuruViewModel(R.drawable.duyuru1,
                        value!!.baslik.toString(),
                        value!!.tarih.toString(),
                        value!!.icerik.toString()))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun silDuyuru(title: String){
        if (title.isEmpty()) {
            Toast.makeText(this, "Herhangi bir duyuru seçmediniz!", Toast.LENGTH_SHORT).show()
        } else {
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (postSnapshot in dataSnapshot.children){
                        var value = postSnapshot.getValue<DuyuruModel>()
                        if(value!!.baslik == title){
                            myRef.child(postSnapshot.key.toString()).removeValue()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    null
                }
            })

        }
    }

    fun getGorev(){
        val yeniDuyuruBtn = findViewById<Button>(R.id.btnYeniDuyuru)
        val silDuyuruBtn = findViewById<Button>(R.id.btnDuyuruSil)
        val txtDuyuruBtn = findViewById<LinearLayout>(R.id.textDuyuruBtn)
        val user = Firebase.auth.currentUser
        user?.let {
            for (profile in it.providerData) {
                email = profile.email.toString()
            }
        }
        myRefUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if (value!!.ePosta == email){
                        gorev = value!!.gorev.toString()
                        if(gorev == "INSAN KAYNAKLAR" || gorev == "YONETICI"){
                            yeniDuyuruBtn.visibility = View.VISIBLE
                            silDuyuruBtn.visibility = View.VISIBLE
                            txtDuyuruBtn.visibility = View.VISIBLE
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                null
            }
        })
    }
}