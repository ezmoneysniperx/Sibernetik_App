package com.example.sibernetik

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import kotlinx.android.synthetic.main.activity_arac_kullanim_user.*
import kotlinx.android.synthetic.main.izin_user_activity.*
import kotlinx.android.synthetic.main.izin_user_time.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class AracKullanimUser : AppCompatActivity(), CustomAdapter.OnItemClickListener {

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Arac Kullanim")
    val myRefUser = database.getReference("Users")
    var serverKey = "serverkey"

    val data = ArrayList<ItemsViewModel>()
    val adapter = CustomAdapter(data, this)

    var spinnerSelItem = ""

    var name = ""

    var clickedPlaka = ""
    var clickedAdsoyad = ""
    var clickedSebeb = ""
    var clickedCikTarih = ""
    var clickedCikSaat = ""
    var clickedCikKm = ""
    var clickedGirTarih = ""
    var clickedGirSaat = ""
    var clickedGirKm = ""
    var clickedOnay = ""
    var clickedId = ""

    var edit = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arac_kullanim_user)

        girTarihLayoutAracUser.layoutParams = LinearLayout.LayoutParams(
            330.toPx(),
            0.toPx()
        )

        girSaatLayoutAracUser.layoutParams = LinearLayout.LayoutParams(
            330.toPx(),
            0.toPx()
        )

        girKmTxtAracUser.layoutParams = LinearLayout.LayoutParams(
            330.toPx(),
            0.toPx()
        )

        val user = Firebase.auth.currentUser
        name = user!!.displayName.toString()
        showComments(name)

        val nameTextBox = findViewById<TextView>(R.id.adSoyadTxtAracUser)
        nameTextBox.setText(name)
        //SPINNER//

        val spinnerPlaka = findViewById<Spinner>(R.id.plakaSpinnerAracUser)
        val arrayPlaka = resources.getStringArray(R.array.arac_plaka)

        if (spinnerPlaka != null) {
            val adapterArray = ArrayAdapter(
                this,
                R.layout.spinner_list, arrayPlaka
            )
            adapterArray.setDropDownViewResource(R.layout.spinner_list)
            spinnerPlaka.adapter = adapterArray
        }

        spinnerPlaka.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ){
                spinnerSelItem = arrayPlaka[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }
        //CIKIS-GIRIS DATE SELECTOR//

        var formattedCikTarih = ""
        var formattedGirTarih = ""
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)
        val format = SimpleDateFormat("dd-MM-yyyy")

        cikTarihBtnAracUser.setOnClickListener {
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
                formattedCikTarih = "$formattedDate-$formattedMonth-$year"
                cikTarihTxtAracUser.setText(formattedCikTarih).toString()
            }, year, month, day)
            dpd.show()
        }

        girTarihBtnAracUser.setOnClickListener {
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
                formattedGirTarih = "$formattedDate-$formattedMonth-$year"
                if(cikTarihTxtAracUser.text.toString().isEmpty()){
                    showMessage("Önce Cıkış Tarihini Seçmeniz Gerekiyor!","Tamam")
                }else{
                    val days = TimeUnit.DAYS.convert(
                        format.parse(formattedGirTarih).getTime() -
                                format.parse(cikTarihTxtAracUser.text.toString()).getTime(),
                        TimeUnit.MILLISECONDS)
                    if (days < 0){
                        showMessage("Giriş tarihi, Cıkış tarihinden daha ileri ya da aynı olmalıdır!","Tamam")
                    }else{
                        girTarihTxtAracUser.setText(formattedGirTarih).toString()
                    }
                }
            }, year, month, day)
            dpd.show()
        }
        //CIKIS-GIRIS HOUR SELECTOR//

        var cikSaati = ""
        var girSaati = ""
        val formatSaat = SimpleDateFormat("HH:mm")

        cikSaatBtnAracUser.setOnClickListener{
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cikSaati = SimpleDateFormat("HH:mm").format(cal.time)

                cikSaatTxtAracUser.setText(cikSaati)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        girSaatBtnAracUser.setOnClickListener{
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                girSaati = SimpleDateFormat("HH:mm").format(cal.time)
                girSaatTxtAracUser.setText(girSaati)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        val anasayfaUserGunlukBtn = findViewById<ImageButton>(R.id.anasayfaAracUserBtn)
        anasayfaUserGunlukBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSubmitAracUser.setOnClickListener {
            val plaka = spinnerSelItem
            val name = adSoyadTxtAracUser.text.toString()
            val sebeb = sebebTxtAracUser.text.toString()
            val cikTarih = cikTarihTxtAracUser.text.toString()
            val cikSaat = cikSaatTxtAracUser.text.toString()
            val cikKm = cikKmTxtAracUser.text.toString()
            val girTarih = girTarihTxtAracUser.text.toString()
            val girSaat = girSaatTxtAracUser.text.toString()
            val girkKm = girKmTxtAracUser.text.toString()

            val ciktarihVerif = cikTarih.matches(Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}"))
            val ciksaatVerif = cikSaat.matches(Regex("[0-9]{2}:[0-9]{2}"))

            if(plaka.isEmpty() || name.isEmpty() || sebeb.isEmpty() || cikTarih.isEmpty() || cikSaat.isEmpty() || cikKm.isEmpty()){
                showMessage("Tüm Bilgileri Doldurmalıdır!", "Tamam")
            }else if (!ciksaatVerif && !ciktarihVerif){
                showMessage("Yanlış Tarih veya Saat Formatı! Lütfen GG-AA-YYYY tarih formatını ve SS:DD saat formatını kullanın!", "Tamam")
            }else{
                if(edit == 0){
                    saveTalep(plaka,name,sebeb,cikTarih,cikSaat,cikKm)
                }else if(edit == 1){
                    editTalep(plaka,name,sebeb,cikTarih,cikSaat,cikKm,girTarih,girSaat,girkKm)
                }
            }
        }

        btnTemizleAracUser.setOnClickListener {
            edit = 0

            adSoyadTxtAracUser.text.clear()
            sebebTxtAracUser.text.clear()
            cikTarihTxtAracUser.text.clear()
            cikSaatTxtAracUser.text.clear()
            cikKmTxtAracUser.text.clear()
            girTarihTxtAracUser.text.clear()
            girSaatTxtAracUser.text.clear()
            girKmTxtAracUser.text.clear()

            clickedPlaka = ""
            clickedAdsoyad = ""
            clickedSebeb = ""
            clickedCikTarih = ""
            clickedCikSaat = ""
            clickedCikKm = ""
            clickedGirTarih = ""
            clickedGirSaat = ""
            clickedGirKm = ""
            clickedOnay = ""
            clickedId = ""

            btnSubmitAracUser.setText("Gönder")
            btnSubmitAracUser.layoutParams = LinearLayout.LayoutParams(
                330.toPx(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        btnDeleteAracUser.setOnClickListener {
            if(clickedId.isEmpty() || edit == 0){
                showMessage("Herhangi bir talep Seçmediniz!", "Tamam")
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

    override fun onItemClick(position: Int) {
        //INTERFACE CHANGES//
        girTarihLayoutAracUser.layoutParams = LinearLayout.LayoutParams(
            330.toPx(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        girSaatLayoutAracUser.layoutParams = LinearLayout.LayoutParams(
            330.toPx(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        girKmTxtAracUser.layoutParams = LinearLayout.LayoutParams(
            330.toPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        btnSubmitAracUser.layoutParams = LinearLayout.LayoutParams(
            100.toPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val param1 = girTarihLayoutAracUser.layoutParams as ViewGroup.MarginLayoutParams
        param1.setMargins(0,7.toPx(),0,0)
        girTarihLayoutAracUser.layoutParams = param1

        val param2 = girSaatLayoutAracUser.layoutParams as ViewGroup.MarginLayoutParams
        param2.setMargins(0,7.toPx(),0,0)
        girSaatLayoutAracUser.layoutParams = param2

        val param3 = girKmTxtAracUser.layoutParams as ViewGroup.MarginLayoutParams
        param3.setMargins(0,7.toPx(),0,0)
        girKmTxtAracUser.layoutParams = param3

        btnSubmitAracUser.setText("Güncelle")
        //GETTING DATA//
        val clickedItem:ItemsViewModel = data[position]

        clickedPlaka = clickedItem.date.toString()
        clickedAdsoyad = clickedItem.text.toString()
        clickedSebeb = clickedItem.nedeni.toString()
        clickedCikTarih = clickedItem.time.subSequence(0,10).toString()
        clickedCikSaat = clickedItem.tip.toString()
        clickedCikKm = clickedItem.mazeret.toString()
        clickedGirTarih = clickedItem.time.subSequence(13,23).toString()
        clickedGirSaat = clickedItem.mesaj.toString()
        clickedGirKm = clickedItem.day.toString()
        clickedOnay = clickedItem.yonetici1onay.toString()
        clickedId = clickedItem.id.toString()

        val girTarihVerif = clickedGirTarih.matches(Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}"))
        if (!girTarihVerif){
            clickedGirTarih = ""
            clickedGirSaat = ""
            clickedGirKm = ""
        }

        setPlakaSpinnerSelectionByValue(clickedPlaka)
        adSoyadTxtAracUser.setText(clickedAdsoyad)
        sebebTxtAracUser.setText(clickedSebeb)
        cikTarihTxtAracUser.setText(clickedCikTarih)
        cikSaatTxtAracUser.setText(clickedCikSaat)
        cikKmTxtAracUser.setText(clickedCikKm)
        girTarihTxtAracUser.setText(clickedGirTarih)
        girSaatTxtAracUser.setText(clickedGirSaat)
        girKmTxtAracUser.setText(clickedGirKm)

        edit = 1
    }

    fun showMessage(message : String, button : String){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setNeutralButton(button){dialogInterface , which -> }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun Int.toPx() : Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun saveTalep(plaka : String, adsoyad : String, sebeb : String, ciktarih : String, ciksaat : String, cikkm : String){
        val talepId = myRef.push().getKey()
        val newTalep = AracKullanimModel(plaka,adsoyad, sebeb, ciktarih,ciksaat,cikkm,"","","","ONAY BEKLIYOR")
        myRef.child(talepId.toString()).setValue(newTalep)
        Toast.makeText(this, "Izin talebi başarıyla gönderildi!", Toast.LENGTH_LONG).show()
        finish()
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    fun editTalep(plaka : String, adsoyad : String, sebeb : String, ciktarih : String, ciksaat : String, cikkm : String, girtarih : String, girsaat : String, girkm : String){
        if(girtarih.isEmpty() && girsaat.isEmpty() && girkm.isEmpty()){
            myRef.child(clickedId).child("adsoyad").setValue(adsoyad)
            myRef.child(clickedId).child("sebeb").setValue(sebeb)
            myRef.child(clickedId).child("plaka").setValue(plaka)
            myRef.child(clickedId).child("cikisTarih").setValue(ciktarih)
            myRef.child(clickedId).child("cikisSaat").setValue(ciksaat)
            myRef.child(clickedId).child("cikisKm").setValue(cikkm)
            Toast.makeText(this, "Izin Talebi Başarıyla Düzenlendi!", Toast.LENGTH_LONG).show()
            finish()
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }else{
            val girtarihVerif = girtarih.matches(Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}"))
            val girsaatVerif = girsaat.matches(Regex("[0-9]{2}:[0-9]{2}"))
            if(girsaatVerif && girtarihVerif){
                myRef.child(clickedId).child("adsoyad").setValue(adsoyad)
                myRef.child(clickedId).child("sebeb").setValue(sebeb)
                myRef.child(clickedId).child("plaka").setValue(plaka)
                myRef.child(clickedId).child("cikisTarih").setValue(ciktarih)
                myRef.child(clickedId).child("cikisSaat").setValue(ciksaat)
                myRef.child(clickedId).child("cikisKm").setValue(cikkm)
                myRef.child(clickedId).child("girisTarih").setValue(girtarih)
                myRef.child(clickedId).child("girisSaat").setValue(girsaat)
                myRef.child(clickedId).child("girisKm").setValue(girkm)
                Toast.makeText(this, "Izin Talebi Başarıyla Düzenlendi!", Toast.LENGTH_LONG).show()
                finish()
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }else{
                showMessage("Yanlış Tarih veya Saat Formatı! Lütfen GG-AA-YYYY tarih formatını ve SS:DD saat formatını kullanın!", "Tamam")
            }
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
                    var value = postSnapshot.getValue<AracKullanimModel>()
                    if( value!!.adsoyad == name ){
                        val cikisTarih = value.cikisTarih.toString()
                        val girisTarih = value.girisTarih.toString()
                        var tarih = ""
                        if(girisTarih.isEmpty()){
                            tarih = "$cikisTarih - (GİRİLMEMİŞ)"
                        }else{
                            tarih = "$cikisTarih - $girisTarih"
                        }

                        if( value.ikOnay == "ONAY BEKLIYOR"){
                            data.add(ItemsViewModel(
                                R.drawable.bekleme,
                                value.adsoyad.toString(),
                                value.sebeb.toString(),
                                value.plaka.toString(),
                                tarih,
                                value.ikOnay.toString(),
                                "-",
                                value.girisSaat.toString(),
                                postSnapshot.key.toString(),
                                value.girisKm.toString(),
                                value.cikisSaat.toString(),
                                value.cikisKm.toString()
                            ))
                        }else if ( value.ikOnay == "ONAYLANDI"){
                            data.add(ItemsViewModel(
                                R.drawable.onaylandi,
                                value.adsoyad.toString(),
                                value.sebeb.toString(),
                                value.plaka.toString(),
                                tarih,
                                value.ikOnay.toString(),
                                "-",
                                value.girisSaat.toString(),
                                postSnapshot.key.toString(),
                                value.girisKm.toString(),
                                value.cikisSaat.toString(),
                                value.cikisKm.toString()
                            ))
                        }else {
                            data.add(ItemsViewModel(
                                R.drawable.reddeti,
                                value.adsoyad.toString(),
                                value.sebeb.toString(),
                                value.plaka.toString(),
                                tarih,
                                value.ikOnay.toString(),
                                "-",
                                value.girisSaat.toString(),
                                postSnapshot.key.toString(),
                                value.girisKm.toString(),
                                value.cikisSaat.toString(),
                                value.cikisKm.toString()
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

    fun setPlakaSpinnerSelectionByValue(value: String) {
        val xmlArray: Array<String> = resources.getStringArray(R.array.arac_plaka) // get array from resources
        val spinner = findViewById<Spinner>(R.id.plakaSpinnerAracUser) // get the spinner element

        spinner.setSelection(xmlArray.indexOf(
            xmlArray.first { elem -> elem == value } // find first element in array equal to value
        )) // get index of found element and use it as the position to set spinner to.
    }
}