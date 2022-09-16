package com.example.sibernetik

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_edit_sifre.*

class EditSifreActivity : AppCompatActivity() {

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    private lateinit var auth: FirebaseAuth
    lateinit var progressDialog: ProgressDialog

    var uid = ""
    var userEskiSifre = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_sifre)
        auth = FirebaseAuth.getInstance()

        val user = Firebase.auth.currentUser
        if (user != null) {
            uid = user.uid
            Log.w("cekUID", "$uid")
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("HATA OLUŞTU! (KULLANICI BULUNAMADI)")
            builder.setNeutralButton("Tamam"){dialogInterface , which ->
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        btnSifreDegistir.setOnClickListener {
            val eskiSifre = eskiSifreTxt.text.toString()
            val yeniSifre = yeniSifreTxt.text.toString()
            val yeniSifreTekrar = yeniSifreTekrarTxt.text.toString()

            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Yeni Şifrenizi Kontrol Ediliyor")
            progressDialog.setCancelable(false)
            progressDialog.show()

            if(yeniSifre == yeniSifreTekrar){
                progressDialog.dismiss()
                passwordControl(eskiSifre,yeniSifre, uid)
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Yeni şifreniz birbiriyle eşleşmiyor!!")
                builder.setNeutralButton("Tamam"){dialogInterface , which ->
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                progressDialog.dismiss()
                alertDialog.show()
            }
        }

        val anasayfaSifreBtn = findViewById<ImageButton>(R.id.anasayfaYeniSifreBtn)

        anasayfaSifreBtn.setOnClickListener {
            val intent = Intent(this,EditBilgiActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun changePassword(yeniSifre : String, uid: String){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Şifrenizi Değiştiriliyor")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val user = Firebase.auth.currentUser
        val hashedYeniSifre = HashUtils.sha256(yeniSifre)

        user!!.updatePassword(yeniSifre)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    myRef.child(uid).child("sifre").setValue(hashedYeniSifre)
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Şifreniz başarıyla değiştirildi!")
                    builder.setNeutralButton("Tamam"){dialogInterface , which ->
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    progressDialog.dismiss()
                    alertDialog.show()
                }
                else{
                    progressDialog.dismiss()
                    Toast.makeText(this, "HATA! gnti pwd", Toast.LENGTH_SHORT)
                }
            }
    }

    fun passwordControl(eskiSifre: String, yeniSifre: String, uid : String){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Eski Şifrenizi Kontrol Ediliyor")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val hashedEskiSifre = HashUtils.sha256(eskiSifre)
        Log.w("cekUID", "$uid")
        myRef.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                userEskiSifre = it.child("sifre").value.toString()
                if(hashedEskiSifre == userEskiSifre){
                    changePassword(yeniSifre, uid)
                    progressDialog.dismiss()
                }else{
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Yanlış Şifre Girdiniz!!")
                    builder.setNeutralButton("Tamam"){dialogInterface , which ->
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    progressDialog.dismiss()
                    alertDialog.show()
                    Log.w("cekeskisifre", "$hashedEskiSifre")
                    Log.w("ceksifredb", "$userEskiSifre")
                }
            }
            else{
                progressDialog.dismiss()
                Toast.makeText(this, "HATA!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}