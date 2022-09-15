package com.example.sibernetik

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.edit_bilgi_activity.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditBilgiActivity : AppCompatActivity() {

    var PREFS_KEY = "prefs"
    lateinit var sharedPreferences: SharedPreferences

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    private lateinit var auth: FirebaseAuth

    var uid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_bilgi_activity)
        auth = FirebaseAuth.getInstance()

        val user = Firebase.auth.currentUser
        if (user != null) {
            uid = user.uid
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("HATA OLUŞTU! (KULLANICI BULUNAMADI)")
            builder.setNeutralButton("Tamam"){dialogInterface , which ->
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        showInfo(uid)

        btnEditSifre.setOnClickListener {
            val intent = Intent(this, EditSifreActivity::class.java)
            startActivity(intent)
        }

        btnEditImza.setOnClickListener {
            val intent = Intent(this, ImzaActivity::class.java)
            startActivity(intent)
        }

        val hesapSilButton = findViewById<Button>(R.id.hesapSilBtn)
        hesapSilButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Hesabınız silinecektir! Emin misiniz?")
            builder.setPositiveButton("Tamam"){dialogInterface , which ->
                val user = Firebase.auth.currentUser!!
                val uid = user.uid.toString()
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            myRef.child(uid).removeValue()
                            Toast.makeText(this, "Hesabınız Silindi!", Toast.LENGTH_SHORT).show()
                            val i = Intent(this, LoginActivity::class.java)
                            startActivity(i)
                            finish()
                        }
                    }
            }
            builder.setNegativeButton("Iptal"){dialogInterface , which ->
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        val anasayfaBilgiBtn = findViewById<ImageButton>(R.id.anasayfaEditBilgiBtn)

        anasayfaBilgiBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    fun showInfo(uid : String){
        myRef.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                adSoyadEditBTxt.setText(it.child("adSoyad").value.toString())
                ePostaEditBTxt.setText(it.child("eposta").value.toString())
                telNoEditBTxt.setText(it.child("telefon").value.toString())
                tcknEditBTxt.setText(it.child("tckn").value.toString())
                tarihEditBTxt.setText(it.child("tarih").value.toString())
                bolumEditBTxt.setText(it.child("bolum").value.toString())
                yoneticiEditBTxt.setText(it.child("yonetici").value.toString())
                gorevEditBTxt.setText(it.child("gorev").value.toString())
                bolumGorevEditBTxt.setText(it.child("bolumdekiGorev").value.toString())
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("HATA OLUŞTU! (VERİ BULUNAMADI)")
                builder.setNeutralButton("Tamam"){dialogInterface , which ->
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }
    }
}