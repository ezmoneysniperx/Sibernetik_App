package com.example.sibernetik

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.type.DateTime
import kotlinx.android.synthetic.main.account_onayla_activity.*
import kotlinx.android.synthetic.main.duyuru_activity.*
import kotlinx.android.synthetic.main.duyuru_activity.recyclerview
import kotlinx.android.synthetic.main.duyuru_admin_activity.*
import kotlinx.android.synthetic.main.duyuru_page.*
import kotlinx.android.synthetic.main.izin_admin_activity.*
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Attributes
import kotlin.collections.ArrayList


class DuyuruActivity : AppCompatActivity(), DuyuruAdapter.OnItemClickListener {
    var email = ""
    var gorev = ""

    lateinit var sharedPreferences: SharedPreferences


    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Duyuru")
    val myRefUser = database.getReference("Users")
    private lateinit var auth: FirebaseAuth
    var serverKey = "servekey"

    val data = ArrayList<DuyuruViewModel>()
    var adapter = DuyuruAdapter(data,this)
    var dbaslik = ""
    var tarihD = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContentView(R.layout.duyuru_activity)

        getGorev()

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        showDuyuru()

        val yeniDuyuruBtn = findViewById<Button>(R.id.btnYeniDuyuru)

        val anasayfaDuyuruBtn = findViewById<ImageButton>(R.id.anasayfaDuyuru)

        anasayfaDuyuruBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        yeniDuyuruBtn.setOnClickListener {
            if(gorev == "INSAN KAYNAKLAR" || gorev == "YONETICI"){
                val intent = Intent(this,DuyuruAdminActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this, "Duyuru Sadece Yönetici ve İ.K Oluşturabilir!", Toast.LENGTH_SHORT).show()
            }

        }



    }
    override fun onItemClick(position: Int) {
        val clickedItem:DuyuruViewModel = data[position]
        dbaslik = clickedItem.title

        tarihD = clickedItem.date

        val intent = Intent(this,DuyuruPageActivity::class.java)
        intent.putExtra("dbaslik",dbaslik)
        startActivity(intent)
        finish()
    }

    fun showDuyuru() {
        val dbRef = myRef.orderByChild("tarih")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                recyclerview.adapter = adapter
                for (postSnapshot in dataSnapshot.children) {
                    var value = postSnapshot.getValue<DuyuruModel>()
                    data.add(
                        DuyuruViewModel(
                            R.drawable.duyuru1,
                            value!!.baslik.toString(),
                            value!!.tarih.toString(),
                            value!!.icerik.toString()
                        )
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getGorev(){
        val yeniDuyuruBtn = findViewById<Button>(R.id.btnYeniDuyuru)
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
