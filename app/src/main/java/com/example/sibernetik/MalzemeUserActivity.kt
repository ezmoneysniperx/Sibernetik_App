package com.example.sibernetik

import android.content.Context
import android.content.Intent

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import kotlinx.android.synthetic.main.malzeme_activity.*
import kotlinx.android.synthetic.main.malzeme_activity.recyclerview

class MalzemeUserActivity : AppCompatActivity(), MalzemeAdapter.OnItemClickListener  {
    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Malzeme")

    var ad = ""
    lateinit var sharedPreferences: SharedPreferences

    val data = ArrayList<MalzemeViewModel>()
    val adapter = MalzemeAdapter(data,this)

    var spinnerProjeItem = ""

    override fun onItemClick(position: Int) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.malzeme_activity)

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        val user = Firebase.auth.currentUser
        ad = user!!.displayName.toString()
        showComments(ad)

        val spinnerProje = findViewById<Spinner>(R.id.projeAdiSpinnerMalz)
        val arrayProje = resources.getStringArray(R.array.proje_malzeme)

        if (spinnerProje != null) {
            val adapterArray = ArrayAdapter(
                this,
                R.layout.spinner_list, arrayProje
            )
            adapterArray.setDropDownViewResource(R.layout.spinner_list)
            spinnerProje.adapter = adapterArray
        }

        spinnerProje.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ){
                spinnerProjeItem = arrayProje[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }


        val gonderBtn = findViewById<Button>(R.id.btnSubmit)
        val nameTextBox = findViewById<TextView>(R.id.nameTxt)
        val anasayfaUserBtn = findViewById<ImageButton>(R.id.anasayfaMalzemeUserBtn)

        nameTextBox.setText(ad)

        gonderBtn.setOnClickListener {
            val adsoyad = nameTxt.text.toString()
            val malzemead = malzemeAdi.text.toString()
            val proje = spinnerProjeItem
            val fiyat = fiyat.text.toString()
            saveData(adsoyad,malzemead,proje,fiyat)

            myRef.child(adsoyad).get().addOnSuccessListener {
                Toast.makeText(this, "Malzeme talebi başarıyla gönderildi!", Toast.LENGTH_LONG).show()
                finish()
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        }

        anasayfaUserBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    fun saveData(adSoyad : String, malzemead : String, proje : String, fiyat : String) {
        val id = myRef.push().getKey()
        val newUser = MalzemeModel(adSoyad, malzemead, proje, fiyat, id)
        myRef.child(id.toString()).setValue(newUser)
    }
    fun showComments(adSoyad: String){
        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                recyclerview.adapter = adapter
                adapter.notifyDataSetChanged()
                for (postSnapshot in dataSnapshot.children){
                    val value = postSnapshot.getValue<MalzemeModel>()
                    if(value!!.adSoyad.toString() == adSoyad)
                    data.add( MalzemeViewModel(R.drawable.tools2,
                        value!!.adSoyad.toString(),
                        value!!.malzemead.toString(),
                        value!!.proje.toString(),
                        value!!.fiyat.toString(),
                    ""))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                null
            }
        })
    }
}