package com.example.sibernetik

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.izin_user_activity.*
import kotlinx.android.synthetic.main.izin_user_time.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class IzinUserActivity : AppCompatActivity(), CustomAdapter.OnItemClickListener {
    var PREFS_KEY = "prefs"
    var name = ""
    lateinit var sharedPreferences: SharedPreferences

    val database = Firebase.database("DBLINK")
    val myRef = database.getReference("Izin").child("Izin Gunluk")
    val myRefUser = database.getReference("Users")
    var serverKey = "serverkey"
    val data = ArrayList<ItemsViewModel>()
    val adapter = CustomAdapter(data, this)

    var spinnerSelItem = ""
    var spinnerSelItem2 = ""

    var kalanIzin = 0

    var yoneticiAdi = ""
    var yoneticiUid = ""

    var clickedAdsoyad = ""
    var clickedAciklama = ""
    var clickedTarih = ""
    var clickedBasTarih = ""
    var clickedBitTarih = ""
    var clickedTipi = ""
    var clickedMazeret = ""
    var clickedId = ""
    var clickedOnay = ""

    var edit = 0

    override fun onItemClick(position: Int) {
        val clickedItem:ItemsViewModel = data[position]

        clickedAdsoyad = clickedItem.text
        clickedAciklama = clickedItem.nedeni
        clickedTarih = clickedItem.date
        clickedBasTarih = clickedItem.date.subSequence(0,10).toString()
        clickedBitTarih = clickedItem.date.subSequence(13,23).toString()
        clickedTipi = clickedItem.tip
        clickedMazeret = clickedItem.mazeret
        clickedId = clickedItem.id
        clickedOnay = clickedItem.yonetici1onay

        if(clickedOnay == "ONAYLANDI"){
            showMessage("Onaylandı İzin Talebi Düzenlenemez!", "Tamam")
        }else{
            nameTxt.setText(clickedAdsoyad)
            sebebTxt.setText(clickedAciklama)
            basTarihTxt.setText(clickedBasTarih)
            bitTarihTxt.setText(clickedBitTarih)
            setIzinTipiSpinnerSelectionByValue(clickedTipi)
            setMazeretSpinnerSelectionByValue(clickedMazeret)
            edit = 1
            Toast.makeText(this, "$clickedTarih İzini Seçtiniz!", Toast.LENGTH_LONG).show()

            btnSubmit.setText("Düzenle")
            btnSubmit.layoutParams = LinearLayout.LayoutParams(
                100.toPx(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.izin_user_activity)

        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val user = Firebase.auth.currentUser
        name = user!!.displayName.toString()
        showComments(name)
        getIzinAmount(name)

        val gonderBtn = findViewById<Button>(R.id.btnSubmit)
        val nameTextBox = findViewById<TextView>(R.id.nameTxt)

        nameTextBox.setText(name)

        val spinnerIzinTipi = findViewById<Spinner>(R.id.izinTipiTarihiUser)
        val spinnerMazeret = findViewById<Spinner>(R.id.izinMazeretTarihiUser)
        val arrayIzinTipi = resources.getStringArray(R.array.izin_tipi)
        val arrayMazeret = resources.getStringArray(R.array.mazeret)

        if (spinnerIzinTipi != null) {
            val adapterArray = ArrayAdapter(
                this,
                R.layout.spinner_list, arrayIzinTipi
            )
            adapterArray.setDropDownViewResource(R.layout.spinner_list)
            spinnerIzinTipi.adapter = adapterArray
        }

        spinnerIzinTipi.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ){
                spinnerSelItem = arrayIzinTipi[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }

        if (spinnerMazeret != null) {
            val adapterArray = ArrayAdapter(
                this,
                R.layout.spinner_list, arrayMazeret
            )
            adapterArray.setDropDownViewResource(R.layout.spinner_list)
            spinnerMazeret.adapter = adapterArray
        }

        spinnerMazeret.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ){
                spinnerSelItem2 = arrayMazeret[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }

        var formatted = ""
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

        basTarihBtn.setOnClickListener {
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
                basTarihTxt.setText(formatted).toString()
            }, year, month, day)
            dpd.show()
        }

        bitTarihBtn.setOnClickListener {
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
                bitTarihTxt.setText(formatted).toString()
            }, year, month, day)
            dpd.show()
        }

        gonderBtn.setOnClickListener {
            val adsoyad = nameTxt.text.toString()
            val sebeb = sebebTxt.text.toString()
            val bastarih = basTarihTxt.text.toString()
            val bittarih = bitTarihTxt.text.toString()
            val izintipishow = spinnerSelItem.toString()
            val izinmazeretshow = spinnerSelItem2.toString()

            if(edit == 0){
                sendIzin(adsoyad, sebeb, bastarih, bittarih, izintipishow, izinmazeretshow)
            }else if (edit == 1){
                editIzin(adsoyad, sebeb, bastarih, bittarih, izintipishow, izinmazeretshow)
            }
        }

        btnTemizleGunluk.setOnClickListener {
            nameTxt.text.clear()
            sebebTxt.text.clear()
            basTarihTxt.text.clear()
            bitTarihTxt.text.clear()

            clickedAdsoyad = ""
            clickedAciklama = ""
            clickedTarih = ""
            clickedBasTarih = ""
            clickedBitTarih = ""
            clickedTipi = ""
            clickedMazeret = ""
            clickedId = ""
            clickedOnay = ""

            edit = 0

            btnSubmit.setText("Gönder")
            btnSubmit.layoutParams = LinearLayout.LayoutParams(
                330.toPx(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        btnDeleteGunluk.setOnClickListener {
            if(clickedId.isEmpty() || edit == 0){
                showMessage("Herhangi bir izin Seçmediniz!", "Tamam")
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("İzin Talebiniz silinecektir! Emin misiniz?")
                builder.setPositiveButton("Tamam"){dialogInterface , which ->
                    myRef.child(clickedId).removeValue()
                    Toast.makeText(this, "İzin Talebiniz Silindi!", Toast.LENGTH_SHORT).show()
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

        val anasayfaUserGunlukBtn = findViewById<ImageButton>(R.id.anasayfaIzinUserGunlukBtn)
        anasayfaUserGunlukBtn.setOnClickListener {
            val intent = Intent(this,IzinMenuActivity::class.java)
            intent.putExtra("Yetki","User")
            startActivity(intent)
            finish()
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
                    var value = postSnapshot.getValue<IzinModel>()
                    if( value!!.adsoyad == name ){
                        val basTrh = value.bastarih.toString()
                        val bitTrh = value.bittarih.toString()
                        val tarih = "$basTrh - $bitTrh"
                        val izinGun = value.day.toString()
                        val izinTip = value.izinTipi.toString()
                        val izinMaz = value.izinMazeret.toString()

                        if( value.yonetici1 == "ONAY BEKLIYOR" && value.yonetici2 == "ONAY BEKLIYOR"){
                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value.adsoyad.toString(),
                                value.sebeb.toString(),
                                tarih,
                                "",
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                value.mesaj.toString(),
                                value.izinId.toString(),
                                "$izinGun Gün",
                                izinTip,
                                izinMaz
                            ))
                        }else if ( value.yonetici1 == "ONAY BEKLIYOR" && value.yonetici2 == "ONAYLANDI"){
                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value.adsoyad.toString(),
                                value.sebeb.toString(),
                                tarih,
                                "",
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                value.mesaj.toString(),
                                value.izinId.toString(),
                                "$izinGun Gün",
                                izinTip,
                                izinMaz
                            ))
                        }else if ( value.yonetici1 == "ONAYLANDI" && value.yonetici2 == "ONAYLANDI"){
                            data.add(ItemsViewModel(
                                R.drawable.onaylandi,
                                value.adsoyad.toString(),
                                value.sebeb.toString(),
                                tarih,
                                "",
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                value.mesaj.toString(),
                                value.izinId.toString(),
                                "$izinGun Gün",
                                izinTip,
                                izinMaz
                            ))
                        }else {
                            data.add(ItemsViewModel(
                                R.drawable.reddeti,
                                value.adsoyad.toString(),
                                value.sebeb.toString(),
                                tarih,
                                "",
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                value.mesaj.toString(),
                                value.izinId.toString(),
                                "$izinGun Gün",
                                izinTip,
                                izinMaz
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

    fun saveData(adsoyad : String, sebeb : String, bastarih : String, bittarih : String, yonetici1 : String, yonetici2 : String, mesaj : String, izinTipi : String, izinMazeret : String){
        val izinId = myRef.push().getKey()
        val format = SimpleDateFormat("dd-MM-yyyy")
        val days = TimeUnit.DAYS.convert(
            format.parse(bittarih).getTime() -
                    format.parse(bastarih).getTime(),
            TimeUnit.MILLISECONDS)
        val newIzin = IzinModel(izinId, adsoyad, sebeb, bastarih, bittarih, yonetici1, yonetici2, mesaj, izinTipi, izinMazeret, days.toInt())
        myRef.child(izinId.toString()).setValue(newIzin)
    }

    fun showMessage(message : String, button : String){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setNeutralButton(button){dialogInterface , which -> }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun getIzinAmount(name : String){
        myRefUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if (value!!.adSoyad == name){
                        kalanIzinTxt.setText(value.izin.toString())
                        kalanIzin = value.izin!!.toInt()
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
                                Toast.makeText(this@IzinUserActivity, "HATA OLUSTU! Lutfen Tekrar Deneyin", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                null
            }
        })
    }

    fun setIzinTipiSpinnerSelectionByValue(value: String) {
        val xmlArray: Array<String> = resources.getStringArray(R.array.izin_tipi) // get array from resources
        val spinner = findViewById<Spinner>(R.id.izinTipiTarihiUser) // get the spinner element

        spinner.setSelection(xmlArray.indexOf(
            xmlArray.first { elem -> elem == value } // find first element in array equal to value
        )) // get index of found element and use it as the position to set spinner to.
    }

    fun setMazeretSpinnerSelectionByValue(value: String) {
        val xmlArray: Array<String> = resources.getStringArray(R.array.mazeret) // get array from resources
        val spinner = findViewById<Spinner>(R.id.izinMazeretTarihiUser) // get the spinner element

        spinner.setSelection(xmlArray.indexOf(
            xmlArray.first { elem -> elem == value } // find first element in array equal to value
        )) // get index of found element and use it as the position to set spinner to.
    }

    fun Int.toPx() : Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun sendIzin(adsoyad : String, sebeb : String, bastarih : String, bittarih : String, izintipishow : String, izinmazeretshow : String) {
        val yonetici1durum = "ONAY BEKLIYOR"
        val yonetici2durum = "ONAY BEKLIYOR"
        val mesaj = "-"

        if(adsoyad.isEmpty()){
            showMessage("Lütfen Adınız ve Soyadınız Giriniz!","Tamam")
        }else if (bastarih.isEmpty()){
            showMessage("Lütfen Başlangıç Tarihi Giriniz!","Tamam")
        }else if (bittarih.isEmpty()){
            showMessage("Lütfen Bitiş Tarihi Giriniz!","Tamam")
        }else{
            val format = SimpleDateFormat("dd-MM-yyyy")
            val days = TimeUnit.DAYS.convert(
                format.parse(bittarih).getTime() -
                        format.parse(bastarih).getTime(),
                TimeUnit.MILLISECONDS)
            if (days > kalanIzin){
                showMessage("İzin Hakkınız Dolmuştur ya da Yetersiz!", "Tamam")
            }
            saveData(adsoyad, sebeb, bastarih, bittarih, yonetici1durum, yonetici2durum, mesaj, izintipishow, izinmazeretshow)
            //notifikasi//
            var icon = R.drawable.logo
            val iconString = icon.toString()
            val notification = Notification()
            notification.title = "Yeni İzin Talebi Var"
            notification.body = "$adsoyad adlı kişi $bastarih - $bittarih tarihi için izin talebi göndermişti"
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

    fun editIzin(adsoyad : String, sebeb : String, bastarih : String, bittarih : String, izintipishow : String, izinmazeretshow : String) {
        if(adsoyad.isEmpty()){
            showMessage("Lütfen Adınız ve Soyadınız Giriniz!","Tamam")
        }else if (bastarih.isEmpty()){
            showMessage("Lütfen Başlangıç Tarihi Giriniz!","Tamam")
        }else if (bittarih.isEmpty()){
            showMessage("Lütfen Bitiş Tarihi Giriniz!","Tamam")
        }else{
            val format = SimpleDateFormat("dd-MM-yyyy")
            val days = TimeUnit.DAYS.convert(
                format.parse(bittarih).getTime() -
                        format.parse(bastarih).getTime(),
                TimeUnit.MILLISECONDS)
            if (days > kalanIzin){
                showMessage("İzin Hakkınız Dolmuştur ya da Yetersiz!", "Tamam")
            }
            myRef.child(clickedId).child("adsoyad").setValue(adsoyad)
            myRef.child(clickedId).child("sebeb").setValue(sebeb)
            myRef.child(clickedId).child("bastarih").setValue(bastarih)
            myRef.child(clickedId).child("bittarih").setValue(bittarih)
            myRef.child(clickedId).child("izinTipi").setValue(izintipishow)
            myRef.child(clickedId).child("izinMazeret").setValue(izinmazeretshow)
            myRef.child(clickedId).child("day").setValue(days)


            Toast.makeText(this, "Izin Talebi Başarıyla Düzenlendi!", Toast.LENGTH_LONG).show()
            finish()
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);

        }
    }
}