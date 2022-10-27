package com.example.sibernetik

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
import kotlinx.android.synthetic.main.activity_arac_kullanim_admin.*
import kotlinx.android.synthetic.main.activity_arac_kullanim_admin.recyclerview
import kotlinx.android.synthetic.main.activity_gecici_gorevlendirme_admin.*
import kotlinx.android.synthetic.main.mesai_admin_activity.*

class AracKullanimAdmin : AppCompatActivity(), CustomAdapter.OnItemClickListener {

    val data = ArrayList<ItemsViewModel>()
    val adapter = CustomAdapter(data, this)

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
    var clickedUid = ""

    val database =
        Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Arac Kullanim")
    val myRefUser = database.getReference("Users")
    private lateinit var auth: FirebaseAuth
    var serverKey = "serverkey"

    var plakaSpinnerItem = ""
    var durumSpinnerItem = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arac_kullanim_admin)

        auth = Firebase.auth

        val plakaArray = resources.getStringArray(R.array.arac_plaka)
        val durumArray = resources.getStringArray(R.array.izin_durum)

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        val spinnerPlaka = findViewById<Spinner>(R.id.plakaSpinnerAracAdmin)
        if (spinnerPlaka != null) {
            val adapterArray = ArrayAdapter(
                this,
                R.layout.spinner_list, plakaArray
            )
            adapterArray.setDropDownViewResource(R.layout.spinner_list)
            spinnerPlaka.adapter = adapterArray
        }
        spinnerPlaka.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                plakaSpinnerItem = plakaArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }

        val spinnerDurum = findViewById<Spinner>(R.id.spinnerAracAdmin)
        if (spinnerDurum != null) {
            val adapterArray = ArrayAdapter(
                this,
                R.layout.spinner_list, durumArray
            )
            adapterArray.setDropDownViewResource(R.layout.spinner_list)
            spinnerDurum.adapter = adapterArray
        }
        spinnerDurum.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                durumSpinnerItem = durumArray[position]
                filtreleme(durumSpinnerItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }

        val anasayfaUserGunlukBtn = findViewById<ImageButton>(R.id.anasayfaAracAdminBtn)
        anasayfaUserGunlukBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        plakaAraBtn.setOnClickListener {
            searchPlaka(plakaSpinnerItem)
        }

        btnOnayAracAdmin.setOnClickListener {
            talepAccept(clickedId)
        }

        btnTemizleAracAdmin.setOnClickListener {
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
            clickedUid = ""

            adsoyadDisplayArac.setText("-")
            plakaDisplayArac.setText("-")
            cikTarDisplayArac.setText("-")
            girTarDisplayArac.setText("-")

            btnOnayAracAdmin.visibility = View.INVISIBLE
            btnTemizleAracAdmin.visibility = View.INVISIBLE
            btSilAracAdmin.visibility = View.INVISIBLE
        }

        btSilAracAdmin.setOnClickListener {
            if (clickedId.isEmpty()) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Herhangi bir talep seçmediniz!")
                builder.setNeutralButton("Tamam") { dialogInterface, which -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Talebi silinecektir! Emin misiniz?")
                builder.setPositiveButton("Tamam") { dialogInterface, which ->
                    myRef.child(clickedId).removeValue()
                    Toast.makeText(this, "Talebi Silindi!", Toast.LENGTH_SHORT).show()
                    finish()
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                }
                builder.setNegativeButton("Iptal") { dialogInterface, which ->
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }
    }

    override fun onItemClick(position: Int) {
        val clickedItem: ItemsViewModel = data[position]

        clickedPlaka = clickedItem.date.toString()
        clickedAdsoyad = clickedItem.text.toString()
        clickedSebeb = clickedItem.nedeni.toString()
        clickedCikTarih = clickedItem.time.subSequence(0, 10).toString()
        clickedCikSaat = clickedItem.tip.toString()
        clickedCikKm = clickedItem.mazeret.toString()
        clickedGirTarih = clickedItem.time.subSequence(13, 23).toString()
        clickedGirSaat = clickedItem.mesaj.toString()
        clickedGirKm = clickedItem.day.toString()
        clickedOnay = clickedItem.yonetici1onay.toString()
        clickedId = clickedItem.id.toString()

        myRefUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if(value!!.adSoyad == clickedAdsoyad){
                        clickedUid = postSnapshot.key.toString()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        adsoyadDisplayArac.setText(clickedAdsoyad)
        plakaDisplayArac.setText(clickedPlaka)
        cikTarDisplayArac.setText(clickedCikTarih)
        girTarDisplayArac.setText(clickedGirTarih)

        btnOnayAracAdmin.visibility = View.VISIBLE
        btnTemizleAracAdmin.visibility = View.VISIBLE
        btSilAracAdmin.visibility = View.VISIBLE
    }

    fun filtreleme(durum: String) {
        val dbRef = myRef.orderByChild("plaka")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                recyclerview.adapter = adapter
                for (postSnapshot in snapshot.children) {
                    var value = postSnapshot.getValue<AracKullanimModel>()
                    if (value!!.ikOnay.toString() == durum) {
                        val cikisTarih = value!!.cikisTarih.toString()
                        val girisTarih = value.girisTarih.toString()
                        var tarih = ""
                        if (girisTarih.isEmpty()) {
                            tarih = "$cikisTarih - (GİRİLMEMİŞ)"
                        } else {
                            tarih = "$cikisTarih - $girisTarih"
                        }
                        if (durum == "ONAY BEKLIYOR" && !value.girisTarih.toString().isEmpty()) {
                            data.add(
                                ItemsViewModel(
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
                                )
                            )
                        } else if (durum == "ONAYLANDI" && !value.girisTarih.toString().isEmpty()) {
                            data.add(
                                ItemsViewModel(
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
                                )
                            )
                        } else if (!value.girisTarih.toString().isEmpty()) {
                            data.add(
                                ItemsViewModel(
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
                                )
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun searchPlaka(plaka: String) {
        val dbRef = myRef.orderByChild("plaka")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                data.clear()
                adapter.notifyDataSetChanged()
                recyclerview.adapter = adapter
                for (postSnapshot in snapshot.children) {
                    var value = postSnapshot.getValue<AracKullanimModel>()
                    if (value!!.plaka.toString() == plaka) {
                        val cikisTarih = value!!.cikisTarih.toString()
                        val girisTarih = value.girisTarih.toString()
                        var tarih = ""
                        if (girisTarih.isEmpty()) {
                            tarih = "$cikisTarih - (GİRİLMEMİŞ)"
                        } else {
                            tarih = "$cikisTarih - $girisTarih"
                        }
                        if (value.ikOnay == "ONAY BEKLIYOR" && !value.girisTarih.toString()
                                .isEmpty()
                        ) {
                            data.add(
                                ItemsViewModel(
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
                                )
                            )
                        } else if (value.ikOnay == "ONAYLANDI" && !value.girisTarih.toString()
                                .isEmpty()
                        ) {
                            data.add(
                                ItemsViewModel(
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
                                )
                            )
                        } else if (!value.girisTarih.toString().isEmpty()) {
                            data.add(
                                ItemsViewModel(
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
                                )
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun talepAccept(id: String) {
        val durum = "ONAYLANDI"

        if (id.isEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Herhangi bir mesai seçmediniz!")
            builder.setNeutralButton("Tamam") { dialogInterface, which -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        } else if (clickedOnay == "ONAYLANDI") {
            Toast.makeText(this, "Daha önce onaylandi!", Toast.LENGTH_SHORT).show()
        } else {
            myRef.child(id).get().addOnSuccessListener {
                if (it.exists()) {
                    myRef.child(id).child("ikOnay").setValue(durum)
                    //notifikasi//
                    var icon = R.drawable.logo
                    val iconString = icon.toString()
                    val notification = Notification()
                    notification.title = "Araç Kullanım Talebi Onaylandı"
                    notification.body = "$clickedPlaka aracı için araç kullanım talebi onaylandı"
                    notification.icon = iconString
                    val firebasePush = FirebasePush.build(serverKey)
                        .setNotification(notification)
                        .setOnFinishPush {  }
                    firebasePush.sendToTopic("$clickedUid")
                    //notifikasi//
                    Toast.makeText(this, "Onaylama islemi basarili!", Toast.LENGTH_SHORT).show()
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                }
            }
        }


    }
}