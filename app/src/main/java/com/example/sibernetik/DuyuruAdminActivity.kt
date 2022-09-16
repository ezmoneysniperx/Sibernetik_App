package com.example.sibernetik

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fcm.androidtoandroid.FirebasePush
import fcm.androidtoandroid.model.Notification
import kotlinx.android.synthetic.main.duyuru_admin_activity.*
import java.text.SimpleDateFormat
import java.util.*

class DuyuruAdminActivity : AppCompatActivity(){

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Duyuru")
    var serverKey = "serverkey"


    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.duyuru_admin_activity)

        val yeniDuyuru = findViewById<Button>(R.id.yeniDuyuruBtn)
        val anasayfaDuyuruAdminBtn = findViewById<ImageButton>(R.id.anasayfaDuyuruAdminBtn)

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        var formatted: String = simpleDateFormat.format(Date())
        duyuruDateTxt.setText(formatted)

        anasayfaDuyuruAdminBtn.setOnClickListener {
            val intent = Intent(this,DuyuruActivity::class.java)
            startActivity(intent)
            finish()
        }

        yeniDuyuru.setOnClickListener {
            val baslik = duyuruTitleTxt.text.toString()
            val tarih = duyuruDateTxt.text.toString()
            val icerik = duyuruTextTxt.text.toString()

            if(baslik.isEmpty() || tarih.isEmpty() || icerik.isEmpty()){
                Toast.makeText(this,"Hepsini Doldurmalısınız!",Toast.LENGTH_LONG).show()
            }else{
                saveData(baslik,tarih,icerik)
                //notifikasi//
                var icon = R.drawable.logo
                val iconString = icon.toString()
                val notification = Notification()
                notification.title = "Yeni Duyuru Var"
                notification.body = "$tarih - $baslik"
                notification.icon = iconString
                val firebasePush = FirebasePush.build(serverKey)
                    .setNotification(notification)
                    .setOnFinishPush {  }
                firebasePush.sendToTopic("allUser")
                //notifikasi//
                Toast.makeText(this,"Duyuru Paylaşıldı!",Toast.LENGTH_LONG).show()
                val intent = Intent(this, DuyuruActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    fun saveData(baslik : String, tarih : String, icerik : String) {
        val newDuyuru = DuyuruModel(baslik, tarih, icerik)
        val duyuruId = myRef.push().getKey()
        myRef.child(duyuruId.toString()).setValue(newDuyuru)
    }
}