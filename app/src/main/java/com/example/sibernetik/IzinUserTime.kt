package com.example.sibernetik

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import fcm.androidtoandroid.FirebasePush
import fcm.androidtoandroid.model.Notification
import kotlinx.android.synthetic.main.izin_user_time.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class IzinUserTime : AppCompatActivity(), CustomAdapter.OnItemClickListener  {
    var PREFS_KEY = "prefs"
    var EMAIL_KEY = "email"
    var name = ""
    lateinit var sharedPreferences: SharedPreferences
    var serverKey = "SERVERKEY"

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Izin").child("Izin Saatlik")
    val myRefUser = database.getReference("Users")
    private lateinit var auth: FirebaseAuth

    val data = ArrayList<ItemsViewModel>()
    val adapter = CustomAdapter(data, this)

    var spinnerSelItem = ""
    var spinnerSelItem2 = ""

    var yoneticiAdi = ""
    var yoneticiUid = ""

    var clickedAdsoyad = ""
    var clickedAciklama = ""
    var clickedTarih = ""
    var clickedBasSaat = ""
    var clickedBitSaat = ""
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
        clickedBasSaat = clickedItem.time.subSequence(0,5).toString()
        clickedBitSaat = clickedItem.time.subSequence(8,13).toString()
        clickedTipi = clickedItem.tip
        clickedMazeret = clickedItem.mazeret
        clickedId = clickedItem.id
        clickedOnay = clickedItem.yonetici1onay

        if(clickedOnay == "ONAYLANDI"){
            showMessage("Onaylandı İzin Talebi Düzenlenemez!", "Tamam")
        }else{
            nameTxtSaatlik.setText(clickedAdsoyad)
            sebebTxtSaatlik.setText(clickedAciklama)
            tarihTxtSaatlik.setText(clickedTarih)
            saatBaslamaTxt.setText(clickedBasSaat)
            saatBitisTxt.setText(clickedBitSaat)
            setIzinTipiSpinnerSelectionByValue(clickedTipi)
            setMazeretSpinnerSelectionByValue(clickedMazeret)
            edit = 1
            Toast.makeText(this, "$clickedTarih İzini Seçtiniz!", Toast.LENGTH_LONG).show()

            btnSubmitSaatlik.setText("Düzenle")
            btnSubmitSaatlik.layoutParams = LinearLayout.LayoutParams(
                100.toPx(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.izin_user_time)

        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val user = Firebase.auth.currentUser
        name = user!!.displayName.toString()
        showComments(name)
        getYoneticiUid(name)

        val gonderBtn = findViewById<Button>(R.id.btnSubmitSaatlik)
        val nameTextBox = findViewById<TextView>(R.id.nameTxtSaatlik)

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        var formatted: String = simpleDateFormat.format(Date())

        nameTextBox.setText(name)
        tarihTxtSaatlik.setText(formatted)

        val arrayIzinTipi = resources.getStringArray(R.array.izin_tipi)
        val spinnerIzinTipi = findViewById<Spinner>(R.id.izinTipiTimeUser)

        val arrayMazeret = resources.getStringArray(R.array.mazeret)
        val spinnerMazeret = findViewById<Spinner>(R.id.izinMazeretTimeUser)

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

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

        izinTarihBtn.setOnClickListener {
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
                tarihTxtSaatlik.setText(formatted).toString()
            }, year, month, day)
            dpd.show()
        }

        saatBaslamaBtn.setOnClickListener{
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                saatBaslamaTxt.setText(SimpleDateFormat("HH:mm").format(cal.time))
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        saatBitisBtn.setOnClickListener{
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                saatBitisTxt.setText(SimpleDateFormat("HH:mm").format(cal.time))
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        gonderBtn.setOnClickListener {
            val adsoyad = nameTxtSaatlik.text.toString()
            val sebeb = sebebTxtSaatlik.text.toString()
            val izintarihi = tarihTxtSaatlik.text.toString()
            val bassaati = saatBaslamaTxt.text.toString()
            val bitsaati= saatBitisTxt.text.toString()
            val izintipishow = spinnerSelItem
            val izinmazeretshow = spinnerSelItem2

            val izintarihiVerif = izintarihi.matches(Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}"))

            if(izintarihiVerif){
                if(edit == 0){
                    sendIzin(adsoyad, sebeb, izintarihi, bassaati, bitsaati, izintipishow, izinmazeretshow)
                }else if (edit == 1){
                    editIzin(adsoyad, sebeb, izintarihi, bassaati, bitsaati, izintipishow, izinmazeretshow)
                }
            }else{
                showMessage("Yanlış Tarih Formatı! Lütfen GG-AA-YYYY tarih formatını kullanın!", "Tamam")
            }


        }

        btnTemizleSaatlik.setOnClickListener {
            nameTxtSaatlik.text.clear()
            sebebTxtSaatlik.text.clear()
            saatBaslamaTxt.text.clear()
            saatBitisTxt.text.clear()
            tarihTxtSaatlik.text.clear()

            clickedAdsoyad = ""
            clickedAciklama = ""
            clickedTarih = ""
            clickedBasSaat = ""
            clickedBitSaat = ""
            clickedTipi = ""
            clickedMazeret = ""
            clickedId = ""
            clickedOnay = ""

            edit = 0

            gonderBtn.setText("Gönder")
            gonderBtn.layoutParams = LinearLayout.LayoutParams(
                330.toPx(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        btnDeleteSaatlik.setOnClickListener {
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

        val anasayfaUserTimeBtn = findViewById<ImageButton>(R.id.anasayfaIzinUserTimeBtn)
        anasayfaUserTimeBtn.setOnClickListener {
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
                    var value = postSnapshot.getValue<IzinModelSaatlik>()
                    if( value!!.adsoyad == name ){
                        val basSaati = value!!.bassaati.toString()
                        val bitSaati = value!!.bitsaati.toString()
                        Log.w("HATAACCManagementONAY", "$basSaati - $bitSaati")
                        val saat = "$basSaati - $bitSaati"
                        Log.w("HATAACCManagementONAY", "$saat")
                        val tarih = value!!.izintarihi.toString()
                        val izinTip = value!!.izinTipi.toString()
                        val izinMaz = value!!.izinMazeret.toString()

                        if( value!!.yonetici1 == "ONAY BEKLIYOR" || value!!.yonetici2 == "ONAY BEKLIYOR"){
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
                        }else {
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

    fun saveData(adsoyad : String, sebeb : String, izintarihi : String, bassaati : String, bitsaati : String, yonetici1 : String, yonetici2 : String, mesaj : String, izinTipi : String, izinMazeret : String){
        val izinId = myRef.push().getKey()
        val newIzin = IzinModelSaatlik(izinId, adsoyad, sebeb, izintarihi, bassaati, bitsaati, yonetici1, yonetici2, mesaj, "0", izinTipi, izinMazeret) //days.toInt())
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

    fun getYoneticiUid(name : String){
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
                                Toast.makeText(this@IzinUserTime, "HATA OLUSTU! Lutfen Tekrar Deneyin", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@IzinUserTime, "HATA OLUSTU! Lutfen Tekrar Deneyin", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun setIzinTipiSpinnerSelectionByValue(value: String) {
        val xmlArray: Array<String> = resources.getStringArray(R.array.izin_tipi) // get array from resources
        val spinner = findViewById<Spinner>(R.id.izinTipiTimeUser) // get the spinner element

        spinner.setSelection(xmlArray.indexOf(
            xmlArray.first { elem -> elem == value } // find first element in array equal to value
        )) // get index of found element and use it as the position to set spinner to.
    }

    fun setMazeretSpinnerSelectionByValue(value: String) {
        val xmlArray: Array<String> = resources.getStringArray(R.array.mazeret) // get array from resources
        val spinner = findViewById<Spinner>(R.id.izinMazeretTimeUser) // get the spinner element

        spinner.setSelection(xmlArray.indexOf(
            xmlArray.first { elem -> elem == value } // find first element in array equal to value
        )) // get index of found element and use it as the position to set spinner to.
    }

    fun Int.toPx() : Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun sendIzin(adsoyad : String, sebeb : String, izintarihi: String, bassaati : String, bitsaati : String, izintipishow : String, izinmazeretshow : String) {
        val yonetici1durum = "ONAY BEKLIYOR"
        val yonetici2durum = "ONAY BEKLIYOR"
        val mesaj = ""

        if(adsoyad.isEmpty()){
            showMessage("Lütfen Adınız ve Soyadınız Giriniz!","Tamam")
        }else if (bassaati.isEmpty()){
            showMessage("Lütfen Başlangıç Saati Giriniz!","Tamam")
        }else if (bitsaati.isEmpty()){
            showMessage("Lütfen Bitiş Saati Giriniz!","Tamam")
        }else{
            saveData(adsoyad, sebeb, izintarihi, bassaati, bitsaati, yonetici1durum, yonetici2durum, mesaj, izintipishow, izinmazeretshow)
            //notifikasi//
            var icon = R.drawable.logo
            val iconString = icon.toString()
            val notification = Notification()
            notification.title = "Yeni Saatlık İzin Talebi Var"
            notification.body = "$adsoyad adlı kişi $izintarihi tarihi için saatlık izin talebi göndermişti"
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

    fun editIzin(adsoyad : String, sebeb : String, izintarihi: String, bassaati : String, bitsaati : String, izintipishow : String, izinmazeretshow : String) {
        if(adsoyad.isEmpty()){
            showMessage("Lütfen Adınız ve Soyadınız Giriniz!","Tamam")
        }else if (bassaati.isEmpty()){
            showMessage("Lütfen Başlangıç Tarihi Giriniz!","Tamam")
        }else if (bitsaati.isEmpty()){
            showMessage("Lütfen Bitiş Tarihi Giriniz!","Tamam")
        }else if (izintarihi.isEmpty()){
            showMessage("Lütfen Bitiş Tarihi Giriniz!","Tamam")
        }else{
            myRef.child(clickedId).child("adsoyad").setValue(adsoyad)
            myRef.child(clickedId).child("sebeb").setValue(sebeb)
            myRef.child(clickedId).child("izintarihi").setValue(izintarihi)
            myRef.child(clickedId).child("bassaati").setValue(bassaati)
            myRef.child(clickedId).child("bitsaati").setValue(bitsaati)
            myRef.child(clickedId).child("izinTipi").setValue(izintipishow)
            myRef.child(clickedId).child("izinMazeret").setValue(izinmazeretshow)

            Toast.makeText(this, "Izin Talebi Başarıyla Düzenlendi!", Toast.LENGTH_LONG).show()
            finish()
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);

        }
    }
}