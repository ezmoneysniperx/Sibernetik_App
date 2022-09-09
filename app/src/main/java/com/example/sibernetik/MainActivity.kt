package com.example.sibernetik

import android.app.*
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.activity_izin_menu.*

class MainActivity : AppCompatActivity()  {
    var PREFS_KEY = "prefs"
    var EMAIL_KEY = "email"
    var name = ""
    var gorev = ""
    var email = ""
    var uid = ""
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var auth: FirebaseAuth
    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    val data = ArrayList<ItemsViewModel>()
    lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth

        val user = Firebase.auth.currentUser
        name = user!!.displayName.toString()
        uid = user.uid

        getGorev()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

        top_header.setText("Hoşgeldiniz $name!")

        val logoutButton = findViewById<ImageButton>(R.id.logoutBtn)
        val izinButton = findViewById<ImageButton>(R.id.izinBtn)
        val malzemeButton = findViewById<ImageButton>(R.id.malzemeBtn)
        val duyuruButton = findViewById<ImageButton>(R.id.duyuruBtn)
        val hesapYonetimButton = findViewById<ImageButton>(R.id.hesapYonetimBtn)
        val izinHakkiButton = findViewById<ImageButton>(R.id.izinHakkiBtn)
        val bilgilerimButton = findViewById<Button>(R.id.editBilgiBtn)

        val popupMenuMalzeme = PopupMenu(this@MainActivity, malzemeButton)
        popupMenuMalzeme.menuInflater.inflate(R.menu.popup_menu, popupMenuMalzeme.menu)
        popupMenuMalzeme.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId
            if (id == R.id.adminEkran){
                val admin = Intent(this, MalzemeAdminActivity::class.java)
                startActivity(admin)
            }else if (id == R.id.userEkran){
                val user = Intent(this, MalzemeUserActivity::class.java)
                startActivity(user)
            }
            false
        }

        val popupMenuIzin = PopupMenu(this@MainActivity, izinButton)
        popupMenuIzin.menuInflater.inflate(R.menu.popup_menu, popupMenuIzin.menu)
        popupMenuIzin.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId
            if (id == R.id.adminEkran) {
                val intent = Intent(this, IzinMenuActivity::class.java)
                intent.putExtra("Yetki","Admin")
                startActivity(intent)
            } else if (id == R.id.userEkran) {
                val intent = Intent(this, IzinMenuActivity::class.java)
                intent.putExtra("Yetki","User")
                startActivity(intent)
            }
            false
        }

        logoutButton.setOnClickListener{
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            Firebase.messaging.unsubscribeFromTopic("$uid")
                .addOnCompleteListener { task ->
                    var msg = "Unsubscribed"
                    if (!task.isSuccessful) {
                        msg = "Unsubscribe failed"
                    }
                    Log.d("Unsubscribe", "$msg - $uid")
                    //Toast.makeText(baseContext, "$msg - $name", Toast.LENGTH_SHORT).show()
                }
            Firebase.messaging.unsubscribeFromTopic("IK")
                .addOnCompleteListener { task ->
                    var msg = "Unsubscribed"
                    if (!task.isSuccessful) {
                        msg = "Unsubscribe failed"
                    }
                    Log.d("Unsubscribe", "$msg - IK")
                    //Toast.makeText(baseContext, "$msg - $name", Toast.LENGTH_SHORT).show()
                }
            Firebase.messaging.unsubscribeFromTopic("allUser")
                .addOnCompleteListener { task ->
                    var msg = "Unsubscribed"
                    if (!task.isSuccessful) {
                        msg = "Unsubscribe failed"
                    }
                    Log.d("Unsubscribe", "$msg - allUser")
                    //Toast.makeText(baseContext, "$msg - allUser", Toast.LENGTH_SHORT).show()
                }
            auth.signOut()
            finish()
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)

        }

        izinButton.setOnClickListener {
            myRef.child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val imzaDurum = it.child("imzaExist").value.toString()
                    Log.w("test","$imzaDurum")
                    if(imzaDurum == "1"){
                        if(gorev == "INSAN KAYNAKLAR" || gorev == "YONETICI"){
                            popupMenuIzin.show()
                        }else{
                            val intent = Intent(this, IzinMenuActivity::class.java)
                            intent.putExtra("Yetki","User")
                            startActivity(intent)
                        }
                    }else{
                        Toast.makeText(this@MainActivity, "Bu Özelliği Kullanmak İçin İmza Oluşturmanız Gerekiyor! Bilgilerim Sayfasında Ulaşabilirsiniz!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        malzemeButton.setOnClickListener {
            if(gorev == "INSAN KAYNAKLAR" || gorev == "YONETICI"){
                popupMenuMalzeme.show()
            }else{
                val user = Intent(this, MalzemeUserActivity::class.java)
                startActivity(user)
            }
        }

        duyuruButton.setOnClickListener {
            val intent = Intent(this, DuyuruActivity::class.java)
            startActivity(intent)
        }

        yemekMenuBtn.setOnClickListener{
            val intent = Intent(this,YemekActivity::class.java)
            startActivity(intent)
        }

        hesapYonetimButton.setOnClickListener {
            if(gorev == "INSAN KAYNAKLAR"){
                val intent = Intent(this,AccountManagementActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this@MainActivity, "Bu sayfaya sadece adminler erişebilir!!", Toast.LENGTH_SHORT).show()
            }
        }

        izinHakkiButton.setOnClickListener{
            if(gorev == "INSAN KAYNAKLAR"){
                val intent = Intent(this,IzinManagementActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this@MainActivity, "Bu sayfaya sadece adminler erişebilir!!", Toast.LENGTH_SHORT).show()
            }
        }

        bilgilerimButton.setOnClickListener{
            val intent = Intent(this,EditBilgiActivity::class.java)
            startActivity(intent)
        }
    }

    fun getGorev(){
        val hesapYonetimButton = findViewById<ImageButton>(R.id.hesapYonetimBtn)
        val izinHakkiButton = findViewById<ImageButton>(R.id.izinHakkiBtn)
        val user = Firebase.auth.currentUser
        user?.let {
            for (profile in it.providerData) {
                email = profile.email.toString()
            }
        }
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if (value!!.ePosta == email){
                        gorev = value!!.gorev.toString()
                        if(gorev == "INSAN KAYNAKLAR"){
                            hesapYonetimButton.visibility = View.VISIBLE
                            izinHakkiButton.visibility = View.VISIBLE
                        }else{
                            hesapYonetimButton.visibility = View.INVISIBLE
                            izinHakkiButton.visibility = View.INVISIBLE
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