package com.example.sibernetik

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import fcm.androidtoandroid.FirebasePush
import fcm.androidtoandroid.model.Notification
import kotlinx.android.synthetic.main.activity_gecici_gorevlendirme_user.*
import kotlinx.android.synthetic.main.izin_user_activity.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class GeciciGorevlendirmeUserActivity : AppCompatActivity(), CustomAdapter.OnItemClickListener {

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Gecici Gorevlendirme")
    val myRefUser = database.getReference("Users")
    var serverKey = "serverkey"

    val data = ArrayList<ItemsViewModel>()
    val adapter = CustomAdapter(data,this)

    var yoneticiAdi = ""
    var yoneticiUid = ""
    var name = ""

    var clickedAdsoyad = ""
    var clickedYeri = ""
    var clickedTarif = ""
    var clickedBasTarih = ""
    var clickedBitTarih = ""
    var clickedTarih = ""
    var clickedId = ""
    var clickedOnay = ""

    var edit = 0

    override fun onItemClick(position: Int) {
        val clickedItem:ItemsViewModel = data[position]

        clickedAdsoyad = clickedItem.text
        clickedYeri = clickedItem.nedeni
        clickedTarif = clickedItem.date
        clickedBasTarih = clickedItem.time.subSequence(0,10).toString()
        clickedBitTarih = clickedItem.time.subSequence(13,23).toString()
        clickedTarih = clickedItem.time
        clickedId = clickedItem.id
        clickedOnay = clickedItem.yonetici1onay

        if(clickedOnay == "ONAYLANDI"){
            showMessage("Onaylandı İzin Talebi Düzenlenemez!", "Tamam")
        }else{
            nameGGTxt.setText(clickedAdsoyad)
            ggYeriTxt.setText(clickedYeri)
            ggTarifiTxt.setText(clickedTarif)
            ggBasTarihTxt.setText(clickedBasTarih)
            ggBitTarihTxt.setText(clickedBitTarih)
            edit = 1
            Toast.makeText(this, "$clickedTarih İzini Seçtiniz!", Toast.LENGTH_LONG).show()

            btnSubmitGg.setText("Düzenle")
            btnSubmitGg.layoutParams = LinearLayout.LayoutParams(
                100.toPx(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gecici_gorevlendirme_user)

        val user = Firebase.auth.currentUser
        name = user!!.displayName.toString()
        getYoneticiDetay()
        showComments(name)

        nameGGTxt.setText(name)

        var formatted = ""
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

        ggBasTarihBtn.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                var newMonth = monthOfYear + 1
                var formattedMonth = "" + newMonth
                var formattedDate = "" + dayOfMonth
                if(newMonth < 10){

                    formattedMonth = "0" + newMonth;
                }
                if(dayOfMonth < 10){

                    formattedDate  = "0" + dayOfMonth ;
                }
                formatted = "$formattedDate-$formattedMonth-$year"
                ggBasTarihTxt.setText(formatted).toString()
            }, year, month, day)
            dpd.show()
        }
        ggBitTarihBtn.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                var newMonth = monthOfYear + 1
                var formattedMonth = "" + newMonth
                var formattedDate = "" + dayOfMonth
                if(newMonth < 10){

                    formattedMonth = "0" + newMonth;
                }
                if(dayOfMonth < 10){

                    formattedDate  = "0" + dayOfMonth ;
                }
                formatted = "$formattedDate-$formattedMonth-$year"
                ggBitTarihTxt.setText(formatted).toString()
            }, year, month, day)
            dpd.show()
        }

        anasayfaGGUserBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSubmitGg.setOnClickListener {
            val adsoyad = nameGGTxt.text.toString()
            val yeri = ggYeriTxt.text.toString()
            val tarifi = ggTarifiTxt.text.toString()
            val bastarih = ggBasTarihTxt.text.toString()
            val bittarih = ggBitTarihTxt.text.toString()

            val bastarihVerif = bastarih.matches(Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}"))
            val bittarihVerif = bittarih.matches(Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}"))

            if(bastarihVerif && bittarihVerif){
                if(edit == 0){
                    sendGG(adsoyad, yeri, tarifi, bastarih, bittarih)
                }else if (edit == 1){
                    editGG(adsoyad, yeri, tarifi, bastarih, bittarih)
                }
            }else{
                showMessage("Yanlış Tarih Formatı! Lütfen GG-AA-YYYY tarih formatını kullanın!", "Tamam")
            }
        }

        btnTemizleGg.setOnClickListener {
            nameGGTxt.setText(name)
            ggYeriTxt.text.clear()
            ggTarifiTxt.text.clear()
            ggBasTarihTxt.text.clear()
            ggBitTarihTxt.text.clear()

            clickedAdsoyad = ""
            clickedYeri = ""
            clickedTarif = ""
            clickedBasTarih = ""
            clickedBitTarih = ""
            clickedTarih = ""
            clickedId = ""
            clickedOnay = ""

            edit = 0

            btnSubmitGg.setText("Gönder")
            btnSubmitGg.layoutParams = LinearLayout.LayoutParams(
                330.toPx(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        btnDeleteGg.setOnClickListener {
            if(clickedId.isEmpty() || edit == 0){
                showMessage("Herhangi bir talebi Seçmediniz!", "Tamam")
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Talebiniz silinecektir! Emin misiniz?")
                builder.setPositiveButton("Tamam"){dialogInterface , which ->
                    myRef.child(clickedId).removeValue()
                    Toast.makeText(this, "Talebiniz Silindi!", Toast.LENGTH_SHORT).show()
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
    }

    fun saveData (adsoyad : String, yeri : String, tarif : String, bastarih : String, bittarih : String){
        val yonetici1 = "ONAY BEKLIYOR"
        val yonetici2 = "ONAY BEKLIYOR"
        val format = SimpleDateFormat("dd-MM-yyyy")
        val days = TimeUnit.DAYS.convert(
            format.parse(bittarih).getTime() -
                    format.parse(bastarih).getTime(),
            TimeUnit.MILLISECONDS)

        val id = myRef.push().getKey()
        val newGG = GeciciGorevModel(id, adsoyad, tarif, yeri, bastarih, bittarih, yonetici1, yonetici2, days.toInt(),"-")
        myRef.child(id.toString()).setValue(newGG)
    }

    fun Int.toPx() : Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun showMessage(message : String, button : String){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setNeutralButton(button){dialogInterface , which -> }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun getYoneticiDetay(){
        myRefUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if (value!!.adSoyad == name){
                        yoneticiAdi = value.yonetici.toString()
                        myRefUser.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (postSnapshot in snapshot.children){
                                    var value = postSnapshot.getValue<UsersModel>()
                                    if (value!!.adSoyad == yoneticiAdi){
                                        yoneticiUid = postSnapshot.key.toString()
                                        Log.d("dalem fungsi",yoneticiUid)
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(applicationContext, "HATA OLUSTU! Lutfen Tekrar Deneyin", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "HATA OLUSTU! Lutfen Tekrar Deneyin", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun sendGG (adsoyad : String, yeri : String, tarif : String, bastarih : String, bittarih : String){
        if(adsoyad.isEmpty()){
            showMessage("Lütfen Adınız ve Soyadınız Giriniz!","Tamam")
        }else if (bastarih.isEmpty()){
            showMessage("Lütfen Başlangıç Tarihi Giriniz!","Tamam")
        }else if (bittarih.isEmpty()){
            showMessage("Lütfen Bitiş Tarihi Giriniz!","Tamam")
        }else if (yeri.isEmpty()){
            showMessage("Lütfen Görev Yeri Giriniz!","Tamam")
        }else if (tarif.isEmpty()){
            showMessage("Lütfen Görev Tarifi Giriniz!","Tamam")
        }else{
            saveData(adsoyad, yeri, tarif, bastarih, bittarih)
            //notifikasi//
            var icon = R.drawable.logo
            val iconString = icon.toString()
            val notification = Notification()
            notification.title = "Yeni Geçici Görevlendirme Talebi Var"
            notification.body = "$adsoyad adlı kişi $bastarih - $bittarih tarihi için geçici görevlendirme talebi göndermişti"
            notification.icon = iconString
            val firebasePush = FirebasePush.build(serverKey)
                .setNotification(notification)
                .setOnFinishPush {  }
            firebasePush.sendToTopic("$yoneticiUid")
            Log.d("yoneticiUid",yoneticiUid)
            //notifikasi//
            Toast.makeText(this, "Izin talebi başarıyla gönderildi!", Toast.LENGTH_LONG).show()
            finish()
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
    }

    fun editGG (adsoyad : String, yeri : String, tarif : String, bastarih : String, bittarih : String) {
        if (adsoyad.isEmpty()) {
            showMessage("Lütfen Adınız ve Soyadınız Giriniz!", "Tamam")
        } else if (bastarih.isEmpty()) {
            showMessage("Lütfen Başlangıç Tarihi Giriniz!", "Tamam")
        } else if (bittarih.isEmpty()) {
            showMessage("Lütfen Bitiş Tarihi Giriniz!", "Tamam")
        } else if (yeri.isEmpty()) {
            showMessage("Lütfen Görev Yeri Giriniz!", "Tamam")
        } else if (tarif.isEmpty()) {
            showMessage("Lütfen Görev Tarifi Giriniz!", "Tamam")
        } else {
            myRef.child(clickedId).child("adsoyad").setValue(adsoyad)
            myRef.child(clickedId).child("yeri").setValue(yeri)
            myRef.child(clickedId).child("tarif").setValue(tarif)
            myRef.child(clickedId).child("bastarih").setValue(bastarih)
            myRef.child(clickedId).child("bittarih").setValue(bittarih)

            Toast.makeText(this, "Talebi Başarıyla Düzenlendi!", Toast.LENGTH_LONG).show()
            finish()
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
    }

    fun showComments(name : String){
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                for( postSnapshot in dataSnapshot.children ){
                    var value = postSnapshot.getValue<GeciciGorevModel>()
                    if( value!!.adsoyad == name ){
                        val basTrh = value.bastarih.toString()
                        val bitTrh = value.bittarih.toString()
                        val tarih = "$basTrh - $bitTrh"
                        val izinGun = value.day.toString()

                        if( value.yonetici1 == "ONAY BEKLIYOR" && value.yonetici2 == "ONAY BEKLIYOR"){
                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value.adsoyad.toString(),
                                value.yeri.toString(),
                                value.tarif.toString(),
                                tarih,
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                value.mesaj.toString(),
                                value.id.toString(),
                                "-",
                                "$izinGun Gün",
                                "-"
                            ))
                        }else if ( value.yonetici1 == "ONAY BEKLIYOR" && value.yonetici2 == "ONAYLANDI"){
                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value.adsoyad.toString(),
                                value.yeri.toString(),
                                value.tarif.toString(),
                                tarih,
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                value.mesaj.toString(),
                                value.id.toString(),
                                "$izinGun Gün",
                                "-",
                                "-"
                            ))
                        }else if ( value.yonetici1 == "ONAYLANDI" && value.yonetici2 == "ONAYLANDI"){
                            data.add(ItemsViewModel(
                                R.drawable.onaylandi,
                                value.adsoyad.toString(),
                                value.yeri.toString(),
                                value.tarif.toString(),
                                tarih,
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                value.mesaj.toString(),
                                value.id.toString(),
                                "$izinGun Gün",
                                "-",
                                "-"
                            ))
                        }else {
                            data.add(ItemsViewModel(
                                R.drawable.reddeti,
                                value.adsoyad.toString(),
                                value.yeri.toString(),
                                value.tarif.toString(),
                                tarih,
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                value.mesaj.toString(),
                                value.id.toString(),
                                "$izinGun Gün",
                                "-",
                                "-"
                            ))
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