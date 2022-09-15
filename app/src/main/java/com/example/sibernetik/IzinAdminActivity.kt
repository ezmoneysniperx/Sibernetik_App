package com.example.sibernetik

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import fcm.androidtoandroid.FirebasePush
import fcm.androidtoandroid.model.Notification
import kotlinx.android.synthetic.main.izin_admin_activity.*
import kotlinx.android.synthetic.main.izin_admin_activity.recyclerview
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import com.example.sibernetik.R.drawable.*

class IzinAdminActivity : AppCompatActivity(), CustomAdapter.OnItemClickListener {

    val data = ArrayList<ItemsViewModel>()
    val adapter = CustomAdapter(data, this)

    var adsoyad = ""
    var nedeni = ""
    var tarih = ""
    var durum = ""
    var mesaj = ""
    var izinId = ""
    var yonetici1onay = ""
    var yonetici2onay = ""

    var serverKey = "serverkey"
    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Izin").child("Izin Gunluk")
    val myRefUser = database.getReference("Users")
    private lateinit var auth: FirebaseAuth

    var email = ""
    var emailIzinPerson = ""
    var gorev = ""
    var uid = ""
    var userAdSoyad = ""

    var bastarih = ""
    var bittarih = ""

    var spinnerSelItem = ""

    var userUid = ""

    val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.izin_admin_activity)

        getNameIzin()
        getGorevIzin()

        auth = Firebase.auth
        val user = Firebase.auth.currentUser
        userUid = user!!.uid

        val arrayDurum = resources.getStringArray(R.array.izin_durum)

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        //showCommentsAdmin()

        val spinner = findViewById<Spinner>(R.id.spinnerIzinGunluk)
        if (spinner != null) {
            val adapterArray = ArrayAdapter(
                this,
                R.layout.spinner_list, arrayDurum
            )
            adapterArray.setDropDownViewResource(R.layout.spinner_list)
            spinner.adapter = adapterArray
        }

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ){
                spinnerSelItem = arrayDurum[position]
                filtreleme(spinnerSelItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }

        val araBtn = findViewById<Button>(R.id.araBtn)
        val onayBtn = findViewById<Button>(R.id.btnOnay)
        val reddetBtn = findViewById<Button>(R.id.btnReddet)
        val anasayfaAdminGunlukBtn = findViewById<ImageButton>(R.id.anasayfaIzinAdminGunlukBtn)

        araBtn.setOnClickListener {
            val adSoyad = adAraTxt.text.toString()
            searchIzinAdmin(adSoyad)
        }

        onayBtn.setOnClickListener {
            izinAccepted(izinId)
        }

        reddetBtn.setOnClickListener {
            izinDeclined(izinId)
        }

        btnTemizle.setOnClickListener {
            adsoyad = ""
            nedeni = ""
            tarih = ""
            yonetici1onay = ""
            yonetici2onay = ""
            izinId = ""
            durum = ""
            emailIzinPerson = ""

            adsoyadDisplay.setText("-")
            nedeniDisplay.setText("-")
            tarDisplay.setText("-")
            durumDisplay.setText("-")

            btnOnay.visibility = View.INVISIBLE
            btnReddet.visibility = View.INVISIBLE
            btnTemizle.visibility = View.INVISIBLE
            btnGunlukSil.visibility = View.INVISIBLE
        }

        btnGunlukSil.setOnClickListener {
            if(izinId.isEmpty()){
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Herhangi bir izin seçmediniz!")
                builder.setNeutralButton("Tamam"){dialogInterface , which -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("İzin Talebi silinecektir! Emin misiniz?")
                builder.setPositiveButton("Tamam"){dialogInterface , which ->
                    myRef.child(izinId).removeValue()
                    Toast.makeText(this, "İzin Talebi Silindi!", Toast.LENGTH_SHORT).show()
                    finish()
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                }
                builder.setNegativeButton("Iptal"){dialogInterface , which ->
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }

        anasayfaAdminGunlukBtn.setOnClickListener {
            val intent = Intent(this,IzinMenuActivity::class.java)
            intent.putExtra("Yetki","Admin")
            startActivity(intent)
            finish()
        }
    }

    override fun onItemClick(position: Int) {
        val clickedItem:ItemsViewModel = data[position]

        adsoyad = clickedItem.text.toString()
        nedeni = clickedItem.nedeni.toString()
        tarih = clickedItem.date.toString()
        yonetici1onay = clickedItem.yonetici1onay.toString()
        yonetici2onay = clickedItem.yonetici2onay.toString()
        izinId = clickedItem.id.toString()

        if(yonetici1onay == "ONAYLANDI" && yonetici2onay == "ONAYLANDI"){
            durum = "ONAYLANDI"
        }else if(yonetici2onay == "ONAY BEKLIYOR"){
            durum = "ONAY BEKLIYOR"
        }else if(yonetici1onay == "ONAY BEKLIYOR"){
            durum = "ONAY BEKLIYOR"
        }else if(yonetici1onay == "REDDETTI" || yonetici2onay == "REDDETTI"){
            durum = "REDDETTI"
        }

        myRefUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if (value!!.adSoyad == adsoyad){
                        emailIzinPerson = value!!.ePosta.toString()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        adsoyadDisplay.setText(adsoyad)
        nedeniDisplay.setText(nedeni)
        tarDisplay.setText(tarih)
        durumDisplay.setText(durum)

        btnOnay.visibility = View.VISIBLE
        btnReddet.visibility = View.VISIBLE
        btnTemizle.visibility = View.VISIBLE
        btnGunlukSil.visibility = View.VISIBLE
    }

    fun searchIzinAdmin(name : String){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                for( postSnapshot in dataSnapshot.children ){
                    var value = postSnapshot.getValue<IzinModel>()
                    if( value!!.adsoyad == name ){
                        if (value!!.yonetici1 == "REDDETTI" && value!!.yonetici2 == "REDDETTI"){
                            val basTrh = value!!.bastarih.toString()
                            val bitTrh = value!!.bittarih.toString()
                            val tarih = "$basTrh - $bitTrh"
                            val izinGun = value!!.day.toString()
                            val izinTip = value!!.izinTipi.toString()
                            val izinMaz = value!!.izinMazeret.toString()

                            data.add(ItemsViewModel(
                                R.drawable.reddeti,
                                value!!.adsoyad.toString(),
                                value!!.sebeb.toString(),
                                tarih,
                                "",
                                value!!.yonetici1.toString(),
                                value!!.yonetici2.toString(),
                                value!!.mesaj.toString(),
                                value!!.izinId.toString(),
                                "$izinGun Gün",
                                izinTip,
                                izinMaz
                            ))
                        }else if (gorev == "YONETICI" && value!!.yonetici1 == "ONAY BEKLIYOR"){
                            val basTrh = value!!.bastarih.toString()
                            val bitTrh = value!!.bittarih.toString()
                            val tarih = "$basTrh - $bitTrh"
                            val izinGun = value!!.day.toString()
                            val izinTip = value!!.izinTipi.toString()
                            val izinMaz = value!!.izinMazeret.toString()

                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value!!.adsoyad.toString(),
                                value!!.sebeb.toString(),
                                tarih,
                                "",
                                value!!.yonetici1.toString(),
                                value!!.yonetici2.toString(),
                                value!!.mesaj.toString(),
                                value!!.izinId.toString(),
                                "$izinGun Gün",
                                izinTip,
                                izinMaz
                            ))
                        }else if ( value!!.yonetici1 == "ONAYLANDI" && value!!.yonetici2 == "ONAYLANDI"){
                            val basTrh = value!!.bastarih.toString()
                            val bitTrh = value!!.bittarih.toString()
                            val tarih = "$basTrh - $bitTrh"
                            val izinGun = value!!.day.toString()
                            val izinTip = value!!.izinTipi.toString()
                            val izinMaz = value!!.izinMazeret.toString()

                            data.add(ItemsViewModel(
                                R.drawable.onaylandi,
                                value!!.adsoyad.toString(),
                                value!!.sebeb.toString(),
                                tarih,
                                "",
                                value!!.yonetici1.toString(),
                                value!!.yonetici2.toString(),
                                value!!.mesaj.toString(),
                                value!!.izinId.toString(),
                                "$izinGun Gün",
                                izinTip,
                                izinMaz
                            ))
                        }else if( gorev == "INSAN KAYNAKLAR" && value!!.yonetici2 == "ONAYLANDI"){
                            val basTrh = value!!.bastarih.toString()
                            val bitTrh = value!!.bittarih.toString()
                            val tarih = "$basTrh - $bitTrh"
                            val izinGun = value!!.day.toString()
                            val izinTip = value!!.izinTipi.toString()
                            val izinMaz = value!!.izinMazeret.toString()

                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value!!.adsoyad.toString(),
                                value!!.sebeb.toString(),
                                tarih,
                                "",
                                value!!.yonetici1.toString(),
                                value!!.yonetici2.toString(),
                                value!!.mesaj.toString(),
                                value!!.izinId.toString(),
                                "$izinGun Gün",
                                izinTip,
                                izinMaz
                            ))
                        }else if ( gorev == "INSAN KAYNAKLAR" && value!!.yonetici2 != "ONAYLANDI"){
                            continue
                        }else {
                            val basTrh = value!!.bastarih.toString()
                            val bitTrh = value!!.bittarih.toString()
                            val tarih = "$basTrh - $bitTrh"
                            val izinGun = value!!.day.toString()
                            val izinTip = value!!.izinTipi.toString()
                            val izinMaz = value!!.izinMazeret.toString()

                            data.add(ItemsViewModel(
                                R.drawable.reddeti,
                                value!!.adsoyad.toString(),
                                value!!.sebeb.toString(),
                                tarih,
                                "",
                                value!!.yonetici1.toString(),
                                value!!.yonetici2.toString(),
                                value!!.mesaj.toString(),
                                value!!.izinId.toString(),
                                "$izinGun Gün",
                                izinTip,
                                izinMaz
                            ))
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun filtreleme(durum : String){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                recyclerview.adapter = adapter
                for(postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<IzinModel>()
                    if(gorev == "YONETICI"){
                        if (durum == "ONAY BEKLIYOR"){
                            if(value!!.yonetici2 == "ONAY BEKLIYOR"){
                                val basTrh = value!!.bastarih.toString()
                                val bitTrh = value!!.bittarih.toString()
                                val tarih = "$basTrh - $bitTrh"
                                val izinGun = value!!.day.toString()
                                val izinTip = value!!.izinTipi.toString()
                                val izinMaz = value!!.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.bekleme,
                                    value!!.adsoyad.toString(),
                                    value!!.sebeb.toString(),
                                    tarih,
                                    "",
                                    value!!.yonetici1.toString(),
                                    value!!.yonetici2.toString(),
                                    value!!.mesaj.toString(),
                                    value!!.izinId.toString(),
                                    "$izinGun Gün",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }else if (durum == "ONAYLANDI"){
                            if(value!!.yonetici1 == "ONAYLANDI" && value!!.yonetici2 == "ONAYLANDI"){
                                val basTrh = value!!.bastarih.toString()
                                val bitTrh = value!!.bittarih.toString()
                                val tarih = "$basTrh - $bitTrh"
                                val izinGun = value!!.day.toString()
                                val izinTip = value!!.izinTipi.toString()
                                val izinMaz = value!!.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.onaylandi,
                                    value!!.adsoyad.toString(),
                                    value!!.sebeb.toString(),
                                    tarih,
                                    "",
                                    value!!.yonetici1.toString(),
                                    value!!.yonetici2.toString(),
                                    value!!.mesaj.toString(),
                                    value!!.izinId.toString(),
                                    "$izinGun Gün",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }else{
                            if(value!!.yonetici2 == "REDDETTI"){
                                val basTrh = value!!.bastarih.toString()
                                val bitTrh = value!!.bittarih.toString()
                                val tarih = "$basTrh - $bitTrh"
                                val izinGun = value!!.day.toString()
                                val izinTip = value!!.izinTipi.toString()
                                val izinMaz = value!!.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.reddeti,
                                    value!!.adsoyad.toString(),
                                    value!!.sebeb.toString(),
                                    tarih,
                                    "",
                                    value!!.yonetici1.toString(),
                                    value!!.yonetici2.toString(),
                                    value!!.mesaj.toString(),
                                    value!!.izinId.toString(),
                                    "$izinGun Gün",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }
                    }else if (gorev == "INSAN KAYNAKLAR"){
                        if (durum == "ONAY BEKLIYOR"){
                            if(value!!.yonetici1 == "ONAY BEKLIYOR" && value!!.yonetici2 == "ONAYLANDI"){
                                val basTrh = value!!.bastarih.toString()
                                val bitTrh = value!!.bittarih.toString()
                                val tarih = "$basTrh - $bitTrh"
                                val izinGun = value!!.day.toString()
                                val izinTip = value!!.izinTipi.toString()
                                val izinMaz = value!!.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.bekleme,
                                    value!!.adsoyad.toString(),
                                    value!!.sebeb.toString(),
                                    tarih,
                                    "",
                                    value!!.yonetici1.toString(),
                                    value!!.yonetici2.toString(),
                                    value!!.mesaj.toString(),
                                    value!!.izinId.toString(),
                                    "$izinGun Gün",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }else if (durum == "ONAYLANDI"){
                            if(value!!.yonetici1 == "ONAYLANDI" && value!!.yonetici2 == "ONAYLANDI"){
                                val basTrh = value!!.bastarih.toString()
                                val bitTrh = value!!.bittarih.toString()
                                val tarih = "$basTrh - $bitTrh"
                                val izinGun = value!!.day.toString()
                                val izinTip = value!!.izinTipi.toString()
                                val izinMaz = value!!.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.onaylandi,
                                    value!!.adsoyad.toString(),
                                    value!!.sebeb.toString(),
                                    tarih,
                                    "",
                                    value!!.yonetici1.toString(),
                                    value!!.yonetici2.toString(),
                                    value!!.mesaj.toString(),
                                    value!!.izinId.toString(),
                                    "$izinGun Gün",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }else{
                            if(value!!.yonetici1 == "REDDETTI" || value!!.yonetici2 == "REDDETTI"){
                                val basTrh = value!!.bastarih.toString()
                                val bitTrh = value!!.bittarih.toString()
                                val tarih = "$basTrh - $bitTrh"
                                val izinGun = value!!.day.toString()
                                val izinTip = value!!.izinTipi.toString()
                                val izinMaz = value!!.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.reddeti,
                                    value!!.adsoyad.toString(),
                                    value!!.sebeb.toString(),
                                    tarih,
                                    "",
                                    value!!.yonetici1.toString(),
                                    value!!.yonetici2.toString(),
                                    value!!.mesaj.toString(),
                                    value!!.izinId.toString(),
                                    "$izinGun Gün",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun izinAccepted (id : String){
        var izinSayisi = 0
        durum = "ONAYLANDI"
        mesaj = izinMesaj.text.toString()
        if (mesaj.isEmpty()) { mesaj = "-" }


        myRefUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if(value!!.ePosta == emailIzinPerson){
                        izinSayisi = value!!.izin!!.toInt()
                        uid = postSnapshot.key.toString()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        if(id.isEmpty()){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Herhangi bir izin seçmediniz!")
            builder.setNeutralButton("Tamam"){dialogInterface , which -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else{
            if (yonetici1onay == "ONAYLANDI" && yonetici2onay == "ONAYLANDI") {
                Toast.makeText(this, "Izin daha önce onaylandi!", Toast.LENGTH_SHORT).show()
            }else if(gorev == "YONETICI"){
                myRefUser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(postSnapshot in snapshot.children){
                            var value = postSnapshot.getValue<UsersModel>()
                            if (value!!.adSoyad == adsoyad){
                                if (value!!.yonetici == userAdSoyad){
                                    myRef.child(id).child("yonetici2").setValue(durum)
                                    myRef.child(id).child("mesaj").setValue(mesaj)
                                    myRef.child(id).child("yoneticiId").setValue(userUid)
                                    //notifikasi//
                                    var icon = logo
                                    val iconString = icon.toString()
                                    val notification = Notification()
                                    notification.title = "Yeni İzin Talebi Var"
                                    notification.body = "$adsoyad adlı kişi $tarih tarihi için izin talebi göndermişti"
                                    notification.icon = iconString
                                    val firebasePush = FirebasePush.build(serverKey)
                                        .setNotification(notification)
                                        .setOnFinishPush {  }
                                    firebasePush.sendToTopic("IK")
                                    //notifikasi//
                                    Toast.makeText(this@IzinAdminActivity, "Onaylama islemi basarili!", Toast.LENGTH_SHORT).show()
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity(getIntent());
                                    overridePendingTransition(0, 0);
                                }else{
                                    Toast.makeText(this@IzinAdminActivity, "Kabul etmeye çalıştığınız izin personelinize ait değil!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }else{
                myRef.child(id).get().addOnSuccessListener {
                    if (it.exists()){
                        bastarih = it.child("bastarih").value.toString()
                        bittarih = it.child("bittarih").value.toString()
                        val yoneticiUid = it.child("yoneticiId").value.toString()
                        val format = SimpleDateFormat("dd-MM-yyyy")
                        val days = TimeUnit.DAYS.convert(
                            format.parse(bittarih).getTime() -
                                    format.parse(bastarih).getTime(),
                            TimeUnit.MILLISECONDS)
                        Log.w("test","$izinSayisi -- $days")
                        izinSayisi = izinSayisi - days.toInt()
                        myRef.child(id).child("yonetici1").setValue(durum)
                        myRef.child(id).child("mesaj").setValue(mesaj)
                        myRef.child(id).child("ikId").setValue(userUid)
                        myRefUser.child(uid).child("izin").setValue(izinSayisi)
                        var bolum = ""
                        var tckn = ""
                        myRefUser.child(uid).get().addOnSuccessListener {
                            if(it.exists()){
                                bolum = it.child("bolum").value.toString()
                                tckn = it.child("tckn").value.toString()
                                //notifikasi//
                                var icon = logo
                                val iconString = icon.toString()
                                val notification = Notification()
                                notification.title = "İzin Talebiniz Onaylandı"
                                notification.body = "$bastarih - $bittarih İzin Talebiniz Onaylandı!"
                                notification.icon = iconString
                                val firebasePush = FirebasePush.build(serverKey)
                                    .setNotification(notification)
                                    .setOnFinishPush {  }
                                firebasePush.sendToTopic("$uid")
                                //notifikasi//
                                printPdfGunluk(id, bolum, tckn, uid, yoneticiUid, userUid)
                                Toast.makeText(this, "Onaylama islemi basarili! Kişinin Kalan izin hakkı $izinSayisi == $adsoyad", Toast.LENGTH_SHORT).show()
                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    fun izinDeclined(id : String){
        durum = "REDDETTI"
        mesaj = izinMesaj.text.toString()

        if(id.isEmpty()){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Herhangi bir izin seçmediniz!")
            builder.setNeutralButton("Tamam"){dialogInterface , which -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else{
            myRefUser.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children){
                        var value = postSnapshot.getValue<UsersModel>()
                        if(value!!.ePosta == emailIzinPerson){
                            uid = postSnapshot.key.toString()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@IzinAdminActivity, "HATA OLUSTU! Lutfen Tekrar Deneyin", Toast.LENGTH_SHORT).show()
                }
            })
            if (yonetici1onay == "REDDETTI" && yonetici2onay == "REDDETTI") {
                Toast.makeText(this, "Izin daha önce reddedildi!", Toast.LENGTH_SHORT).show()
            }else if(gorev == "YONETICI"){
                myRefUser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(postSnapshot in snapshot.children){
                            var value = postSnapshot.getValue<UsersModel>()
                            if (value!!.adSoyad == adsoyad){
                                if (value!!.yonetici == userAdSoyad){
                                    myRef.child(id).child("yonetici2").setValue(durum)
                                    myRef.child(id).child("mesaj").setValue(mesaj)
                                    //notifikasi//
                                    var icon = logo
                                    val iconString = icon.toString()
                                    val notification = Notification()
                                    notification.title = "İzin Talebiniz Reddedildi"
                                    notification.body = "$tarih İzin Talebiniz Reddedildi!"
                                    notification.icon = iconString
                                    val firebasePush = FirebasePush.build(serverKey)
                                        .setNotification(notification)
                                        .setOnFinishPush {  }
                                    firebasePush.sendToTopic("$uid")
                                    //notifikasi//
                                    Toast.makeText(this@IzinAdminActivity, "Reddetme islemi basarili!", Toast.LENGTH_SHORT).show()
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity(getIntent());
                                    overridePendingTransition(0, 0);
                                }else{
                                    Toast.makeText(this@IzinAdminActivity, "Reddet etmeye çalıştığınız izin personelinize ait değil!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }else{
                myRef.child(id).child("yonetici1").setValue(durum)
                myRef.child(id).child("mesaj").setValue(mesaj)
                //notifikasi//
                val icon = R.drawable.logo
                val iconString = icon.toString()
                val notification = Notification()
                notification.title = "İzin Talebiniz Reddedildi"
                notification.body = "$tarih İzin Talebiniz Reddedildi!"
                notification.icon = iconString
                val firebasePush = FirebasePush.build(serverKey)
                    .setNotification(notification)
                    .setOnFinishPush {  }
                firebasePush.sendToTopic("$uid")
                //notifikasi//
                Toast.makeText(this@IzinAdminActivity, "Reddetme islemi basarili!", Toast.LENGTH_SHORT).show()
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        }
    }

    fun getGorevIzin(){
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
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getNameIzin() {
        val user = Firebase.auth.currentUser
        user?.let {
            for (profile in it.providerData) {
                userAdSoyad = profile.displayName.toString()
            }
        }
    }

    fun printPdfGunluk(izinId : String, bolum : String, tckn : String, talepEdenUid : String, yoneticiUid : String, ikUid : String){
        val storageRef = storage.reference
        val getTalepEdenFileRef = storageRef.child("imza_gorseller/$talepEdenUid.jpg")
        val localfileTalepEden = File.createTempFile("tempImg","jpg")
        val getYoneticiFileRef = storageRef.child("imza_gorseller/$yoneticiUid.jpg")
        val localfileYonetici = File.createTempFile("tempImg","jpg")
        val getIkFileRef = storageRef.child("imza_gorseller/$ikUid.jpg")
        val localfileIk = File.createTempFile("tempImg","jpg")

        val task1 = getTalepEdenFileRef.getFile(localfileTalepEden)
        val task2 = getYoneticiFileRef.getFile(localfileYonetici)
        val task3 = getIkFileRef.getFile(localfileIk)

        var talepEdenBitmap : Bitmap
        var yoneticiBitmap : Bitmap
        var ikBitmap : Bitmap

        Tasks.whenAll(task1,task2,task3).addOnSuccessListener {
            talepEdenBitmap = BitmapFactory.decodeFile(localfileTalepEden.absolutePath)
            yoneticiBitmap = BitmapFactory.decodeFile(localfileYonetici.absolutePath)
            ikBitmap = BitmapFactory.decodeFile(localfileIk.absolutePath)

            myRef.child(izinId).get().addOnSuccessListener {
                if(it.exists()){
                    val izinDetailsList = listOf(
                        IzinDetails("İzin Tipi", "Günlük İzin"),
                        IzinDetails("İzin ID", it.child("izinId").value.toString()),
                        IzinDetails(" ", " "),
                        IzinDetails("Bölüm", bolum),
                        IzinDetails("TCKN", tckn),
                        IzinDetails("İzin Başlama Tarihi", it.child("bastarih").value.toString()),
                        IzinDetails("İzin Bitiş Tarihi", it.child("bittarih").value.toString()),
                        IzinDetails("Ücretli / Ücretsiz", it.child("izinTipi").value.toString()),
                        IzinDetails("İzin Nedeni", it.child("izinMazeret").value.toString()),
                        IzinDetails("İzin Nedeni (Açıklama)", it.child("sebeb").value.toString()),
                        IzinDetails(" ", " "),
                        IzinDetails("Yönetici Onayı", it.child("yonetici2").value.toString()),
                        IzinDetails("İnsan Kaynaklar Onayı", it.child("yonetici1").value.toString()),
                        IzinDetails("Mesaj", it.child("mesaj").value.toString()),
                    )
                    val pdfDetails = PdfDetails(it.child("adsoyad").value.toString(), izinDetailsList, talepEdenBitmap,yoneticiBitmap,ikBitmap)
                    val pdfConverter = PDFConverter()
                    pdfConverter.createPdf(this, pdfDetails, this, izinId,"gunluk")
                }
            }.addOnFailureListener {
                Log.w("HATA TASK bitmap", "error bro")
            }
        }
    }
}