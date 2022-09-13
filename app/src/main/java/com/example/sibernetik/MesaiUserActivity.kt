package com.example.sibernetik

import android.app.DatePickerDialog
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
import kotlinx.android.synthetic.main.mesai_user_activity.*
import java.util.*
import kotlin.collections.ArrayList

class MesaiUserActivity : AppCompatActivity(), CustomAdapter.OnItemClickListener  {
    var PREFS_KEY = "prefs"
    var name = ""
    lateinit var sharedPreferences: SharedPreferences

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Mesai")
    val myRefUser = database.getReference("Users")
    var serverKey = "serverkey"
    val data = ArrayList<ItemsViewModel>()
    val adapter = CustomAdapter(data, this)

    var yoneticiUid = ""

    var clickedAdsoyad = ""
    var clickedKurumIci = ""
    var clickedKurumDisi = ""
    var clickedMesaiTarih = ""
    var clickedMesaiSaat = ""
    var clickedFirma = ""
    var clickedYapilan = ""
    var clickedId = ""
    var clickedOnay = ""

    var edit = 0

    override fun onItemClick(position: Int) {
        val clickedItem:ItemsViewModel = data[position]

        clickedAdsoyad = clickedItem.text
        clickedKurumIci = clickedItem.date
        clickedKurumDisi = clickedItem.nedeni
        clickedMesaiTarih = clickedItem.time.subSequence(0,10).toString()
        clickedMesaiSaat = clickedItem.tip
        clickedFirma = clickedItem.mazeret
        clickedYapilan = clickedItem.day
        clickedId = clickedItem.id
        clickedOnay = clickedItem.yonetici1onay

        if(clickedOnay == "ONAYLANDI"){
            showMessageMesai("Onaylandı Mesai Kaydı Düzenlenemez!", "Tamam")
        }else{
            nameMesaiTxt.setText(clickedAdsoyad)
            kurumIciTxt.setText(clickedKurumIci)
            kurumDisiTxt.setText(clickedKurumDisi)
            mesaiTarihTxt.setText(clickedMesaiTarih)
            saatMesaiTxt.setText(clickedMesaiSaat)
            firmaMesaiTxt.setText(clickedFirma)
            calismaAdiTxt.setText(clickedYapilan)
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
        showComments(name)

        val gonderBtn = findViewById<Button>(R.id.btnSubmitMesai)
        val nameTextBox = findViewById<TextView>(R.id.nameMesaiTxt)

        nameTextBox.setText(name)

        var formatted = ""
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

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
                mesaiTarihTxt.setText(formatted).toString()
            }, year, month, day)
            dpd.show()
        }

        gonderBtn.setOnClickListener {
            val adsoyad = nameMesaiTxt.text.toString()
            val kurumici = kurumIciTxt.text.toString()
            val kurumdisi = kurumDisiTxt.text.toString()
            val tarih = mesaiTarihTxt.text.toString()
            val saat = saatMesaiTxt.text.toString()
            val firma = firmaMesaiTxt.text.toString()
            val yapilanadi = calismaAdiTxt.text.toString()

            if(edit == 0){
                sendMesai(adsoyad, kurumici, kurumdisi, tarih, saat, firma, yapilanadi)
            }else if (edit == 1){
                editMesai(adsoyad, kurumici, kurumdisi, tarih, saat, firma, yapilanadi)
            }
        }

        btnTemizleMesai.setOnClickListener {
            nameMesaiTxt.setText(name)
            kurumIciTxt.text.clear()
            kurumDisiTxt.text.clear()
            mesaiTarihTxt.text.clear()
            saatMesaiTxt.text.clear()
            firmaMesaiTxt.text.clear()
            calismaAdiTxt.text.clear()

            clickedAdsoyad = ""
            clickedKurumIci = ""
            clickedKurumDisi = ""
            clickedMesaiTarih = ""
            clickedMesaiSaat = ""
            clickedFirma = ""
            clickedYapilan = ""
            clickedId = ""
            clickedOnay = ""

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
                        val kurIci = value.kurumIci.toString()
                        val kurDisi = value.kurumDisi.toString()
                        val trh = value.mesaiTarih.toString()
                        val saat = value.mesaiSaat.toString()
                        val yapFirma = value.yapilanFirma.toString()
                        val yapAdi = value.yapilanAdi.toString()


                        if( value.yonetici1 == "ONAY BEKLIYOR" && value.yonetici2 == "ONAY BEKLIYOR"){
                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value.adsoyad.toString(),
                                kurDisi,
                                kurIci,
                                trh,
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                "",
                                value.mesaiId.toString(),
                                yapAdi,
                                saat,
                                yapFirma

                            ))
                        }else if ( value.yonetici1 == "ONAY BEKLIYOR" && value.yonetici2 == "ONAYLANDI"){
                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value.adsoyad.toString(),
                                kurDisi,
                                kurIci,
                                trh,
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                "",
                                value.mesaiId.toString(),
                                yapAdi,
                                saat,
                                yapFirma
                            ))
                        }else if ( value.yonetici1 == "ONAYLANDI" && value.yonetici2 == "ONAYLANDI"){
                            data.add(ItemsViewModel(
                                R.drawable.onaylandi,
                                value.adsoyad.toString(),
                                kurDisi,
                                kurIci,
                                trh,
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                "",
                                value.mesaiId.toString(),
                                yapAdi,
                                saat,
                                yapFirma
                            ))
                        }else {
                            data.add(ItemsViewModel(
                                R.drawable.reddeti,
                                value.adsoyad.toString(),
                                kurDisi,
                                kurIci,
                                trh,
                                value.yonetici1.toString(),
                                value.yonetici2.toString(),
                                "",
                                value.mesaiId.toString(),
                                yapAdi,
                                saat,
                                yapFirma
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

    fun saveData(adsoyad : String, kurumIci : String, kurumDisi : String, mesaiTarih : String, mesaiSaat : String, yapilanFirma : String, yapilanAdi : String, yonetici1 : String, yonetici2 : String, mesaj : String){
        val yonetici1 = "ONAY BEKLIYOR"
        val yonetici2 = "ONAY BEKLIYOR"
        val mesaiId = myRef.push().getKey()
        val newMesai = MesaiModel(mesaiId, adsoyad, kurumIci, kurumDisi, mesaiTarih, mesaiSaat, yapilanFirma, yapilanAdi, yonetici1, yonetici2, mesaj, "")
        myRef.child(mesaiId.toString()).setValue(newMesai)
    }

    fun showMessageMesai(message : String, button : String){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setNeutralButton(button){dialogInterface , which -> }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun editMesai(adsoyad : String, kurumIci: String, kurumDisi: String, mesaiTarih : String, mesaiSaat : String, yapilanFirma : String, yapilanAdi : String) {
        if(adsoyad.isEmpty()){
            showMessageMesai("Lütfen Adınız ve Soyadınız Giriniz!","Tamam")
        }else if (kurumIci.isEmpty()){
            showMessageMesai("Lütfen Kurum İçi Mesai Giriniz!","Tamam")
        }else if (kurumDisi.isEmpty()){
            showMessageMesai("Lütfen Kurum Dişi Mesai Giriniz!","Tamam")
        }else if (mesaiTarih.isEmpty()){
            showMessageMesai("Lütfen Mesai Yapılan Tarihi Giriniz!","Tamam")
        }else if (mesaiSaat.isEmpty()){
            showMessageMesai("Lütfen Toplam Mesai Saati Giriniz!","Tamam")
        }else if (yapilanFirma.isEmpty()){
            showMessageMesai("Lütfen Kurum Çalışma Yapılan Firma Giriniz!","Tamam")
        }else if (yapilanAdi.isEmpty()){
            showMessageMesai("Lütfen Kurum Yapılan Çalışma Adı Giriniz!","Tamam")
        }else{
            myRef.child(clickedId).child("adsoyad").setValue(adsoyad)
            myRef.child(clickedId).child("kurumIci").setValue(kurumIci)
            myRef.child(clickedId).child("kurumDisi").setValue(kurumDisi)
            myRef.child(clickedId).child("mesaiTarih").setValue(mesaiTarih)
            myRef.child(clickedId).child("mesaiSaat").setValue(mesaiSaat)
            myRef.child(clickedId).child("yapilanFirma").setValue(yapilanFirma)
            myRef.child(clickedId).child("yapilanAdi").setValue(yapilanAdi)


            Toast.makeText(this, "Mesai Kaydınız Başarıyla Düzenlendi!", Toast.LENGTH_LONG).show()
            finish()
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);

        }
    }

    fun Int.toPx() : Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun sendMesai(adsoyad : String, kurumIci: String, kurumDisi: String, mesaiTarih : String, mesaiSaat : String, yapilanFirma : String, yapilanAdi : String) {
        val yonetici1durum = "ONAY BEKLIYOR"
        val yonetici2durum = "ONAY BEKLIYOR"
        val mesaj = "-"

        if(adsoyad.isEmpty()){
            showMessageMesai("Lütfen Adınız ve Soyadınız Giriniz!","Tamam")
        }else if (kurumIci.isEmpty()){
            showMessageMesai("Lütfen Kurum İçi Mesai Giriniz!","Tamam")
        }else if (kurumDisi.isEmpty()){
            showMessageMesai("Lütfen Kurum Dişi Mesai Giriniz!","Tamam")
        }else if (mesaiTarih.isEmpty()){
            showMessageMesai("Lütfen Mesai Yapılan Tarihi Giriniz!","Tamam")
        }else if (mesaiSaat.isEmpty()){
            showMessageMesai("Lütfen Toplam Mesai Saati Giriniz!","Tamam")
        }else if (yapilanFirma.isEmpty()){
            showMessageMesai("Lütfen Kurum Çalışma Yapılan Firma Giriniz!","Tamam")
        }else if (yapilanAdi.isEmpty()){
            showMessageMesai("Lütfen Kurum Yapılan Çalışma Adı Giriniz!","Tamam")
        }else{
            }
            saveData(adsoyad, kurumIci, kurumDisi, mesaiTarih, mesaiSaat, yapilanFirma, yapilanAdi, yonetici1durum, yonetici2durum, mesaj)
            //notifikasi//
            var icon = R.drawable.logo
            val iconString = icon.toString()
            val notification = Notification()
            notification.title = "Yeni Mesai Kaydı Var"
            notification.body = "$adsoyad adlı kişi $mesaiTarih tarihi için mesai kaydı göndermişti"
            notification.icon = iconString
            val firebasePush = FirebasePush.build(serverKey)
                .setNotification(notification)
                .setOnFinishPush {  }
            firebasePush.sendToTopic("$yoneticiUid")
            Log.d("yoneticiUid",yoneticiUid)
            //notifikasi//
            Toast.makeText(this, "Mesai kaydı başarıyla gönderildi!", Toast.LENGTH_LONG).show()
            finish()
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
    }
}

