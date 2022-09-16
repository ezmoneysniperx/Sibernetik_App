package com.example.sibernetik

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.account_management.*
import kotlinx.android.synthetic.main.izin_admin_activity.recyclerview
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.izin_management.*

class AccountManagementActivity : AppCompatActivity(), MalzemeAdapter.OnItemClickListener {

    val data = ArrayList<MalzemeViewModel>()
    val adapter = MalzemeAdapter(data,this)

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    private lateinit var auth: FirebaseAuth

    var adsoyad = ""
    var eposta = ""
    var durum = ""

    var spinnerSelItem = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_management)
        auth = FirebaseAuth.getInstance()
        showAccounts()
        // Create an ArrayAdapter
        val arrayDurum = resources.getStringArray(R.array.hesap_durum)

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        // access the spinner
        val spinner = findViewById<Spinner>(R.id.spinner)
        if (spinner != null) {
            val adapterArray = ArrayAdapter(
                this,
                R.layout.spinner_list, arrayDurum
            )
            adapterArray.setDropDownViewResource(R.layout.spinner_list)
            spinner.adapter = adapterArray
        }

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ){
                spinnerSelItem = arrayDurum[position]
                accountsFilter(spinnerSelItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }

        val onayBtn = findViewById<Button>(R.id.btnHesapOnay)
        val reddetBtn = findViewById<Button>(R.id.btnHesapReddet)
        val anasayfaAccountBtn = findViewById<ImageButton>(R.id.anasayfaHesapBtn)

        anasayfaAccountBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        onayBtn.setOnClickListener {
            val intent = Intent(this,AccountOnaylaActivity::class.java)
            if(adsoyad.isEmpty()){
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Herhangi Bir Hesap Seçmediniz!")
                builder.setNeutralButton("Tamam"){dialogInterface , which -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }else{
                intent.putExtra("eposta",eposta)
                intent.putExtra("mode","ONAY")
                startActivity(intent)
                finish()
            }
        }

        reddetBtn.setOnClickListener {
            rejectAccount(eposta)
        }

        btnTemizleHesap.setOnClickListener {
            adsoyad = ""
            eposta = ""
            durum = ""

            adDisplay.setText("-")
            ePostaDisplay.setText("-")
            accDurumDisplay.setText("-")

            btnHesapOnay.visibility = View.INVISIBLE
            btnHesapReddet.visibility = View.INVISIBLE
            btnTemizleHesap.visibility = View.INVISIBLE
        }

    }

    override fun onItemClick(position: Int) {
        val clickedItem:MalzemeViewModel = data[position]

        adsoyad = clickedItem.adsoyad.toString()
        eposta = clickedItem.malzemead.toString()
        durum = clickedItem.proje.toString()

        if(durum != "ONAYLANDI"){
            adDisplay.setText(adsoyad)
            ePostaDisplay.setText(eposta)
            accDurumDisplay.setText(durum)

            btnHesapOnay.visibility = View.VISIBLE
            btnHesapReddet.visibility = View.VISIBLE
            btnTemizleHesap.visibility = View.VISIBLE
        }else{
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Bilgileri düzenleyeceğiniz kişinin adı $adsoyad, Devam etmek istiyor musunuz?")
            builder.setPositiveButton("Evet"){dialogInterface , which ->
                val intent = Intent(this,AccountOnaylaActivity::class.java)
                intent.putExtra("eposta",eposta)
                intent.putExtra("mode","EDIT")
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("Iptal"){dialogInterface , which ->
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    fun showAccounts(){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                recyclerview.adapter = adapter
                for (postSnapshot in dataSnapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if (value!!.durum.toString() == "ONAYLANDI"){
                        data.add(MalzemeViewModel(
                            R.drawable.onaylandi,
                            value!!.adSoyad.toString(),
                            value!!.ePosta.toString(),
                            value!!.durum.toString(),
                            "",
                            postSnapshot.key.toString()
                        ))
                    }else{
                        data.add(MalzemeViewModel(
                            R.drawable.bekleme,
                            value!!.adSoyad.toString(),
                            value!!.ePosta.toString(),
                            value!!.durum.toString(),
                            "",
                            postSnapshot.key.toString()
                        ))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun accountsFilter(filter : String){
        val dbRef = myRef.orderByChild("adSoyad")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                for( postSnapshot in dataSnapshot.children ){
                    var value = postSnapshot.getValue<UsersModel>()
                    if( value!!.durum == filter ){
                        if (value!!.durum.toString() == "ONAYLANDI"){
                            data.add(MalzemeViewModel(
                                R.drawable.onaylandi,
                                value!!.adSoyad.toString(),
                                value!!.ePosta.toString(),
                                value!!.durum.toString(),
                                "",
                                postSnapshot.key.toString()
                            ))
                        }else{
                            data.add(MalzemeViewModel(
                                R.drawable.bekleme,
                                value!!.adSoyad.toString(),
                                value!!.ePosta.toString(),
                                value!!.durum.toString(),
                                "",
                                postSnapshot.key.toString()
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

    fun rejectAccount (eposta: String){
        if(eposta.isEmpty()){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Herhangi Bir Hesap Seçmediniz!")
            builder.setNeutralButton("Tamam"){dialogInterface , which -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else{
            myRef.addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children){
                        var value = postSnapshot.getValue<UsersModel>()
                        if(value!!.ePosta == eposta){
                            myRef.child(postSnapshot.key.toString()).removeValue()
                            val builder = AlertDialog.Builder(this@AccountManagementActivity)
                            builder.setMessage("İşlem Başarılı!")
                            builder.setNeutralButton("Tamam"){dialogInterface , which ->
                                finish()
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);
                            }
                            val alertDialog: AlertDialog = builder.create()
                            alertDialog.setCancelable(false)
                            alertDialog.show()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}