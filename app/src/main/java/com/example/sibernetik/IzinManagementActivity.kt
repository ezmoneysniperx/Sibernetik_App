package com.example.sibernetik

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import fcm.androidtoandroid.FirebasePush
import fcm.androidtoandroid.model.Notification
import kotlinx.android.synthetic.main.izin_admin_time.*
import kotlinx.android.synthetic.main.izin_management.*
import kotlinx.android.synthetic.main.izin_management.recyclerview
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class IzinManagementActivity : AppCompatActivity(), DuyuruAdapter.OnItemClickListener {
    val data = ArrayList<DuyuruViewModel>()
    val adapter = DuyuruAdapter(data,this)

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    var serverKey = "serverkey"
    //private lateinit var auth: FirebaseAuth

    var adsoyadIzinY = ""
    var iseBaslangic = ""
    var kalanIzinHakki = ""
    var calistigiYil = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.izin_management)

        autoUpdateIzin()

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        showIzin()

        btnIzinModify.setOnClickListener {
            var yeniIzinSayisi = yeniIzinHakkiTxt.text.toString()
            if (yeniIzinSayisi.isEmpty()){
                showMessage("Yeni Izin Sayisi Bos Birakilmaz!","Tamam")
            }
            else{
                val yeniIzinSayisiInt = yeniIzinSayisi.toInt()
                updateIzin(adsoyadIzinY,yeniIzinSayisiInt)
            }

        }

        btnIzinTemizle.setOnClickListener {
            adSoyadIzinYDisplay.setText("-")
            iseBasIzinYDisplay.setText("-")
            KalanIzinYDisplay.setText("-")
            CalistigiYilIzinYDisplay.setText("-")

            adsoyadIzinY = ""
            iseBaslangic = ""
            kalanIzinHakki = ""
            calistigiYil = ""

            btnIzinModify.visibility = View.INVISIBLE
            yeniIzinHakkiTxt.visibility = View.INVISIBLE
            textView17.visibility = View.INVISIBLE
            btnIzinTemizle.visibility = View.INVISIBLE
        }

        val anasayfaManagementBtn = findViewById<ImageButton>(R.id.anasayfaIzinManagementBtn)
        anasayfaManagementBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onItemClick(position: Int) {
        val clickedItem:DuyuruViewModel = data[position]

        adsoyadIzinY = clickedItem.title
        iseBaslangic = clickedItem.date
        kalanIzinHakki = clickedItem.text

        iseBaslangic = iseBaslangic.takeLast(10)
        kalanIzinHakki = kalanIzinHakki.takeLast(2)

        val format = SimpleDateFormat("dd-MM-yyyy")
        val currentDate = format.format(Date())
        val gunler = TimeUnit.DAYS.convert(
            format.parse(currentDate).getTime() -
                    format.parse(iseBaslangic).getTime(),
            TimeUnit.MILLISECONDS)
        val yil = gunler/365

        calistigiYil = "$yil Yıl"

        adSoyadIzinYDisplay.setText(adsoyadIzinY)
        iseBasIzinYDisplay.setText(iseBaslangic)
        KalanIzinYDisplay.setText(kalanIzinHakki)
        CalistigiYilIzinYDisplay.setText(calistigiYil)

        btnIzinModify.visibility = View.VISIBLE
        yeniIzinHakkiTxt.visibility = View.VISIBLE
        textView17.visibility = View.VISIBLE
        btnIzinTemizle.visibility = View.VISIBLE
    }

    fun autoUpdateIzin() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    val format = SimpleDateFormat("dd-MM-yyyy")
                    val currentDate = format.format(Date())
                    var dayWorked = TimeUnit.DAYS.convert(
                        format.parse(currentDate).getTime() -
                                format.parse(value!!.tarih.toString()).getTime(),
                        TimeUnit.MILLISECONDS)
                    if (value!!.isUpdated.toString() == "0"){
                        Log.w("HATAACCManagementONAY", value!!.adSoyad.toString())
                        Log.w("HATAACCManagementONAY", dayWorked.toString())
                        if (dayWorked.toInt() == 366 || dayWorked.toInt() == 731 || dayWorked.toInt() == 1096 || dayWorked.toInt() == 1461 || dayWorked.toInt() == 1826){
                            Log.w("1-5", value!!.adSoyad.toString())
                            var uid = postSnapshot.key.toString()
                            val newIzin = value!!.izin!!.toInt() + 14
                            myRef.child(uid).child("updated").setValue(1)
                            myRef.child(uid).child("izin").setValue(newIzin)
                            var nama = value!!.adSoyad.toString()
                            showMessage("$nama İzin Hakkını Otomatık Olarak $newIzin'a Güncelledi!","Tamam")
                            break
                        }else if(dayWorked.toInt() == 2191){
                            Log.w(">5", value!!.adSoyad.toString())
                            var uid = postSnapshot.key.toString()
                            val newIzin = value!!.izin!!.toInt() + 20
                            myRef.child(uid).child("updated").setValue(1)
                            myRef.child(uid).child("izin").setValue(newIzin)
                            var nama = value!!.adSoyad.toString()
                            showMessage("$nama İzin Hakkını Otomatık Olarak $newIzin'a Güncelledi!","Tamam")
                            break
                        }
                    }else if (value!!.isUpdated.toString() == "1"){
                        Log.w("HATAACCManagementONAY", value!!.adSoyad.toString())
                        Log.w("HATAACCManagementONAY", dayWorked.toString())
                        if (dayWorked.toInt() == 367 || dayWorked.toInt() == 732 || dayWorked.toInt() == 1097 || dayWorked.toInt() == 1462 || dayWorked.toInt() == 1827){
                            var uid = postSnapshot.key.toString()
                            myRef.child(uid).child("updated").setValue(0)
                        }else if(dayWorked.toInt() == 2192){
                            var uid = postSnapshot.key.toString()
                            myRef.child(uid).child("updated").setValue(0)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                showMessage("Hata Oluştu!","Tamam")
            }
        })
    }

    fun showIzin() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                recyclerview.adapter = adapter
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    var tarih = value!!.tarih.toString()
                    var izin = value!!.izin.toString()
                    data.add(
                        DuyuruViewModel(
                        0,
                            value!!.adSoyad.toString(),
                            "Işe Başlangıç Tarihi : $tarih",
                            "Kalan İzin Hakkı (Gün) : $izin"
                    ))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showMessage("Hata Oluştu!","Tamam")
            }
        })
    }

    fun updateIzin(adsoyad : String, izinSayisi : Int){
        var count = 1
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    val value = postSnapshot.getValue<UsersModel>()
                    if (value!!.adSoyad.toString() == adsoyad && count == 1){
                        count = 0
                        val uid = postSnapshot.key.toString()
                        myRef.child(uid).child("izin").setValue(izinSayisi)
                        //notifikasi//
                        var icon = R.drawable.logo
                        val iconString = icon.toString()
                        val notification = Notification()
                        notification.title = "İzin Hakkı Güncelleme"
                        notification.body = "Kalan izin hakkınız $izinSayisi olarak güncellendi."
                        notification.icon = iconString
                        val firebasePush = FirebasePush.build(serverKey)
                            .setNotification(notification)
                            .setOnFinishPush {  }
                        firebasePush.sendToTopic("$uid")
                        //notifikasi//
                        Toast.makeText(this@IzinManagementActivity,"İşlem Başarılı!",Toast.LENGTH_SHORT).show()
                        finish()
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                showMessage("Hata Oluştu!","Tamam")
            }
        })
    }

    fun showMessage(message : String, button : String){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setNeutralButton(button){dialogInterface , which -> }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}