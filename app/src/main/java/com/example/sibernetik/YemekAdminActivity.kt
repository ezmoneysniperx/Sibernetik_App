package com.example.sibernetik

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.android.synthetic.main.yemek_admin_activity.*
import java.text.SimpleDateFormat
import java.util.*

class YemekAdminActivity : AppCompatActivity() {

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Yemek")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.yemek_admin_activity)

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        val formatted: String = simpleDateFormat.format(Date())

        val tarihTxt = findViewById<TextView>(R.id.yemekTarihTxt)
        tarihTxt.setText(formatted).toString()

        val gonderBtn = findViewById<Button>(R.id.menuGirBtn)
        val anasayfaYemekBtn = findViewById<ImageButton>(R.id.anasayfaYemekGirBtn)

        anasayfaYemekBtn.setOnClickListener {
            val intent = Intent(this,YemekActivity::class.java)
            startActivity(intent)
            finish()
        }

        gonderBtn.setOnClickListener {
            val tarih = yemekTarihTxt.text.toString()
            val yemek1 = yemek1txt.text.toString()
            val yemek2 = yemek2txt.text.toString()
            val yemek3 = yemek3txt.text.toString()
            val yemek4 = yemek4txt.text.toString()

            myRef.child(tarih).get().addOnSuccessListener {
                if(it.exists()){
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("$tarih yemek bilgileri girilmiştir!")
                    builder.setNeutralButton("Tamam"){dialogInterface , which ->
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
                else {
                    if(tarih.isEmpty() || yemek1.isEmpty() || yemek2.isEmpty()){
                        Toast.makeText(this,"Hepsini Doldurmalısınız!", Toast.LENGTH_LONG).show()
                    }else{
                        saveData(tarih, yemek1, yemek2, yemek3, yemek4)
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("$tarih yemek menu bilgileri veritabanı başarıyla girdi!")
                        builder.setNeutralButton("Anasayfa'ya Dön"){dialogInterface , which ->
                            val intent = Intent(this,MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        val alertDialog: AlertDialog = builder.create()
                        alertDialog.setCancelable(false)
                        alertDialog.show()
                    }
                }
            }
        }
    }

    fun saveData(tarih : String, yemek1 : String, yemek2 : String, yemek3 : String, yemek4 : String) {
        val newYemek = YemekModel(tarih, yemek1, yemek2, yemek3, yemek4)
        myRef.child(tarih).setValue(newYemek)
    }
}