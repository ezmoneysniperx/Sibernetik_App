package com.example.sibernetik

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.login_activity.*
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity(){

    var adsoyad = ""
    var gorev = ""

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        auth = Firebase.auth

        val button = findViewById<Button>(R.id.btnLogin)
        button.setOnClickListener{
            val eposta = emailTxt.text.toString().trim()
            val sifre = passwordTxt.text.toString()

            if(eposta.isEmpty() || sifre.isEmpty()){
                Toast.makeText(this, "Lütfen E-Postayı ve Şifreyi Doldurun!", Toast.LENGTH_SHORT).show()
            }else{
                getGorevLogin(eposta)
                login(eposta,sifre)
            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
        finish()
        exitProcess(0)
    }

    fun goToRegister(view: View) {
        val intent = Intent(this,RegisterActivity::class.java)
        startActivity(intent)
    }
    
    fun forgotPassword(view: View) {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    fun login(email : String, pwd : String){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Giriş Yapılıyor")
        progressDialog.setCancelable(false)
        progressDialog.show()

        if(checkForInternet(this)){
            auth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user!!.uid
                        val profileUpdates = userProfileChangeRequest {
                            displayName = adsoyad
                        }
                        user!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task -> }
                        Firebase.crashlytics.setUserId("$uid")
                        Firebase.crashlytics.log("message")
                        if(gorev == "INSAN KAYNAKLARI"){
                            Firebase.messaging.subscribeToTopic("IK")
                                .addOnCompleteListener { task ->
                                    var msg = "Subscribed"
                                    if (!task.isSuccessful) {
                                        msg = "Subscribe failed"
                                    }
                                    Log.d("Subscribe", "$msg - IK")
                                }
                        }
                        Firebase.messaging.subscribeToTopic("$uid")
                            .addOnCompleteListener { task ->
                                var msg = "Subscribed"
                                if (!task.isSuccessful) {
                                    msg = "Subscribe failed"
                                }
                                Log.d("Subscribe", "$msg - $uid")
                            }
                        Firebase.messaging.subscribeToTopic("allUser")
                            .addOnCompleteListener { task ->
                                var msg = "Subscribed"
                                if (!task.isSuccessful) {
                                    msg = "Subscribe failed"
                                }
                                Log.d("Subscribe", "$msg - allUser")
                            }
                        progressDialog.dismiss()
                        //Toast.makeText(this@LoginActivity, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else {
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("Yanlış Email / Şifre Girdiniz veya Hesabınız Henüz Onaylanmadı! Lütfen Tekrar Kontrol Ediniz")
                        builder.setNeutralButton("Tamam"){dialogInterface , which ->
                            emailTxt.text.clear()
                            passwordTxt.text.clear()
                        }
                        val alertDialog: AlertDialog = builder.create()
                        alertDialog.setCancelable(false)
                        progressDialog.dismiss()
                        alertDialog.show()
                    }
                }
        }else{
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Sibernetik App Kullanabilmek İçin Cihazınız İnternete Bağlı Olmalıdır!")
            builder.setNeutralButton("Tamam"){dialogInterface , which ->
                this.finish()
                exitProcess(0)
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            progressDialog.dismiss()
            alertDialog.show()
        }
    }

    fun getGorevLogin(email: String){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if (value!!.ePosta == email){
                        gorev = value!!.gorev.toString()
                        adsoyad = value!!.adSoyad.toString()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "HATA OLUSTU! Lutfen Tekrar Deneyin", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}