package com.example.sibernetik

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.yemek_activity.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class YemekActivity : AppCompatActivity() {

    var PREFS_KEY = "prefs"
    var EMAIL_KEY = "email"
    var name = ""

    var email = ""
    var gorev = ""

    lateinit var sharedPreferences: SharedPreferences

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Yemek")
    val myRefUser = database.getReference("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.yemek_activity)

        getGorevYemek()

        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        name = sharedPreferences.getString(EMAIL_KEY, null)!!

        val adminBtn = findViewById<Button>(R.id.yemekBilgiBtn)
        val anasayfaBtn = findViewById<ImageButton>(R.id.anasayfaYemekBtn)
        val silBtn = findViewById<Button>(R.id.yemekSilBtn)
        val tarihBtn = findViewById<Button>(R.id.tarihSecBtn)

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        var formatted: String = simpleDateFormat.format(Date())

        val tarihTxtView = findViewById<TextView>(R.id.dateYemekTxt)
        //val yemek1 = findViewById<TextView>(R.id.yemek1)
        //val yemek2 = findViewById<TextView>(R.id.yemek2)
        //val yemek3 = findViewById<TextView>(R.id.yemek3)
        //val yemek4 = findViewById<TextView>(R.id.yemek4)

        tarihTxtView.setText(formatted).toString()

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

        showYemek(formatted)

        tarihBtn.setOnClickListener {
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
                tarihTxtView.setText(formatted).toString()
                showYemek(formatted)
            }, year, month, day)
            dpd.show()
        }

        anasayfaBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        adminBtn.setOnClickListener {
            val intent = Intent(this,YemekAdminActivity::class.java)
            startActivity(intent)
            finish()
        }

        silBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("$formatted yemek menu bilgileri silinecektir!")
            builder.setPositiveButton("Tamam"){dialogInterface , which ->
                myRef.child(formatted).removeValue()
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

    fun showYemek(tarih : String){
        myRef.child(tarih).get().addOnSuccessListener {
            if (it.exists()){
                yemek1.setText(it.child("yemek1").value.toString())
                yemek2.setText(it.child("yemek2").value.toString())
                yemek3.setText(it.child("yemek3").value.toString())
                yemek4.setText(it.child("yemek4").value.toString())
            }else {
                yemek1.setText("Yemek Bilgileri Henüz Girilmemiştir!")
                yemek2.setText(" ")
                yemek3.setText(" ")
                yemek4.setText(" ")
            }
        }
    }

    fun getGorevYemek(){
        val adminBtn = findViewById<Button>(R.id.yemekBilgiBtn)
        val silBtn = findViewById<Button>(R.id.yemekSilBtn)
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
                        if(gorev == "INSAN KAYNAKLAR" || gorev == "YONETICI"){
                            adminBtn.visibility = View.VISIBLE
                            silBtn.visibility = View.VISIBLE
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