package com.example.sibernetik

import android.app.*
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity()  {

    var name = ""
    var gorev = ""
    var email = ""
    var uid = ""

    private lateinit var auth: FirebaseAuth
    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    val data = ArrayList<ItemsViewModel>()
    lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(!checkForInternet(this)){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Sibernetik App Kullanabilmek İçin Cihazınız İnternete Bağlı Olmalıdır!")
            builder.setNeutralButton("Tamam"){dialogInterface , which ->
                this.finish()
                exitProcess(0)
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        auth = Firebase.auth
        val user = Firebase.auth.currentUser
        if(user != null){
            name = user!!.displayName.toString()
            uid = user.uid
            getGorev()
            top_header.setText("Hoşgeldiniz $name!")
        }else {
            Toast.makeText(this@MainActivity, "Giriş Yapmadınız!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val logoutButton = findViewById<ImageButton>(R.id.logoutBtn)
        val izinButton = findViewById<ImageButton>(R.id.izinBtn)
        val malzemeButton = findViewById<ImageButton>(R.id.malzemeBtn)
        val duyuruButton = findViewById<ImageButton>(R.id.duyuruBtn)
        val hesapYonetimButton = findViewById<ImageButton>(R.id.hesapYonetimBtn)
        val izinHakkiButton = findViewById<ImageButton>(R.id.izinHakkiBtn)
        val bilgilerimButton = findViewById<Button>(R.id.editBilgiBtn)
        val geciciGorevButton = findViewById<ImageButton>(R.id.geciciBtn)
        val aracButton = findViewById<ImageButton>(R.id.aracBtn)

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

        val popupMenuMesai = PopupMenu(this@MainActivity, mesaiBtn)
        popupMenuMesai.menuInflater.inflate(R.menu.popup_menu, popupMenuMesai.menu)
        popupMenuMesai.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId
            if (id == R.id.adminEkran) {
                val intent = Intent(this, MesaiAdminActivity::class.java)
                intent.putExtra("Yetki","Admin")
                startActivity(intent)
            } else if (id == R.id.userEkran) {
                val intent = Intent(this, MesaiUserActivity::class.java)
                intent.putExtra("Yetki","User")
                startActivity(intent)
            }
            false
        }

        val popupMenuGeciciGorev = PopupMenu(this@MainActivity, geciciGorevButton)
        popupMenuGeciciGorev.menuInflater.inflate(R.menu.popup_menu, popupMenuGeciciGorev.menu)
        popupMenuGeciciGorev.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId
            if (id == R.id.adminEkran){
                val admin = Intent(this, GeciciGorevlendirmeAdminActivity::class.java)
                startActivity(admin)
            }else if (id == R.id.userEkran){
                val user = Intent(this, GeciciGorevlendirmeUserActivity::class.java)
                startActivity(user)
            }
            false
        }

        val popupMenuArac = PopupMenu(this@MainActivity, aracButton)
        popupMenuArac.menuInflater.inflate(R.menu.popup_menu, popupMenuArac.menu)
        popupMenuArac.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId
            if (id == R.id.adminEkran){
                val admin = Intent(this, AracKullanimAdmin::class.java)
                startActivity(admin)
            }else if (id == R.id.userEkran){
                val user = Intent(this, AracKullanimUser::class.java)
                startActivity(user)
            }
            false
        }

        logoutButton.setOnClickListener{
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
                        if(gorev == "INSAN KAYNAKLARI" || gorev == "YONETICI"){
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

        geciciGorevButton.setOnClickListener {
            myRef.child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val imzaDurum = it.child("imzaExist").value.toString()
                    Log.w("test","$imzaDurum")
                    if(imzaDurum == "1"){
                        if(gorev == "INSAN KAYNAKLARI" || gorev == "YONETICI"){
                            popupMenuGeciciGorev.show()
                        }else{
                            val intent = Intent(this, GeciciGorevlendirmeUserActivity::class.java)
                            startActivity(intent)
                        }
                    }else{
                        Toast.makeText(this@MainActivity, "Bu Özelliği Kullanmak İçin İmza Oluşturmanız Gerekiyor! Bilgilerim Sayfasında Ulaşabilirsiniz!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        aracButton.setOnClickListener {
            myRef.child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val imzaDurum = it.child("imzaExist").value.toString()
                    Log.w("test","$imzaDurum")
                    if(imzaDurum == "1"){
                        if(gorev == "INSAN KAYNAKLARI"){
                            popupMenuArac.show()
                        }else{
                            val intent = Intent(this, AracKullanimUser::class.java)
                            startActivity(intent)
                        }
                    }else{
                        Toast.makeText(this@MainActivity, "Bu Özelliği Kullanmak İçin İmza Oluşturmanız Gerekiyor! Bilgilerim Sayfasında Ulaşabilirsiniz!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        malzemeButton.setOnClickListener {
            if(gorev == "INSAN KAYNAKLARI" || gorev == "YONETICI"){
                popupMenuMalzeme.show()
            }else{
                val user = Intent(this, MalzemeUserActivity::class.java)
                startActivity(user)
            }
        }

        mesaiBtn.setOnClickListener{
            myRef.child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val imzaDurum = it.child("imzaExist").value.toString()
                    Log.w("test","$imzaDurum")
                    if(imzaDurum == "1"){
                        if(gorev == "INSAN KAYNAKLARI" || gorev == "YONETICI"){
                            popupMenuMesai.show()
                        }else{
                            val user = Intent(this, MesaiUserActivity::class.java)
                            startActivity(user)
                        }
                    }else{
                        Toast.makeText(this@MainActivity, "Bu Özelliği Kullanmak İçin İmza Oluşturmanız Gerekiyor! Bilgilerim Sayfasında Ulaşabilirsiniz!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        duyuruButton.setOnClickListener {
            val intent = Intent(this, DuyuruActivity::class.java)
            startActivity(intent)
        }

        hesapYonetimButton.setOnClickListener {
            if(gorev == "INSAN KAYNAKLARI"){
                val intent = Intent(this,AccountManagementActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this@MainActivity, "Bu sayfaya sadece adminler erişebilir!!", Toast.LENGTH_SHORT).show()
            }
        }

        izinHakkiButton.setOnClickListener{
            if(gorev == "INSAN KAYNAKLARI"){
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
                        if(gorev == "INSAN KAYNAKLARI"){
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