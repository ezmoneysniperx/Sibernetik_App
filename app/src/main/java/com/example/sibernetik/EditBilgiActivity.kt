package com.example.sibernetik

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.edit_bilgi_activity.*
import java.text.SimpleDateFormat
import java.util.*

class EditBilgiActivity : AppCompatActivity() {

    var PREFS_KEY = "prefs"
    lateinit var sharedPreferences: SharedPreferences

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    private lateinit var auth: FirebaseAuth

    var uid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_bilgi_activity)
        auth = FirebaseAuth.getInstance()

        val user = Firebase.auth.currentUser
        if (user != null) {
            uid = user.uid
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("HATA OLUŞTU! (KULLANICI BULUNAMADI)")
            builder.setNeutralButton("Tamam"){dialogInterface , which ->
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        showInfo(uid)

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        var formatted: String = simpleDateFormat.format(Date())
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

        iseTarihEditBtn.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                var newMonth = month + 1
                var formattedMonth = "" + newMonth
                var formattedDate = "" + dayOfMonth
                if(newMonth < 10){

                    formattedMonth = "0" + newMonth;
                }
                if(dayOfMonth < 10){

                    formattedDate  = "0" + dayOfMonth ;
                }
                formatted = "$formattedDate-$formattedMonth-$year"
                tarihEditBTxt.setText(formatted).toString()
            }, year, month, day)
            dpd.show()
        }

        btnEditBilgi.setOnClickListener {
            val adsoyad = adSoyadEditBTxt.text.toString()
            val eposta = ePostaEditBTxt.text.toString()
            val tel = telNoEditBTxt.text.toString()
            val tckn = tcknEditBTxt.text.toString()
            val tarih = tarihEditBTxt.text.toString()
            val bolum = bolumEditBTxt.text.toString()
            val yonetici = yoneticiEditBTxt.text.toString()
            val gorev = gorevEditBTxt.text.toString()
            val bolumdekigorev = bolumGorevEditBTxt.text.toString()

            editInfo(adsoyad, eposta, tel, tckn, tarih, bolum, yonetici, gorev, bolumdekigorev)
        }

        btnEditSifre.setOnClickListener {
            val intent = Intent(this, EditSifreActivity::class.java)
            startActivity(intent)
        }

        btnEditImza.setOnClickListener {
            val intent = Intent(this, ImzaActivity::class.java)
            startActivity(intent)
        }

        val hesapSilButton = findViewById<Button>(R.id.hesapSilBtn)
        hesapSilButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Hesabınız silinecektir! Emin misiniz?")
            builder.setPositiveButton("Tamam"){dialogInterface , which ->
                val user = Firebase.auth.currentUser!!
                val uid = user.uid.toString()
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            myRef.child(uid).removeValue()
                            Toast.makeText(this, "Hesabınız Silindi!", Toast.LENGTH_SHORT).show()
                            val i = Intent(this, LoginActivity::class.java)
                            startActivity(i)
                            finish()
                        }
                    }
            }
            builder.setNegativeButton("Iptal"){dialogInterface , which ->
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        val anasayfaBilgiBtn = findViewById<ImageButton>(R.id.anasayfaEditBilgiBtn)

        anasayfaBilgiBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    fun showInfo(uid : String){
        myRef.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                adSoyadEditBTxt.setText(it.child("adSoyad").value.toString())
                ePostaEditBTxt.setText(it.child("eposta").value.toString())
                telNoEditBTxt.setText(it.child("telefon").value.toString())
                tcknEditBTxt.setText(it.child("tckn").value.toString())
                tarihEditBTxt.setText(it.child("tarih").value.toString())
                bolumEditBTxt.setText(it.child("bolum").value.toString())
                yoneticiEditBTxt.setText(it.child("yonetici").value.toString())
                gorevEditBTxt.setText(it.child("gorev").value.toString())
                bolumGorevEditBTxt.setText(it.child("bolumdekiGorev").value.toString())
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("HATA OLUŞTU! (VERİ BULUNAMADI)")
                builder.setNeutralButton("Tamam"){dialogInterface , which ->
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }
    }

    fun editInfo(adsoyad : String, eposta : String, tel : String, tckn : String, tarih : String, bolum : String, yonetici : String, gorev : String, bolumdekigorev : String){
        if(adsoyad.isEmpty() || eposta.isEmpty() || tel.isEmpty() || tckn.isEmpty()
            || tarih.isEmpty() || bolum.isEmpty() || yonetici.isEmpty() || gorev.isEmpty() || bolumdekigorev.isEmpty()){

            val builder = AlertDialog.Builder(this)
            builder.setMessage("Bilgileri Boş Bırakılmaz!")
            builder.setNeutralButton("Tamam"){dialogInterface , which ->
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()

        }else if(gorev != "INSAN KAYNAKLAR" && gorev != "YONETICI" && gorev != "CALIŞAN" && gorev != "STAJYER" ) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Görev Alanında INSAN KAYNAKLAR / YONETICI / CALIŞAN / STAJYER ile Doldurmalıdır!")
            builder.setNeutralButton("Tamam"){dialogInterface , which ->
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Değiştirilen Bilgiler Güncellenecektir!")
            builder.setPositiveButton("Tamam"){dialogInterface , which ->
                myRef.child(uid).child("adSoyad").setValue(adsoyad)
                myRef.child(uid).child("eposta").setValue(eposta)
                myRef.child(uid).child("telefon").setValue(tel)
                myRef.child(uid).child("tckn").setValue(tckn)
                myRef.child(uid).child("tarih").setValue(tarih)
                myRef.child(uid).child("bolum").setValue(bolum)
                myRef.child(uid).child("yonetici").setValue(yonetici)
                myRef.child(uid).child("gorev").setValue(gorev)
                myRef.child(uid).child("bolumdekiGorev").setValue(bolumdekigorev)
                Toast.makeText(this, "Güncelleme İşlemi Başarılı!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("Iptal"){dialogInterace , which ->
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }
}