package com.example.sibernetik

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.Constants
import kotlinx.android.synthetic.main.account_onayla_activity.*

class AccountOnaylaActivity : AppCompatActivity() {

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    private lateinit var auth: FirebaseAuth
    lateinit var progressDialog: ProgressDialog

    var spinnerSelItem = ""
    var uid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_onayla_activity)
        auth = Firebase.auth

        val bundle = intent.extras
        var adsoyad = ""
        if (bundle != null) {
            adsoyad = bundle.getString("adSoyad").toString()
        }

        val arrayDurum = resources.getStringArray(R.array.hesap_gorev)
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
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }

        adSoyadOnayTxt.setText(adsoyad)
        getDetails(adsoyad)

        btnOnay.setOnClickListener {
            val eposta = ePostaOnayTxt.text.toString()
            val telefon = telNoOnayTxt.text.toString()
            val tckn = tcknOnayTxt.text.toString()
            val iseBasTar = tarihOnayTxt.text.toString()
            val bolum = bolumOnayTxt.text.toString()
            val yonetici = yoneticiOnayTxt.text.toString()
            val gorev = spinnerSelItem.toString()
            val bolumdekiGorev = gorevBolumOnayTxt.text.toString()

            var detector = 0

            progressDialog = ProgressDialog(this@AccountOnaylaActivity)
            progressDialog.setMessage("Yükleniyor..")
            progressDialog.setCancelable(false) // blocks UI interaction
            progressDialog.show()

            val postRef = myRef.orderByChild("adSoyad").equalTo(yonetici)
            postRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot!=null && snapshot.getChildren()!=null &&
                        snapshot.getChildren().iterator().hasNext() && detector == 0) {
                        detector = 1
                        accAccount(adsoyad, eposta, telefon, tckn, iseBasTar, bolum, yonetici, gorev, bolumdekiGorev)
                    }else if (detector != 1){
                        progressDialog.dismiss()
                        Toast.makeText(this@AccountOnaylaActivity,"Yazdığınız Yöneticiye Ait Uygulamada Hesap Bulunamadı!",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        val anasayfaOnaylaBtn = findViewById<ImageButton>(R.id.anasayfaOnaylaBtn)

        anasayfaOnaylaBtn.setOnClickListener {
            val intent = Intent(this,AccountManagementActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun getDetails(adsoyad : String) {
        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if(value!!.adSoyad.toString() == adsoyad){
                        ePostaOnayTxt.setText(value!!.ePosta.toString())
                        telNoOnayTxt.setText(value!!.telefon.toString())
                        tcknOnayTxt.setText(value!!.tckn.toString())
                        tarihOnayTxt.setText(value!!.tarih.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AccountOnaylaActivity,"Hata Olustu!",Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun accAccount(adsoyad: String, ePosta: String, telefon: String, tckn: String, iseBasTar : String, bolum : String, yonetici : String, gorev : String, bolumdekiGorev : String){
        myRef.addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children){
                        var value = postSnapshot.getValue<UsersModel>()
                        if(value!!.ePosta == ePosta && value!!.durum != "ONAYLANDI"){
                            var sifre = value!!.sifre.toString()

                            val firebaseDefaultApp = Firebase.auth.app
                            val signUpAppName = firebaseDefaultApp.name + "_signUp"

                            val signUpApp = try {
                                FirebaseApp.initializeApp(
                                    this@AccountOnaylaActivity,
                                    firebaseDefaultApp.options,
                                    signUpAppName
                                )
                            } catch (e: IllegalStateException) {
                                // IllegalStateException is throw if an app with the same name has already been initialized.
                                FirebaseApp.getInstance(signUpAppName)
                            }

                            val signUpFirebaseAuth = Firebase.auth(signUpApp)

                            signUpFirebaseAuth.createUserWithEmailAndPassword(ePosta, sifre)
                                .addOnCompleteListener(this@AccountOnaylaActivity){ task ->
                                    if (task.isSuccessful){
                                        val user = task.result.user
                                        uid = user!!.uid
                                        Log.w("data", "$uid nya nih")
                                        val profileUpdates = userProfileChangeRequest {
                                            displayName = adsoyad
                                        }
                                        user!!.updateProfile(profileUpdates)
                                            .addOnCompleteListener { task -> }
                                        sifre = HashUtils.sha256(sifre)
                                        Log.w("data", "sebelum saveData")
                                        saveData(adsoyad, ePosta, telefon, tckn, iseBasTar, sifre,"ONAYLANDI", gorev, bolum, yonetici, uid, bolumdekiGorev)
                                        Log.w("data", "sebelum delete")
                                        myRef.child(tckn).removeValue()
                                        Log.w("data", "abis delete")
                                        progressDialog.dismiss()
                                        val builder = AlertDialog.Builder(this@AccountOnaylaActivity)
                                        builder.setMessage("İşlem Başarılı!")
                                        builder.setNeutralButton("Tamam"){dialogInterface , which ->
                                            signUpFirebaseAuth.signOut()
                                            signUpApp.delete()
                                            val intent = Intent(this@AccountOnaylaActivity,AccountManagementActivity::class.java)
                                            finish()
                                            overridePendingTransition(0, 0);
                                            startActivity(intent);
                                            overridePendingTransition(0, 0);
                                        }
                                        val alertDialog: AlertDialog = builder.create()
                                        alertDialog.setCancelable(false)
                                        alertDialog.show()
                                    }else{
                                        progressDialog.dismiss()
                                        //Toast.makeText(this@AccountOnaylaActivity,"Hata Oluştu! Lütfen Tekrar Deneyin",Toast.LENGTH_SHORT).show()
                                        Log.w("ErrorACCManagementONAY", "createUserWithEmail:failure", task.exception)
                                    }
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun saveData(adSoyad : String, ePosta : String, telefon : String, tckn : String, tarih : String, sifre : String, durum : String, gorev : String, bolum : String, yonetici : String, uid : String, bolumdekiGorev : String) {
        val newUser = UsersModel(adSoyad, ePosta, telefon, tckn, durum, gorev,0, tarih, sifre, 0, bolum, bolumdekiGorev, yonetici,0)
        myRef.child(uid).setValue(newUser)
    }
}