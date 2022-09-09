package com.example.sibernetik

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_izin_menu.*

class IzinMenuActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_izin_menu)

        val anasayfaIzinBtn = findViewById<ImageButton>(R.id.anasayfaIzinBtn)

        val yetki = intent.getStringExtra("Yetki")

        saatlikBtn.setOnClickListener {
            if (yetki == "Admin") {
                val intent = Intent(this, IzinAdminTime::class.java)
                startActivity(intent)
            } else {
                val user = Intent(this, IzinUserTime::class.java)
                startActivity(user)
            }
        }

        gunlukBtn.setOnClickListener {
            if (yetki == "Admin") {
                val intent = Intent(this, IzinAdminActivity::class.java)
                startActivity(intent)
            } else {
                val user = Intent(this, IzinUserActivity::class.java)
                startActivity(user)
            }
        }

        anasayfaIzinBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}