package com.example.sibernetik

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.forgot_password_activity.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.content.Intent
import android.content.SharedPreferences
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences

    var PREFS_KEY = "prefs"
    var EMAIL_KEY = "email"

    var email = ""
    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password_activity)

        auth = FirebaseAuth.getInstance()

        val btnForget = findViewById<Button>(R.id.btnYeniPwd)

        btnForget.setOnClickListener {
            val eposta = emailTxtForgot.text.toString().trim { it <= ' ' }
            if (eposta.isEmpty()){
                Toast.makeText(this@ForgotPasswordActivity, "Lütfen E-Posta Adresinizi Giriniz!", Toast.LENGTH_SHORT).show()
            }else{
                auth.sendPasswordResetEmail(eposta).addOnCompleteListener {
                    task ->
                    if (task.isSuccessful){
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("şifrenizi sıfırlama bağlantısı e-postanıza gönderildi, lütfen spam klasörünü de kontrol edin!")
                        builder.setNeutralButton("Tamam"){dialogInterface , which ->
                            val intent = Intent(this,LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        val alertDialog: AlertDialog = builder.create()
                        alertDialog.setCancelable(false)
                        alertDialog.show()

                    }else{
                        Toast.makeText(this@ForgotPasswordActivity, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        val anasayfaForgetBtn = findViewById<ImageButton>(R.id.anasayfaForgotBtn)

        anasayfaForgetBtn.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun goToLogin(view : View){
        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}