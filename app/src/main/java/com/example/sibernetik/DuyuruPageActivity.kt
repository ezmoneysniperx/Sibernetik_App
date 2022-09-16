package com.example.sibernetik

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.duyuru_activity.*
import kotlinx.android.synthetic.main.duyuru_page.*

class DuyuruPageActivity : AppCompatActivity(){

    var PREFS_KEY = "prefs"
    lateinit var sharedPreferences: SharedPreferences

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Duyuru")
    val myRefUser = database.getReference("Users")

    var id = ""
    var email = ""
    var gorev = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.duyuru_page)

        getGorev()

        val anasayfaDuyuruBtn = findViewById<ImageButton>(R.id.anasayfaDuyuruPageBtn)
        val silDuyuruBtn = findViewById<Button>(R.id.btnDuyuruSil)

        anasayfaDuyuruBtn.setOnClickListener {
            val intent = Intent(this,DuyuruActivity::class.java)
            startActivity(intent)
            finish()
        }

        val bundle = intent.extras
        var dbaslik = ""
        if (bundle != null) {
            dbaslik = bundle.getString("dbaslik").toString()
            getDuyuru(dbaslik)
        }

        silDuyuruBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Duyuru silinecektir! Emin misiniz?")
            builder.setPositiveButton("Tamam"){dialogInterface , which ->
                silDuyuru(dbaslik)
                Toast.makeText(this, "Duyuru Silindi!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DuyuruActivity::class.java)
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("Iptal"){dialogInterface , which ->
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    fun getDuyuru(title: String) {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<DuyuruModel>()
                    if(value!!.baslik.toString() == title){
                        val titleDuyuru = findViewById<TextView>(R.id.textViewPage)
                        val tarihDuyuru = findViewById<TextView>(R.id.dateDuyurPageTxt)
                        val textDuyuru = findViewById<TextView>(R.id.duyuruPageTxt)

                        var titleD = value!!.baslik.toString()
                        var trhD = value!!.tarih.toString()
                        var icerikD = value!!.icerik.toString()

                        titleDuyuru.setText(titleD)
                        tarihDuyuru.setText(trhD)
                        textDuyuru.setText(icerikD)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DuyuruPageActivity,"Hata Olustu!",Toast.LENGTH_SHORT).show()
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
        val silDuyuruBtn = findViewById<Button>(R.id.btnDuyuruSil)
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
                            silDuyuruBtn.visibility = View.VISIBLE
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