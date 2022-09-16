package com.example.sibernetik

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_gecici_gorevlendirme_admin.recyclerview
import kotlinx.android.synthetic.main.mesai_admin_activity.*
import java.io.File

class MesaiAdminActivity : AppCompatActivity(), MesaiAdapter.OnItemClickListener {

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Mesai")
    val myRefUser = database.getReference("Users")
    private lateinit var auth: FirebaseAuth
    val storage = Firebase.storage
var serverKey = "serverkey"

    val data = ArrayList<MesaiViewModel>()
    val adapter = MesaiAdapter(data, this)

    var userUid = ""
    var userEmail = ""
    var userAdsoyad = ""
    var gorev = ""

    var clickedAdsoyad = ""
    var clickedMesaiTarih = ""
    var clickedMesaiSaatBaslama = ""
    var clickedMesaiSaatBitis = ""
    var clickedMesaiSebebi = ""
    var clickedId = ""
    var clickedOnayIK = ""
    var clickedOnayYonetici = ""
    var clickedKisiUid = ""
    var clickedToplamSaati = ""

    var spinnerSelItem = ""

    override fun onItemClick(position: Int) {
        val clickedItem:MesaiViewModel = data[position]

        clickedAdsoyad = clickedItem.adsoyad
        clickedMesaiTarih = clickedItem.mesaiTar.subSequence(0,10).toString()
        clickedMesaiSaatBaslama = clickedItem.timeBas
        clickedMesaiSaatBitis = clickedItem.timeBit
        clickedMesaiSebebi = clickedItem.sebeb
        clickedId = clickedItem.id
        clickedOnayIK = clickedItem.yonetici1onay
        clickedOnayYonetici = clickedItem.yonetici2onay
        clickedToplamSaati = "$clickedMesaiSaatBaslama - $clickedMesaiSaatBitis"

        myRefUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if(value!!.adSoyad == clickedAdsoyad){
                        clickedKisiUid = postSnapshot.key.toString()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MesaiAdminActivity, "HATA OLUSTU! Lutfen Tekrar Deneyin", Toast.LENGTH_SHORT).show()
            }
        })

        adsoyadMesaiDisplay.setText(clickedAdsoyad)
        tarihMesaiDisplay.setText(clickedMesaiTarih)
        saatMesaiDisplay.setText(clickedToplamSaati)

        btnMesaiOnay.visibility = View.VISIBLE
        btnMesaiReddet.visibility = View.VISIBLE
        btnMesaiTemizle.visibility = View.VISIBLE
        btnMesaiSil.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mesai_admin_activity)

        auth = Firebase.auth

        getGorev()

        val arrayDurum = resources.getStringArray(R.array.izin_durum)
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        val spinner = findViewById<Spinner>(R.id.spinnerMesai)
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

        araMesaiBtn.setOnClickListener {
            val araKisi = adAraMesaiAdminTxt.text.toString()
            searchAdmin(araKisi)
        }

        btnMesaiOnay.setOnClickListener {
            mesaiAccepted(clickedId)
        }

        btnMesaiReddet.setOnClickListener {
            mesaiDeclined(clickedId)
        }

        btnMesaiTemizle.setOnClickListener {
            clickedAdsoyad = ""
            clickedMesaiTarih = ""
            clickedMesaiSaatBaslama = ""
            clickedMesaiSaatBitis = ""
            clickedMesaiSebebi = ""
            clickedId = ""
            clickedOnayIK = ""
            clickedOnayYonetici = ""
            clickedKisiUid = ""

            adsoyadMesaiDisplay.setText("-")
            tarihMesaiDisplay.setText("-")
            saatMesaiDisplay.setText("-")

            btnMesaiOnay.visibility = View.INVISIBLE
            btnMesaiReddet.visibility = View.INVISIBLE
            btnMesaiTemizle.visibility = View.INVISIBLE
            btnMesaiSil.visibility = View.INVISIBLE
        }

        btnMesaiSil.setOnClickListener {
            if(clickedId.isEmpty()){
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Herhangi bir Kaydı seçmediniz!")
                builder.setNeutralButton("Tamam"){dialogInterface , which -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Mesai Kaydı silinecektir! Emin misiniz?")
                builder.setPositiveButton("Tamam"){dialogInterface , which ->
                    myRef.child(clickedId).removeValue()
                    Toast.makeText(this, "Mesai Kaydı Silindi!", Toast.LENGTH_SHORT).show()
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

        anasayfaMesaiAdminBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun filtreleme(durum : String){
        val dbRef = myRef.orderByChild("adsoyad")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                recyclerview.adapter = adapter
                for(postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<MesaiModel>()
                    if(gorev == "YONETICI"){
                        if (durum == "ONAY BEKLIYOR"){
                            if(value!!.yonetici2 == "ONAY BEKLIYOR"){
                                val trh = value.mesaiTarih.toString()
                                val saatBas = value.mesaiSaatBaslama.toString()
                                val saatBit = value.mesaiSaatBitis.toString()
                                val sebeb = value.sebeb.toString()

                                data.add(
                                    MesaiViewModel(
                                    R.drawable.bekleme,
                                        value.adsoyad.toString(),
                                        trh,
                                        saatBas,
                                        saatBit,
                                        sebeb,
                                        value.mesaiId.toString(),
                                        value.yonetici1.toString(),
                                        value.yonetici2.toString()
                                )
                                )
                            }
                        }else if (durum == "ONAYLANDI"){
                            if(value!!.yonetici1 == "ONAYLANDI" && value!!.yonetici2 == "ONAYLANDI"){
                                val trh = value.mesaiTarih.toString()
                                val saatBas = value.mesaiSaatBaslama.toString()
                                val saatBit = value.mesaiSaatBitis.toString()
                                val sebeb = value.sebeb.toString()

                                data.add(
                                    MesaiViewModel(
                                    R.drawable.onaylandi,
                                        value.adsoyad.toString(),
                                        trh,
                                        saatBas,
                                        saatBit,
                                        sebeb,
                                        value.mesaiId.toString(),
                                        value.yonetici1.toString(),
                                        value.yonetici2.toString()
                                )
                                )
                            }
                        }else{
                            if(value!!.yonetici2 == "REDDETTI"){
                                val trh = value.mesaiTarih.toString()
                                val saatBas = value.mesaiSaatBaslama.toString()
                                val saatBit = value.mesaiSaatBitis.toString()
                                val sebeb = value.sebeb.toString()

                                data.add(
                                    MesaiViewModel(
                                    R.drawable.reddeti,
                                        value.adsoyad.toString(),
                                        trh,
                                        saatBas,
                                        saatBit,
                                        sebeb,
                                        value.mesaiId.toString(),
                                        value.yonetici1.toString(),
                                        value.yonetici2.toString()
                                )
                                )
                            }
                        }
                    }else if (gorev == "INSAN KAYNAKLARI"){
                        if (durum == "ONAY BEKLIYOR"){
                            if(value!!.yonetici1 == "ONAY BEKLIYOR" && value!!.yonetici2 == "ONAYLANDI"){
                                val trh = value.mesaiTarih.toString()
                                val saatBas = value.mesaiSaatBaslama.toString()
                                val saatBit = value.mesaiSaatBitis.toString()
                                val sebeb = value.sebeb.toString()

                                data.add(
                                    MesaiViewModel(
                                    R.drawable.bekleme,
                                        value.adsoyad.toString(),
                                        trh,
                                        saatBas,
                                        saatBit,
                                        sebeb,
                                        value.mesaiId.toString(),
                                        value.yonetici1.toString(),
                                        value.yonetici2.toString()
                                )
                                )
                            }
                        }else if (durum == "ONAYLANDI"){
                            if(value!!.yonetici1 == "ONAYLANDI" && value!!.yonetici2 == "ONAYLANDI"){
                                val trh = value.mesaiTarih.toString()
                                val saatBas = value.mesaiSaatBaslama.toString()
                                val saatBit = value.mesaiSaatBitis.toString()
                                val sebeb = value.sebeb.toString()

                                data.add(
                                    MesaiViewModel(
                                    R.drawable.onaylandi,
                                        value.adsoyad.toString(),
                                        trh,
                                        saatBas,
                                        saatBit,
                                        sebeb,
                                        value.mesaiId.toString(),
                                        value.yonetici1.toString(),
                                        value.yonetici2.toString()
                                )
                                )
                            }
                        }else{
                            if(value!!.yonetici1 == "REDDETTI" || value!!.yonetici2 == "REDDETTI"){
                                val trh = value.mesaiTarih.toString()
                                val saatBas = value.mesaiSaatBaslama.toString()
                                val saatBit = value.mesaiSaatBitis.toString()
                                val sebeb = value.sebeb.toString()

                                data.add(
                                    MesaiViewModel(
                                    R.drawable.reddeti,
                                        value.adsoyad.toString(),
                                        trh,
                                        saatBas,
                                        saatBit,
                                        sebeb,
                                        value.mesaiId.toString(),
                                        value.yonetici1.toString(),
                                        value.yonetici2.toString()
                                )
                                )
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

    fun searchAdmin(name : String){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                for( postSnapshot in dataSnapshot.children ){
                    var value = postSnapshot.getValue<MesaiModel>()
                    if( value!!.adsoyad!!.toLowerCase().contains(name.toLowerCase()) ){
                        if (value!!.yonetici1 == "REDDETTI" && value!!.yonetici2 == "REDDETTI"){
                            val trh = value.mesaiTarih.toString()
                            val saatBas = value.mesaiSaatBaslama.toString()
                            val saatBit = value.mesaiSaatBitis.toString()
                            val sebeb = value.sebeb.toString()

                            data.add(
                                MesaiViewModel(
                                R.drawable.reddeti,
                                    value.adsoyad.toString(),
                                    trh,
                                    saatBas,
                                    saatBit,
                                    sebeb,
                                    value.mesaiId.toString(),
                                    value.yonetici1.toString(),
                                    value.yonetici2.toString()
                            )
                            )
                        }else if (gorev == "YONETICI" && value!!.yonetici1 == "ONAY BEKLIYOR"){
                            val trh = value.mesaiTarih.toString()
                            val saatBas = value.mesaiSaatBaslama.toString()
                            val saatBit = value.mesaiSaatBitis.toString()
                            val sebeb = value.sebeb.toString()

                            data.add(
                                MesaiViewModel(
                                R.drawable.bekleme,
                                    value.adsoyad.toString(),
                                    trh,
                                    saatBas,
                                    saatBit,
                                    sebeb,
                                    value.mesaiId.toString(),
                                    value.yonetici1.toString(),
                                    value.yonetici2.toString()
                            )
                            )
                        }else if ( value!!.yonetici1 == "ONAYLANDI" && value!!.yonetici2 == "ONAYLANDI"){
                            val trh = value.mesaiTarih.toString()
                            val saatBas = value.mesaiSaatBaslama.toString()
                            val saatBit = value.mesaiSaatBitis.toString()
                            val sebeb = value.sebeb.toString()

                            data.add(
                                MesaiViewModel(
                                R.drawable.onaylandi,
                                    value.adsoyad.toString(),
                                    trh,
                                    saatBas,
                                    saatBit,
                                    sebeb,
                                    value.mesaiId.toString(),
                                    value.yonetici1.toString(),
                                    value.yonetici2.toString()
                            )
                            )
                        }else if( gorev == "INSAN KAYNAKLARI" && value!!.yonetici2 == "ONAYLANDI"){
                            val trh = value.mesaiTarih.toString()
                            val saatBas = value.mesaiSaatBaslama.toString()
                            val saatBit = value.mesaiSaatBitis.toString()
                            val sebeb = value.sebeb.toString()

                            data.add(
                                MesaiViewModel(
                                R.drawable.bekleme,
                                    value.adsoyad.toString(),
                                    trh,
                                    saatBas,
                                    saatBit,
                                    sebeb,
                                    value.mesaiId.toString(),
                                    value.yonetici1.toString(),
                                    value.yonetici2.toString()
                            )
                            )
                        }else if ( gorev == "INSAN KAYNAKLARI" && value!!.yonetici2 != "ONAYLANDI"){
                            continue
                        }else {
                            val trh = value.mesaiTarih.toString()
                            val saatBas = value.mesaiSaatBaslama.toString()
                            val saatBit = value.mesaiSaatBitis.toString()
                            val sebeb = value.sebeb.toString()

                            data.add(
                                MesaiViewModel(
                                R.drawable.reddeti,
                                    value.adsoyad.toString(),
                                    trh,
                                    saatBas,
                                    saatBit,
                                    sebeb,
                                    value.mesaiId.toString(),
                                    value.yonetici1.toString(),
                                    value.yonetici2.toString()
                            )
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun mesaiAccepted (id : String){
        val durum = "ONAYLANDI"
        val mesaj = mesaiMesaj.text.toString()

        if(id.isEmpty()){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Herhangi bir mesai seçmediniz!")
            builder.setNeutralButton("Tamam"){dialogInterface , which -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else{
            if (clickedOnayIK == "ONAYLANDI" && clickedOnayYonetici == "ONAYLANDI") {
                Toast.makeText(this, "Daha önce onaylandi!", Toast.LENGTH_SHORT).show()
            }else if(gorev == "YONETICI"){
                myRefUser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(postSnapshot in snapshot.children){
                            var value = postSnapshot.getValue<UsersModel>()
                            if (value!!.adSoyad == clickedAdsoyad){
                                if (value!!.yonetici == userAdsoyad){
                                    myRef.child(id).child("yonetici2").setValue(durum)
                                    myRef.child(id).child("mesaj").setValue(mesaj)
                                    myRef.child(id).child("yoneticiId").setValue(userUid)
                                    //notifikasi//
                                    var icon = R.drawable.logo
                                    val iconString = icon.toString()
                                    val notification = Notification()
                                    notification.title = "Yeni Mesai Kaydı Var"
                                    notification.body = "$clickedAdsoyad adlı kişi $clickedMesaiTarih tarihi için Mesai Kaydınız göndermişti"
                                    notification.icon = iconString
                                    val firebasePush = FirebasePush.build(serverKey)
                                        .setNotification(notification)
                                        .setOnFinishPush {  }
                                    firebasePush.sendToTopic("IK")
                                    //notifikasi//
                                    Toast.makeText(this@MesaiAdminActivity, "Onaylama islemi basarili!", Toast.LENGTH_SHORT).show()
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity(getIntent());
                                    overridePendingTransition(0, 0);
                                }else{
                                    Toast.makeText(this@MesaiAdminActivity, "Kabul etmeye çalıştığınız izin personelinize ait değil!", Toast.LENGTH_SHORT).show()
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
                        val yoneticiUid = it.child("yoneticiId").value.toString()
                        myRef.child(id).child("yonetici1").setValue(durum)
                        myRef.child(id).child("mesaj").setValue(mesaj)
                        myRef.child(id).child("ikId").setValue(userUid)
                        var bolum = ""
                        var tckn = ""
                        var bolumdekigorev = ""
                        myRefUser.child(clickedKisiUid).get().addOnSuccessListener {
                            if(it.exists()){
                                bolum = it.child("bolum").value.toString()
                                tckn = it.child("tckn").value.toString()
                                bolumdekigorev = it.child("bolumdekiGorev").value.toString()
                                if(bolumdekigorev.isEmpty() || bolumdekigorev == "null"){
                                    bolumdekigorev = "-"
                                }
                                //notifikasi//
                                var icon = R.drawable.logo
                                val iconString = icon.toString()
                                val notification = Notification()
                                notification.title = "Mesai Kaydınız Onaylandı"
                                notification.body = "$clickedMesaiTarih Mesai Kaydınız Onaylandı!"
                                notification.icon = iconString
                                val firebasePush = FirebasePush.build(serverKey)
                                    .setNotification(notification)
                                    .setOnFinishPush {  }
                                firebasePush.sendToTopic("$clickedKisiUid")
                                //notifikasi//
                                printPdfMesai(id, bolum, tckn, bolumdekigorev, clickedKisiUid, yoneticiUid, userUid)
                                Toast.makeText(this, "Onaylama islemi basarili!", Toast.LENGTH_SHORT).show()
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

    fun mesaiDeclined(id : String){
        val durum = "REDDETTI"
        val mesaj = mesaiMesaj.text.toString()

        if(id.isEmpty()){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Herhangi bir mesai seçmediniz!")
            builder.setNeutralButton("Tamam"){dialogInterface , which -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else{
            if (clickedOnayIK == "REDDETTI" && clickedOnayYonetici == "REDDETTI") {
                Toast.makeText(this, "mesai daha önce reddedildi!", Toast.LENGTH_SHORT).show()
            }else if(gorev == "YONETICI"){
                myRefUser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(postSnapshot in snapshot.children){
                            var value = postSnapshot.getValue<UsersModel>()
                            if (value!!.adSoyad == clickedAdsoyad){
                                if (value!!.yonetici == userAdsoyad){
                                    myRef.child(id).child("yonetici2").setValue(durum)
                                    myRef.child(id).child("mesaj").setValue(mesaj)
                                    //notifikasi//
                                    var icon = R.drawable.logo
                                    val iconString = icon.toString()
                                    val notification = Notification()
                                    notification.title = "Mesai Kaydınız Reddedildi"
                                    notification.body = "$clickedMesaiTarih Mesai Kaydınız Reddedildi!"
                                    notification.icon = iconString
                                    val firebasePush = FirebasePush.build(serverKey)
                                        .setNotification(notification)
                                        .setOnFinishPush {  }
                                    firebasePush.sendToTopic("$clickedKisiUid")
                                    //notifikasi//
                                    Toast.makeText(this@MesaiAdminActivity, "Reddetme islemi basarili!", Toast.LENGTH_SHORT).show()
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity(getIntent());
                                    overridePendingTransition(0, 0);
                                }else{
                                    Toast.makeText(this@MesaiAdminActivity, "Reddet etmeye çalıştığınız izin personelinize ait değil!", Toast.LENGTH_SHORT).show()
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
                notification.title = "Mesai Kaydınız Reddedildi"
                notification.body = "$clickedMesaiTarih Mesai Kaydınız Reddedildi!"
                notification.icon = iconString
                val firebasePush = FirebasePush.build(serverKey)
                    .setNotification(notification)
                    .setOnFinishPush {  }
                firebasePush.sendToTopic("$clickedKisiUid")
                //notifikasi//
                Toast.makeText(this, "Reddetme islemi basarili!", Toast.LENGTH_SHORT).show()
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        }
    }

    fun getGorev(){
        val user = Firebase.auth.currentUser
        userUid = user!!.uid
        user?.let {
            for (profile in it.providerData) {
                userEmail = profile.email.toString()
                userAdsoyad = profile.displayName.toString()
            }
        }
        myRefUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if (value!!.ePosta == userEmail){
                        gorev = value!!.gorev.toString()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun printPdfMesai(mesaiId : String, bolum : String, tckn : String, bolumdekigorev : String, talepEdenUid : String, yoneticiUid : String, ikUid : String){
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

            myRef.child(mesaiId).get().addOnSuccessListener {
                if(it.exists()){
                    val izinDetailsList = listOf(
                        IzinDetails("Mesai ID", it.child("mesaiId").value.toString()),
                        IzinDetails("TCKN", tckn),
                        IzinDetails("Bölüm", bolum),
                        IzinDetails("Görevi", bolumdekigorev),
                        IzinDetails("Mesai Yapılan Tarihi", it.child("mesaiTarih").value.toString()),
                        IzinDetails("Başlangıç Mesai Saati", it.child("mesaiSaatBaslama").value.toString()),
                        IzinDetails("Bitiş Mesai Saati", it.child("mesaiSaatBitis").value.toString()),
                        IzinDetails("Mesai Sebebi", it.child("sebeb").value.toString()),
                        IzinDetails(" ", " "),
                        IzinDetails("Yönetici Onayı", it.child("yonetici2").value.toString()),
                        IzinDetails("İnsan Kaynakları Onayı", it.child("yonetici1").value.toString()),
                        IzinDetails("Mesaj", it.child("mesaj").value.toString()),
                    )
                    val pdfDetails = PdfDetails(it.child("adsoyad").value.toString(), izinDetailsList, talepEdenBitmap,yoneticiBitmap,ikBitmap)
                    val pdfConverter = PDFConverterMesai()
                    pdfConverter.createPdf(this, pdfDetails, this, mesaiId)
                }
            }.addOnFailureListener {
                Log.w("HATA TASK bitmap", "error bro")
            }
        }
    }
}