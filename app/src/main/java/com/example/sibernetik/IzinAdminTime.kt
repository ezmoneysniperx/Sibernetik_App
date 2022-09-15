package com.example.sibernetik

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.izin_admin_time.*
import kotlinx.android.synthetic.main.izin_admin_time.recyclerview
import java.io.File

class IzinAdminTime : AppCompatActivity(), CustomAdapter.OnItemClickListener  {
    val data = ArrayList<ItemsViewModel>()
    val adapter = CustomAdapter(data, this)

    var adsoyad = ""
    var nedeni = ""
    var tarih = ""
    var saat = ""
    var durum = ""
    var mesaj = ""
    var izinId = ""
    var yonetici1onay = ""
    var yonetici2onay = ""

    var uid = ""

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Izin").child("Izin Saatlik")
    val myRefUser = database.getReference("Users")
    private lateinit var auth: FirebaseAuth
    var serverKey = "serverkey"

    //var serverKey = "serverkey"
    var email = ""
    var emailIzinPerson = ""
    var gorev = ""
    var userAdSoyad = ""

    var yonetici1durum = ""
    var yonetici2durum = ""

    var spinnerSelItem = ""

    var userUid = ""

    val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.izin_admin_time)

        getGorevIzin()
        getNameIzin()

        auth = Firebase.auth
        val user = Firebase.auth.currentUser
        userUid = user!!.uid

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        //showCommentsAdmin()

        val arrayDurum = resources.getStringArray(R.array.izin_durum)

        val spinner = findViewById<Spinner>(R.id.spinnerIzinSaatlik)
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

        val araBtn = findViewById<Button>(R.id.araTimeBtn)
        val onayBtn = findViewById<Button>(R.id.btnTimeOnay)
        val reddetBtn = findViewById<Button>(R.id.btnTimeReddet)

        araBtn.setOnClickListener {
            val adSoyad = adAraTimeTxt.text.toString()
            searchIzinAdmin(adSoyad)
        }

        onayBtn.setOnClickListener {
            izinAccepted(izinId)
        }

        reddetBtn.setOnClickListener {
            izinDeclined(izinId)
        }

        btnTimeTemizle.setOnClickListener {
            adsoyad = ""
            nedeni = ""
            tarih = ""
            saat = ""
            yonetici1onay = ""
            yonetici2onay = ""
            izinId = ""
            durum = ""

            adsoyadTimeDisplay.setText("-")
            nedeniTimeDisplay.setText("-")
            tarihiTDisplay.setText("-")
            tarTimeDisplay.setText("-")
            durumTimeDisplay.setText("-")

            btnTimeOnay.visibility = View.INVISIBLE
            btnTimeReddet.visibility = View.INVISIBLE
            btnTimeTemizle.visibility = View.INVISIBLE
            btnTimeSil.visibility = View.INVISIBLE
        }

        btnTimeSil.setOnClickListener {
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

        val anasayfaAdminTimeBtn = findViewById<ImageButton>(R.id.anasayfaIzinAdminTimeBtn)
        anasayfaAdminTimeBtn.setOnClickListener {
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
        saat = clickedItem.time.toString()
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
        adsoyadTimeDisplay.setText(adsoyad)
        nedeniTimeDisplay.setText(nedeni)
        tarihiTDisplay.setText(tarih)
        tarTimeDisplay.setText(saat)
        durumTimeDisplay.setText(durum)

        btnTimeOnay.visibility = View.VISIBLE
        btnTimeReddet.visibility = View.VISIBLE
        btnTimeTemizle.visibility = View.VISIBLE
        btnTimeSil.visibility = View.VISIBLE
    }

    fun filtreleme(durum : String){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                recyclerview.adapter = adapter
                for(postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<IzinModelSaatlik>()
                    if(gorev == "YONETICI"){
                        if (durum == "ONAY BEKLIYOR"){
                            if(value!!.yonetici2 == "ONAY BEKLIYOR"){
                                val tarih = value!!.izintarihi.toString()
                                val bassaatii = value!!.bassaati.toString()
                                val bitSaati = value!!.bitsaati.toString()
                                val saat = "$bassaatii - $bitSaati"
                                val izinTip = value!!.izinTipi.toString()
                                val izinMaz = value!!.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.bekleme,
                                    value!!.adsoyad.toString(),
                                    value!!.sebeb.toString(),
                                    tarih,
                                    saat,
                                    value!!.yonetici1.toString(),
                                    value!!.yonetici2.toString(),
                                    value!!.mesaj.toString(),
                                    value!!.izinId.toString(),
                                    "",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }else if (durum == "ONAYLANDI"){
                            if(value!!.yonetici1 == "ONAYLANDI" && value!!.yonetici2 == "ONAYLANDI"){
                                val tarih = value!!.izintarihi.toString()
                                val bassaatii = value!!.bassaati.toString()
                                val bitSaati = value!!.bitsaati.toString()
                                val saat = "$bassaatii - $bitSaati"
                                val izinTip = value!!.izinTipi.toString()
                                val izinMaz = value!!.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.onaylandi,
                                    value!!.adsoyad.toString(),
                                    value!!.sebeb.toString(),
                                    tarih,
                                    saat,
                                    value!!.yonetici1.toString(),
                                    value!!.yonetici2.toString(),
                                    value!!.mesaj.toString(),
                                    value!!.izinId.toString(),
                                    "",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }else{
                            if(value!!.yonetici2 == "REDDETTI"){
                                val tarih = value.izintarihi.toString()
                                val bassaatii = value.bassaati.toString()
                                val bitSaati = value.bitsaati.toString()
                                val saat = "$bassaatii - $bitSaati"
                                val izinTip = value.izinTipi.toString()
                                val izinMaz = value.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.reddeti,
                                    value.adsoyad.toString(),
                                    value.sebeb.toString(),
                                    tarih,
                                    saat,
                                    value.yonetici1.toString(),
                                    value.yonetici2.toString(),
                                    value.mesaj.toString(),
                                    value.izinId.toString(),
                                    "",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }
                    }else if (gorev == "INSAN KAYNAKLAR"){
                        if (durum == "ONAY BEKLIYOR"){
                            if(value!!.yonetici1 == "ONAY BEKLIYOR" && value!!.yonetici2 == "ONAYLANDI"){
                                val tarih = value.izintarihi.toString()
                                val bassaatii = value.bassaati.toString()
                                val bitSaati = value.bitsaati.toString()
                                val saat = "$bassaatii - $bitSaati"
                                val izinTip = value.izinTipi.toString()
                                val izinMaz = value.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.bekleme,
                                    value.adsoyad.toString(),
                                    value.sebeb.toString(),
                                    tarih,
                                    saat,
                                    value.yonetici1.toString(),
                                    value.yonetici2.toString(),
                                    value.mesaj.toString(),
                                    value.izinId.toString(),
                                    "",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }else if (durum == "ONAYLANDI"){
                            if(value!!.yonetici1 == "ONAYLANDI" && value!!.yonetici2 == "ONAYLANDI"){
                                val tarih = value.izintarihi.toString()
                                val bassaatii = value.bassaati.toString()
                                val bitSaati = value.bitsaati.toString()
                                val saat = "$bassaatii - $bitSaati"
                                val izinTip = value.izinTipi.toString()
                                val izinMaz = value.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.onaylandi,
                                    value.adsoyad.toString(),
                                    value.sebeb.toString(),
                                    tarih,
                                    saat,
                                    value.yonetici1.toString(),
                                    value.yonetici2.toString(),
                                    value!!.mesaj.toString(),
                                    value!!.izinId.toString(),
                                    "",
                                    izinTip,
                                    izinMaz
                                ))
                            }
                        }else{
                            if(value!!.yonetici1 == "REDDETTI" || value!!.yonetici2 == "REDDETTI"){
                                val tarih = value!!.izintarihi.toString()
                                val bassaatii = value!!.bassaati.toString()
                                val bitSaati = value!!.bitsaati.toString()
                                val saat = "$bassaatii - $bitSaati"
                                val izinTip = value!!.izinTipi.toString()
                                val izinMaz = value!!.izinMazeret.toString()

                                data.add(ItemsViewModel(
                                    R.drawable.reddeti,
                                    value!!.adsoyad.toString(),
                                    value!!.sebeb.toString(),
                                    tarih,
                                    saat,
                                    value!!.yonetici1.toString(),
                                    value!!.yonetici2.toString(),
                                    value!!.mesaj.toString(),
                                    value!!.izinId.toString(),
                                    "",
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

    fun searchIzinAdmin(name : String){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                for( postSnapshot in dataSnapshot.children ){
                    var value = postSnapshot.getValue<IzinModelSaatlik>()
                    if( value!!.adsoyad == name ){
                        val tarih = value!!.izintarihi.toString()
                        val bassaatii = value!!.bassaati.toString()
                        val bitSaati = value!!.bitsaati.toString()
                        val saat = "$bassaatii - $bitSaati"
                        val izinTip = value!!.izinTipi.toString()
                        val izinMaz = value!!.izinMazeret.toString()

                        if (value!!.yonetici1 == "REDDETTI" || value!!.yonetici2 == "REDDETTI"){
                            val tarih = value!!.izintarihi.toString()
                            val bassaatii = value!!.bassaati.toString()
                            val bitSaati = value!!.bitsaati.toString()
                            val saat = "$bassaatii - $bitSaati"
                            val izinTip = value!!.izinTipi.toString()
                            val izinMaz = value!!.izinMazeret.toString()

                            data.add(ItemsViewModel(
                                R.drawable.reddeti,
                                value!!.adsoyad.toString(),
                                value!!.sebeb.toString(),
                                tarih,
                                saat,
                                value!!.yonetici1.toString(),
                                value!!.yonetici2.toString(),
                                value!!.mesaj.toString(),
                                value!!.izinId.toString(),
                                "",
                                izinTip,
                                izinMaz
                            ))
                        }else if (gorev == "YONETICI" && value!!.yonetici1 == "ONAY BEKLIYOR"){
                            val tarih = value!!.izintarihi.toString()
                            val bassaatii = value!!.bassaati.toString()
                            val bitSaati = value!!.bitsaati.toString()
                            val saat = "$bassaatii - $bitSaati"
                            val izinTip = value!!.izinTipi.toString()
                            val izinMaz = value!!.izinMazeret.toString()

                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value!!.adsoyad.toString(),
                                value!!.sebeb.toString(),
                                tarih,
                                saat,
                                value!!.yonetici1.toString(),
                                value!!.yonetici2.toString(),
                                value!!.mesaj.toString(),
                                value!!.izinId.toString(),
                                "",
                                izinTip,
                                izinMaz
                            ))
                        }else if ( value!!.yonetici1 == "ONAYLANDI" && value!!.yonetici2 == "ONAYLANDI"){
                            val tarih = value!!.izintarihi.toString()
                            val bassaatii = value!!.bassaati.toString()
                            val bitSaati = value!!.bitsaati.toString()
                            val saat = "$bassaatii - $bitSaati"
                            val izinTip = value!!.izinTipi.toString()
                            val izinMaz = value!!.izinMazeret.toString()

                            data.add(ItemsViewModel(
                                R.drawable.onaylandi,
                                value!!.adsoyad.toString(),
                                value!!.sebeb.toString(),
                                tarih,
                                saat,
                                value!!.yonetici1.toString(),
                                value!!.yonetici2.toString(),
                                value!!.mesaj.toString(),
                                value!!.izinId.toString(),
                                "",
                                izinTip,
                                izinMaz
                            ))
                        }else if( gorev == "INSAN KAYNAKLAR" && value!!.yonetici2 == "ONAYLANDI"){
                            val tarih = value!!.izintarihi.toString()
                            val bassaatii = value!!.bassaati.toString()
                            val bitSaati = value!!.bitsaati.toString()
                            val saat = "$bassaatii - $bitSaati"
                            val izinTip = value!!.izinTipi.toString()
                            val izinMaz = value!!.izinMazeret.toString()

                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value!!.adsoyad.toString(),
                                value!!.sebeb.toString(),
                                tarih,
                                saat,
                                value!!.yonetici1.toString(),
                                value!!.yonetici2.toString(),
                                value!!.mesaj.toString(),
                                value!!.izinId.toString(),
                                "",
                                izinTip,
                                izinMaz
                            ))
                        }else if ( gorev == "INSAN KAYNAKLAR" && value!!.yonetici2 != "ONAYLANDI"){
                            continue
                        }else {
                            val tarih = value!!.izintarihi.toString()
                            val bassaatii = value!!.bassaati.toString()
                            val bitSaati = value!!.bitsaati.toString()
                            val saat = "$bassaatii - $bitSaati"
                            val izinTip = value!!.izinTipi.toString()
                            val izinMaz = value!!.izinMazeret.toString()

                            data.add(ItemsViewModel(
                                R.drawable.reddeti,
                                value!!.adsoyad.toString(),
                                value!!.sebeb.toString(),
                                tarih,
                                saat,
                                value!!.yonetici1.toString(),
                                value!!.yonetici2.toString(),
                                value!!.mesaj.toString(),
                                value!!.izinId.toString(),
                                "",
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

    fun izinAccepted (id : String){
        durum = "ONAYLANDI"
        mesaj = izinTimeMesaj.text.toString()

        if(id.isEmpty()){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Herhangi bir izin seçmediniz!")
            builder.setNeutralButton("Tamam"){dialogInterface , which -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else{
            if (yonetici1durum == "ONAYLANDI" && yonetici2durum == "ONAYLANDI") {
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
                                    var icon = R.drawable.logo
                                    val iconString = icon.toString()
                                    val notification = Notification()
                                    notification.title = "Yeni Saatlık İzin Talebi Var"
                                    notification.body = "$adsoyad adlı kişi $tarih tarihi için saatlık izin talebi göndermişti"
                                    notification.icon = iconString
                                    val firebasePush = FirebasePush.build(serverKey)
                                        .setNotification(notification)
                                        .setOnFinishPush {  }
                                    firebasePush.sendToTopic("IK")
                                    //notifikasi//
                                    Toast.makeText(this@IzinAdminTime, "Onaylama islemi basarili!", Toast.LENGTH_SHORT).show()
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity(getIntent());
                                    overridePendingTransition(0, 0);
                                }else{
                                    Toast.makeText(this@IzinAdminTime, "Kabul etmeye çalıştığınız izin personelinize ait değil!", Toast.LENGTH_SHORT).show()
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
                myRef.child(id).child("ikId").setValue(userUid)
                myRef.child(id).get().addOnSuccessListener {
                    if(it.exists()){
                        val yoneticiUid = it.child("yoneticiId").value.toString()

                        myRefUser.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (postSnapshot in snapshot.children){
                                    var value = postSnapshot.getValue<UsersModel>()
                                    if(value!!.ePosta == emailIzinPerson){
                                        uid = postSnapshot.key.toString()
                                        var bolum = ""
                                        var tckn = ""
                                        myRefUser.child(uid).get().addOnSuccessListener {
                                            if(it.exists()){
                                                bolum = it.child("bolum").value.toString()
                                                tckn = it.child("tckn").value.toString()
                                                //notifikasi//
                                                var icon = R.drawable.logo
                                                val iconString = icon.toString()
                                                val notification = Notification()
                                                notification.title = "Saatlık İzin Talebiniz Onaylandı"
                                                notification.body = "$tarih Saatlık İzin Talebiniz Onaylandı!"
                                                notification.icon = iconString
                                                val firebasePush = FirebasePush.build(serverKey)
                                                    .setNotification(notification)
                                                    .setOnFinishPush {  }
                                                firebasePush.sendToTopic("$uid")
                                                //notifikasi//
                                                printPdfSaatlik(id, bolum, tckn, uid, yoneticiUid, userUid)
                                                Toast.makeText(this@IzinAdminTime, "Onaylama islemi basarili!", Toast.LENGTH_SHORT).show()
                                                finish();
                                                overridePendingTransition(0, 0);
                                                startActivity(getIntent());
                                                overridePendingTransition(0, 0);
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
                }
            }
        }
    }

    fun izinDeclined(id : String){
        durum = "REDDETTI"
        mesaj = izinTimeMesaj.text.toString()

        if(id.isEmpty()){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Herhangi bir izin seçmediniz!")
            builder.setNeutralButton("Tamam"){dialogInterface , which -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else{
            if (yonetici1durum == "REDDETTI" && yonetici2durum == "REDDETTI") {
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
                                    uid = postSnapshot.key.toString()
                                    //notifikasi//
                                    var icon = R.drawable.logo
                                    val iconString = icon.toString()
                                    val notification = Notification()
                                    notification.title = "Saatlık İzin Talebiniz Reddedildi"
                                    notification.body = "$tarih Saatlık İzin Talebiniz Reddedildi!"
                                    notification.icon = iconString
                                    val firebasePush = FirebasePush.build(serverKey)
                                        .setNotification(notification)
                                        .setOnFinishPush {  }
                                    firebasePush.sendToTopic("$uid")
                                    //notifikasi//
                                    Toast.makeText(this@IzinAdminTime, "Reddetme islemi basarili!", Toast.LENGTH_SHORT).show()
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity(getIntent());
                                    overridePendingTransition(0, 0);
                                }else{
                                    Toast.makeText(this@IzinAdminTime, "Reddet etmeye çalıştığınız izin personelinize ait değil!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }else{
                myRefUser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (postSnapshot in snapshot.children){
                            var value = postSnapshot.getValue<UsersModel>()
                            if(value!!.adSoyad == adsoyad){
                                myRef.child(id).child("yonetici1").setValue(durum)
                                myRef.child(id).child("mesaj").setValue(mesaj)
                                uid = postSnapshot.key.toString()
                                //notifikasi//
                                var icon = R.drawable.logo
                                val iconString = icon.toString()
                                val notification = Notification()
                                notification.title = "Saatlık İzin Talebiniz Reddedildi"
                                notification.body = "$tarih Saatlık İzin Talebiniz Reddedildi!"
                                notification.icon = iconString
                                val firebasePush = FirebasePush.build(serverKey)
                                    .setNotification(notification)
                                    .setOnFinishPush {  }
                                firebasePush.sendToTopic("$uid")
                                //notifikasi//
                                Toast.makeText(this@IzinAdminTime, "Reddetme islemi basarili!", Toast.LENGTH_SHORT).show()
                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        null
                    }
                })
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

    fun printPdfSaatlik(izinId : String, bolum : String, tckn : String, talepEdenUid : String, yoneticiUid : String, ikUid : String){
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
                        IzinDetails("İzin Tipi", "Saatlik İzin"),
                        IzinDetails("İzin ID", it.child("izinId").value.toString()),
                        IzinDetails(" ", " "),
                        IzinDetails("Bölüm", bolum),
                        IzinDetails("TCKN", tckn),
                        IzinDetails("İzin Tarihi", it.child("izintarihi").value.toString()),
                        IzinDetails("İzin Başlama Saati", it.child("bassaati").value.toString()),
                        IzinDetails("İzin Bitiş Saati", it.child("bitsaati").value.toString()),
                        IzinDetails("Ücretli / Ücretsiz", it.child("izinTipi").value.toString()),
                        IzinDetails("İzin Nedeni", it.child("izinMazeret").value.toString()),
                        IzinDetails("İzin Nedeni (Açıklama)", it.child("sebeb").value.toString()),
                        IzinDetails(" ", " "),
                        IzinDetails("Yönetici Onayı", it.child("yonetici2").value.toString()),
                        IzinDetails("İnsan Kaynaklar Onayı", it.child("yonetici1").value.toString()),
                        IzinDetails("Mesaj", it.child("mesaj").value.toString()),
                    )
                    val pdfDetails = PdfDetails(it.child("adsoyad").value.toString(), izinDetailsList, talepEdenBitmap, yoneticiBitmap, ikBitmap)
                    val pdfConverter = PDFConverter()
                    pdfConverter.createPdf(this, pdfDetails, this, izinId, "saatlik")
                }
            }
        }
    }
}