package com.example.sibernetik

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import fcm.androidtoandroid.FirebasePush
import kotlinx.android.synthetic.main.account_onayla_activity.*
import fcm.androidtoandroid.model.Notification

class AccountOnaylaActivity : AppCompatActivity() {

    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")
    private lateinit var auth: FirebaseAuth
    lateinit var progressDialog: ProgressDialog
    var arrayYonetici = ArrayList<String>()
var serverKey = "serverkey"

    var spinnerSelItem = ""
    var spinnerYoneticiItem = ""
    var uid = ""
    var editUid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_onayla_activity)
        auth = Firebase.auth
        val arrayDurum = resources.getStringArray(R.array.hesap_gorev)

        getYoneticiList()

        val bundle = intent.extras
        var eposta = ""
        var mode = ""
        if (bundle != null) {
            eposta = bundle.getString("eposta").toString()
            mode = bundle.getString("mode").toString()
            if (mode == "EDIT"){
                btnOnay.setText("DÜZENLE")
                getDetailsEdit(eposta)
            }else if (mode == "ONAY"){
                getDetails(eposta)
            }
        }

        val spinner = findViewById<Spinner>(R.id.spinner)
        val spinnerYonetici = findViewById<Spinner>(R.id.spinnerYonetici)

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
                if(position > 0){
                    spinnerSelItem = arrayDurum[position]
                }else{
                    spinnerSelItem = ""
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }

        spinnerYonetici.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ){
                if(position > 0){
                    spinnerYoneticiItem = arrayYonetici[position]
                }else{
                    spinnerYoneticiItem = ""
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                null
            }
        }

        ePostaOnayTxt.setText(eposta)

        btnOnay.setOnClickListener {
            val adsoyadText = adSoyadOnayTxt.text.toString()
            val eposta = ePostaOnayTxt.text.toString()
            val telefon = telNoOnayTxt.text.toString()
            val tckn = tcknOnayTxt.text.toString()
            val iseBasTar = tarihOnayTxt.text.toString()
            val bolum = bolumOnayTxt.text.toString()
            val yonetici = spinnerYoneticiItem
            val gorev = spinnerSelItem
            val bolumdekiGorev = gorevBolumOnayTxt.text.toString()

            val iseBasTarVerif = iseBasTar.matches(Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}"))

            progressDialog = ProgressDialog(this@AccountOnaylaActivity)
            progressDialog.setMessage("Yükleniyor..")
            progressDialog.setCancelable(false) // blocks UI interaction
            progressDialog.show()

            if(adsoyadText.isEmpty() || eposta.isEmpty() || telefon.isEmpty() || tckn.isEmpty() || iseBasTar.isEmpty()
                || bolum.isEmpty() || yonetici.isEmpty() || gorev.isEmpty() || bolumdekiGorev.isEmpty()){
                progressDialog.dismiss()
                Toast.makeText(this@AccountOnaylaActivity,"Formdaki tüm alanı doldurmanız gerekmektedir!",Toast.LENGTH_SHORT).show()
            }else if (iseBasTarVerif){
                if(mode == "ONAY"){
                    progressDialog.dismiss()
                    accAccount(adsoyadText, eposta, telefon, tckn, iseBasTar, bolum, yonetici, gorev, bolumdekiGorev)
                }else if(mode == "EDIT"){
                    progressDialog.dismiss()
                    editAccount(adsoyadText, eposta, telefon, tckn, iseBasTar, bolum, yonetici, gorev, bolumdekiGorev)
                }
            }else{
                progressDialog.dismiss()
                Toast.makeText(this@AccountOnaylaActivity,"Yanlış Tarih Formatı! Lütfen GG-AA-YYYY tarih formatını kullanın!",Toast.LENGTH_SHORT).show()
            }
        }

        val anasayfaOnaylaBtn = findViewById<ImageButton>(R.id.anasayfaOnaylaBtn)

        anasayfaOnaylaBtn.setOnClickListener {
            val intent = Intent(this,AccountManagementActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun getDetails(eposta : String) {
        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if(value!!.ePosta.toString() == eposta){
                        adSoyadOnayTxt.setText(value!!.adSoyad.toString())
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

    fun getDetailsEdit(eposta: String){
        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if(value!!.ePosta.toString() == eposta){
                        var bolum = value!!.bolum.toString()
                        var bolumdekiGorev = value!!.bolumdekiGorev.toString()
                        var gorev = value!!.gorev.toString()
                        var yonetici = value!!.yonetici.toString()

                        if (bolum == "null"){
                            bolum = ""
                        }else if(bolumdekiGorev == "null"){
                            bolumdekiGorev = ""
                        }else if(gorev == "null"){
                            gorev = ""
                        }else if (yonetici == "null"){
                            yonetici = ""
                        }

                        adSoyadOnayTxt.setText(value!!.adSoyad.toString())
                        telNoOnayTxt.setText(value!!.telefon.toString())
                        tcknOnayTxt.setText(value!!.tckn.toString())
                        tarihOnayTxt.setText(value!!.tarih.toString())
                        bolumOnayTxt.setText(bolum)
                        gorevBolumOnayTxt.setText(bolumdekiGorev)
                        setSpinnerGorevSelectionByValue(gorev)
                        setSpinnerYoneticiSelectionByValue(yonetici)
                        editUid = postSnapshot.key.toString()
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

    fun getYoneticiList(){
        progressDialog = ProgressDialog(this@AccountOnaylaActivity)
        progressDialog.setMessage("Yükleniyor..")
        progressDialog.setCancelable(false) // blocks UI interaction
        progressDialog.show()

        arrayYonetici.add("Yönetici Seçin...")
        val spinnerYonetici = findViewById<Spinner>(R.id.spinnerYonetici)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    var value = postSnapshot.getValue<UsersModel>()
                    if (value!!.gorev == "YONETICI"){
                        arrayYonetici.add(value!!.adSoyad.toString())
                    }
                    if (spinnerYonetici != null) {
                        val adapterArray1 = ArrayAdapter(
                            this@AccountOnaylaActivity,
                            R.layout.spinner_list, arrayYonetici
                        )
                        adapterArray1.setDropDownViewResource(R.layout.spinner_list)
                        spinnerYonetici.adapter = adapterArray1
                    }
                }
                progressDialog.dismiss()
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun setSpinnerGorevSelectionByValue(value: String) {
        val xmlArray: Array<String> = resources.getStringArray(R.array.hesap_gorev) // get array from resources
        val spinner = findViewById<Spinner>(R.id.spinner) // get the spinner element

        spinner.setSelection(xmlArray.indexOf(
            xmlArray.first { elem -> elem == value } // find first element in array equal to value
        )) // get index of found element and use it as the position to set spinner to.
    }

    fun setSpinnerYoneticiSelectionByValue(value: String) {
        val spinner = findViewById<Spinner>(R.id.spinnerYonetici) // get the spinner element

        spinner.setSelection(arrayYonetici.indexOf(
            arrayYonetici.first { elem -> elem == value } // find first element in array equal to value
        )) // get index of found element and use it as the position to set spinner to.
    }

    fun editAccount(adsoyad: String, ePosta: String, telefon: String, tckn: String, iseBasTar : String, bolum : String, yonetici : String, gorev : String, bolumdekiGorev : String){
        val user = Firebase.auth.currentUser
        val userAdSoyad = user!!.displayName.toString()
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Değiştirilen Bilgiler Güncellenecektir!")
        builder.setPositiveButton("Tamam"){dialogInterface , which ->
            myRef.child(editUid).child("adSoyad").setValue(adsoyad)
            myRef.child(editUid).child("eposta").setValue(ePosta)
            myRef.child(editUid).child("telefon").setValue(telefon)
            myRef.child(editUid).child("tckn").setValue(tckn)
            myRef.child(editUid).child("tarih").setValue(iseBasTar)
            myRef.child(editUid).child("bolum").setValue(bolum)
            myRef.child(editUid).child("yonetici").setValue(yonetici)
            myRef.child(editUid).child("gorev").setValue(gorev)
            myRef.child(editUid).child("bolumdekiGorev").setValue(bolumdekiGorev)
            //notifikasi//
            var icon = R.drawable.logo
            val iconString = icon.toString()
            val notification = Notification()
            notification.title = "Hesap Bilgi Düzenleme"
            notification.body = "Hesap bilgileriniz $userAdSoyad tarafından düzenlendi"
            notification.icon = iconString
            val firebasePush = FirebasePush.build(serverKey)
                .setNotification(notification)
                .setOnFinishPush {  }
            firebasePush.sendToTopic("$editUid")
            //notifikasi//
            Toast.makeText(this, "Güncelleme İşlemi Başarılı!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AccountManagementActivity::class.java)
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