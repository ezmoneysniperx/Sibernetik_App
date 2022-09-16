package com.example.sibernetik

import android.app.DatePickerDialog
import android.app.ProgressDialog
import kotlinx.android.synthetic.main.register_activity.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fcm.androidtoandroid.FirebasePush
import fcm.androidtoandroid.model.Notification
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity(){

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    private lateinit var auth: FirebaseAuth
    lateinit var progressDialog: ProgressDialog
var serverKey = "serverkey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)
        auth = FirebaseAuth.getInstance()

        val tarihBtn = findViewById<Button>(R.id.iseTarihBtn)

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        var formatted = ""
        val tarihTxtView = findViewById<TextView>(R.id.tarihTxt)

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

        tarihBtn.setOnClickListener {
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
                tarihTxtView.setText(formatted).toString()
            }, year, month, day)
            dpd.show()
        }

        val button = findViewById<Button>(R.id.btnRegister)
        button.setOnClickListener{

            val name = adSoyadTxt.text.toString()
            val email = ePostaTxt.text.toString().trim()
            val tel = telNoTxt.text.toString()
            val tckn = tcknTxt.text.toString()
            val trh = tarihTxt.text.toString()
            val pwd = passwordTxt.text.toString()

            val trhVerif = trh.matches(Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}"))

            if(name.isEmpty()){
                showMessage("Lütfen Adınızı ve Soyadınızı Giriniz!","Tamam")
            }else if(email.isEmpty()){
                showMessage("Lütfen E-Posta Adresinizi Giriniz!","Tamam")
            }else if(tel.isEmpty()){
                showMessage("Lütfen Telefon Giriniz!","Tamam")
            }else if(tckn.isEmpty()){
                showMessage("Lütfen TC Kimlik Numaranızı Giriniz!","Tamam")
            }else if(pwd.isEmpty()) {
                showMessage("Lütfen Şifrenizi Giriniz!", "Tamam")
            }else if (trh.isEmpty()){
                showMessage("Lütfen İşe Başlangıç Tarihi Seçiniz!", "Tamam")
            }else if (pwd.length < 6) {
                showMessage("Şifrenizi En Az 6 Karakterli Olması Gerekiyor!", "Tamam")
            }else if (!trhVerif){
                showMessage("Yanlış Tarih Formatı! Lütfen GG-AA-YYYY tarih formatını kullanın!", "Tamam")
            }else {
                progressDialog = ProgressDialog(this@RegisterActivity)
                progressDialog.setMessage("Hesabınız Oluşturuluyor")
                progressDialog.setCancelable(false) // blocks UI interaction
                progressDialog.show()

                var detected = 0
                val postRef = myRef.orderByChild("eposta").equalTo(email)
                postRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot!=null && dataSnapshot.getChildren()!=null &&
                            dataSnapshot.getChildren().iterator().hasNext() && detected == 0){
                            progressDialog.hide()
                            showMessage("Girdiğiniz E-Posta Kayıtlı Bir Hesap Mevcuttur! Lütfen Giriş Yap!","Tamam")
                        }else if (detected != 1){
                            detected = 1
                            Log.d("test","Sebelum savedata")
                            saveData(name, email, tel, tckn, trh, pwd, "")
                            Log.d("test","Setelah savedata")
                            //notifikasi//
                            var icon = R.drawable.logo
                            val iconString = icon.toString()
                            val notification = Notification()
                            notification.title = "Yeni Onay Bekleyen Hesap Var"
                            notification.body = "$name adlı kişi uygulamamıza yeni kayıt oldu"
                            notification.icon = iconString
                            val firebasePush = FirebasePush.build(serverKey)
                                .setNotification(notification)
                                .setOnFinishPush {  }
                            firebasePush.sendToTopic("IK")
                            Log.d("test","Setelah Notif Sebelum Builder")
                            //notifikasi//
                            val builder = AlertDialog.Builder(this@RegisterActivity)
                            builder.setMessage("Kaydınız Başarılı, Giriş Yapabilmeniz İçin Yöneticinin Onayını Beklemeniz Gerekmektedir.!")
                            builder.setNeutralButton("Tamam"){dialogInterface , which ->
                                Log.d("test","Dalem Builder")
                                val intent = Intent(this@RegisterActivity,LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            val alertDialog: AlertDialog = builder.create()
                            alertDialog.setCancelable(false)
                            Log.d("test","sebelum isFinishing")
                            if(!isFinishing()){
                                progressDialog.hide()
                                Log.d("test","Dalem isFinishing")
                                alertDialog.show()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        showMessage("Hata Oluştu! Lütfen Tekrar Yapın!","Tamam")
                    }
                })
            }
            }
    }
    fun saveData(adSoyad : String, ePosta : String, telefon : String, tckn : String, tarih : String, sifre : String, gorev : String) {
        val durum = "ONAY BEKLIYOR"
        val newUser = UsersModel(adSoyad, ePosta, telefon, tckn, durum, gorev,0, tarih, sifre,)
        myRef.child(tckn).setValue(newUser)
    }

    fun showMessage(message : String, button : String){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setNeutralButton(button){dialogInterface , which -> }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
