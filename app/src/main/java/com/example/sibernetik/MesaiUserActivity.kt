package com.example.sibernetik

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.izin_user_time.*
import kotlinx.android.synthetic.main.mesai_user_activity.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MesaiUserActivity : AppCompatActivity(), MesaiAdapter.OnItemClickListener  {
    var PREFS_KEY = "prefs"
    var name = ""
    lateinit var sharedPreferences: SharedPreferences

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Mesai")
    val myRefUser = database.getReference("Users")
var serverKey = "serverkey"

    val data = ArrayList<MesaiViewModel>()
    val adapter = MesaiAdapter(data, this)

    var yoneticiAdi = ""
    var yoneticiUid = ""

    var clickedAdsoyad = ""
    var clickedMesaiTarih = ""
    var clickedMesaiSaatBaslama = ""
    var clickedMesaiSaatBitis = ""
    var clickedMesaiSebebi = ""
    var clickedId = ""
    var clickedOnay1 = ""
    var clickedOnay2 = ""

    var edit = 0

    override fun onItemClick(position: Int) {
        val clickedItem:MesaiViewModel = data[position]

        clickedAdsoyad = clickedItem.adsoyad
        clickedMesaiTarih = clickedItem.mesaiTar.subSequence(0,10).toString()
        clickedMesaiSaatBaslama = clickedItem.timeBas
        clickedMesaiSaatBitis = clickedItem.timeBit
        clickedMesaiSebebi = clickedItem.sebeb
        clickedId = clickedItem.id
        clickedOnay1 = clickedItem.yonetici1onay
        clickedOnay2 = clickedItem.yonetici2onay

        if(clickedOnay1 == "ONAYLANDI" && clickedOnay2 == "ONAYLANDI"){
            showMessageMesai("Onaylandı Mesai Kaydı Düzenlenemez!", "Tamam")
        }else if(clickedOnay1 == "REDDETTI" && clickedOnay2 == "ONAYLANDI"){
            showMessageMesai("Reddetti Mesai Kaydı Düzenlenemez!", "Tamam")
        }else if(clickedOnay1 == "ONAYLANDI" && clickedOnay2 == "REDDETTI"){
            showMessageMesai("Reddetti Mesai Kaydı Düzenlenemez!", "Tamam")
        }else if(clickedOnay1 == "REDDETTI" && clickedOnay2 == "REDDETTI"){
            showMessageMesai("Reddetti Mesai Kaydı Düzenlenemez!", "Tamam")
        }else{
            nameMesaiTxt.setText(clickedAdsoyad)
            mesaiTarihTxt.setText(clickedMesaiTarih)
            saatMesaiTxt.setText(clickedMesaiSaatBaslama)
            saatMesaiBitisTxt.setText(clickedMesaiSaatBitis)
            mesaiSebebiTxt.setText(clickedMesaiSebebi)
            edit = 1
            Toast.makeText(this, "$clickedMesaiTarih Mesaisi Seçtiniz!", Toast.LENGTH_LONG).show()

            btnSubmitMesai.setText("Düzenle")
            btnSubmitMesai.layoutParams = LinearLayout.LayoutParams(
                100.toPx(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mesai_user_activity)

        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val user = Firebase.auth.currentUser
        name = user!!.displayName.toString()
        getYoneticiDetay()
        showComments(name)

        val gonderBtn = findViewById<Button>(R.id.btnSubmitMesai)
        val nameTextBox = findViewById<TextView>(R.id.nameMesaiTxt)

        nameTextBox.setText(name)

        var formatted = ""
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        var bugunTarihi: String = simpleDateFormat.format(Date())

        mesaiTarihTxt.setText(bugunTarihi)

        mesaiTarihBtn.setOnClickListener {
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
                val days = TimeUnit.DAYS.convert(
                    simpleDateFormat.parse(formatted).getTime() -
                            simpleDateFormat.parse(bugunTarihi).getTime(),
                    TimeUnit.MILLISECONDS)
                if (days < 0){
                    showMessageMesai("İzin tarihi, bugün tarihi olmalıdır ya da daha ileri olmalıdır!","Tamam")
                }else{
                    mesaiTarihTxt.setText(formatted).toString()
                }
                mesaiTarihTxt.setText(formatted).toString()
            }, year, month, day)
            dpd.show()
        }

        var basSaati = ""
        var bitSaati = ""
        val formatSaat = SimpleDateFormat("HH:mm")

        saatBaslamaMesaiBtn.setOnClickListener{
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                basSaati = SimpleDateFormat("HH:mm").format(cal.time)
                saatMesaiTxt.setText(basSaati)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        saatBitisMesaiBtn.setOnClickListener{
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                bitSaati = SimpleDateFormat("HH:mm").format(cal.time)
                if (basSaati.isEmpty()){
                    showMessageMesai("Önce Başlama Saatini Seçmeniz Gerekiyor!","Tamam")
                }else{
                    val minutes = TimeUnit.MINUTES.convert(
                        formatSaat.parse(bitSaati).getTime() -
                                formatSaat.parse(basSaati).getTime(),
                        TimeUnit.MILLISECONDS)
                    if (minutes < 30){
                        showMessageMesai("Bitiş saati, başlangıç saatinden daha ileri olmalıdır!","Tamam")
                    }else{
                        saatMesaiBitisTxt.setText(bitSaati)
                    }
                }
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        gonderBtn.setOnClickListener {
            val adsoyad = nameMesaiTxt.text.toString()
            val mesaiTarih = mesaiTarihTxt.text.toString()
            val saatBaslama = saatMesaiTxt.text.toString()
            val saatBitis = saatMesaiBitisTxt.text.toString()
            val sebeb = mesaiSebebiTxt.text.toString()

            val mesaitarihiVerif = mesaiTarih.matches(Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}"))
            val bassaatVerif = saatBaslama.matches(Regex("[0-9]{2}:[0-9]{2}"))
            val bitsaatVerif = saatBitis.matches(Regex("[0-9]{2}:[0-9]{2}"))

            if(mesaitarihiVerif && bassaatVerif && bitsaatVerif){
                if(edit == 0){
                    sendMesai(adsoyad, mesaiTarih, saatBaslama, saatBitis, sebeb)
                }else if (edit == 1){
                    editMesai(adsoyad, mesaiTarih, saatBaslama, saatBitis, sebeb)
                }
            }else{
                showMessageMesai("Yanlış Tarih veya Saat Formatı! Lütfen GG-AA-YYYY tarih formatını ve SS:DD saat formatını kullanın!", "Tamam")
            }


        }

        btnTemizleMesai.setOnClickListener {
            nameMesaiTxt.setText(name)
            mesaiTarihTxt.text.clear()
            saatMesaiTxt.text.clear()
            saatMesaiBitisTxt.text.clear()
            mesaiSebebiTxt.text.clear()

            clickedAdsoyad = ""
            clickedMesaiTarih = ""
            clickedMesaiSaatBaslama = ""
            clickedMesaiSaatBitis = ""
            clickedMesaiSebebi = ""
            clickedId = ""
            clickedOnay1 = ""
            clickedOnay2 = ""

            edit = 0

            btnSubmitMesai.setText("Gönder")
            btnSubmitMesai.layoutParams = LinearLayout.LayoutParams(
                330.toPx(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        btnDeleteMesai.setOnClickListener {
            if(clickedId.isEmpty() || edit == 0){
                showMessageMesai("Herhangi bir mesai Seçmediniz!", "Tamam")
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Mesai Kaydınız silinecektir! Emin misiniz?")
                builder.setPositiveButton("Tamam"){dialogInterface , which ->
                    myRef.child(clickedId).removeValue()
                    Toast.makeText(this, "Mesai Kaydınız Silindi!", Toast.LENGTH_SHORT).show()
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

        val anasayfaMesaiBtn = findViewById<ImageButton>(R.id.anasayfaMesaiBtn)
        anasayfaMesaiBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            intent.putExtra("Yetki","User")
            startActivity(intent)
            finish()
        }
    }

    fun saveData(adsoyad : String, mesaiTarih : String, mesaiSaatBas : String, mesaiSaatBit : String, mesaiSebebi : String, mesaj : String){
        val yonetici1 = "ONAY BEKLIYOR"
        val yonetici2 = "ONAY BEKLIYOR"
        val mesaiId = myRef.push().getKey()
        val newMesai = MesaiModel(mesaiId, adsoyad, mesaiTarih, mesaiSaatBas, mesaiSaatBit, mesaiSebebi, yonetici1, yonetici2, mesaj, "")
        myRef.child(mesaiId.toString()).setValue(newMesai)
    }

    fun Int.toPx() : Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun showMessageMesai(message : String, button : String){
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

    fun sendMesai(adsoyad : String, mesaiTarih : String, mesaiSaatBas : String, mesaiSaatBit : String, mesaiSebebi : String) {
        if(adsoyad.isEmpty()){
            showMessageMesai("Lütfen Adınız ve Soyadınız Giriniz!","Tamam")
        }else if (mesaiTarih.isEmpty()){
            showMessageMesai("Lütfen Mesai Yapılan Tarihi Giriniz!","Tamam")
        }else if (mesaiSaatBas.isEmpty()){
            showMessageMesai("Lütfen Başlangıç Mesai Saati Giriniz!","Tamam")
        }else if (mesaiSaatBit.isEmpty()){
            showMessageMesai("Lütfen Bitiş Mesai Saati Giriniz!","Tamam")
        }else if (mesaiSebebi.isEmpty()){
            showMessageMesai("Lütfen Mesai Sebebi Giriniz!","Tamam")
        }else {
            saveData(adsoyad, mesaiTarih, mesaiSaatBas, mesaiSaatBit, mesaiSebebi, "" )
            //notifikasi//
            var icon = R.drawable.logo
            val iconString = icon.toString()
            val notification = Notification()
            notification.title = "Yeni Mesai Kaydı Var"
            notification.body = "$adsoyad adlı kişi $mesaiTarih tarihi için mesai kaydı göndermişti"
            notification.icon = iconString
            val firebasePush = FirebasePush.build(serverKey)
                .setNotification(notification)
                .setOnFinishPush { }
            firebasePush.sendToTopic("$yoneticiUid")
            Log.d("yoneticiUid", yoneticiUid)
            //notifikasi//
            Toast.makeText(this, "Mesai kaydı başarıyla gönderildi!", Toast.LENGTH_LONG).show()
            finish()
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
    }

    fun editMesai(adsoyad : String, mesaiTarih : String, mesaiSaatBas : String, mesaiSaatBit : String, mesaiSebebi : String) {
        if(adsoyad.isEmpty()){
            showMessageMesai("Lütfen Adınız ve Soyadınız Giriniz!","Tamam")
        }else if (mesaiTarih.isEmpty()){
            showMessageMesai("Lütfen Mesai Yapılan Tarihi Giriniz!","Tamam")
        }else if (mesaiSaatBas.isEmpty()){
            showMessageMesai("Lütfen Başlangıç Mesai Saati Giriniz!","Tamam")
        }else if (mesaiSaatBit.isEmpty()){
            showMessageMesai("Lütfen Bitiş Mesai Saati Giriniz!","Tamam")
        }else if (mesaiSebebi.isEmpty()){
            showMessageMesai("Lütfen Mesai Sebebi Giriniz!","Tamam")
        }else{
            myRef.child(clickedId).child("adsoyad").setValue(adsoyad)
            myRef.child(clickedId).child("mesaiTarih").setValue(mesaiTarih)
            myRef.child(clickedId).child("mesaiSaatBaslama").setValue(mesaiSaatBas)
            myRef.child(clickedId).child("mesaiSaatBitis").setValue(mesaiSaatBit)
            myRef.child(clickedId).child("sebeb").setValue(mesaiSebebi)


            Toast.makeText(this, "Mesai Kaydınız Başarıyla Düzenlendi!", Toast.LENGTH_LONG).show()
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
                    var value = postSnapshot.getValue<MesaiModel>()
                    if( value!!.adsoyad == name ){
                        val trh = value.mesaiTarih.toString()
                        val saatBas = value.mesaiSaatBaslama.toString()
                        val saatBit = value.mesaiSaatBitis.toString()
                        val sebeb = value.sebeb.toString()


                        if( value.yonetici1 == "ONAY BEKLIYOR" && value.yonetici2 == "ONAY BEKLIYOR"){
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
                        }else if ( value.yonetici1 == "ONAY BEKLIYOR" && value.yonetici2 == "ONAYLANDI"){
                            data.add(MesaiViewModel(
                                R.drawable.bekleme,
                                value.adsoyad.toString(),
                                trh,
                                saatBas,
                                saatBit,
                                sebeb,
                                value.mesaiId.toString(),
                                value.yonetici1.toString(),
                                value.yonetici2.toString()
                            ))
                        }else if ( value.yonetici1 == "ONAYLANDI" && value.yonetici2 == "ONAYLANDI"){
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
                        }else {
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
                null
            }
        })
    }
}

