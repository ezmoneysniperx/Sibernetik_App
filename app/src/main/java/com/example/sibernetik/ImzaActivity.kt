package com.example.sibernetik

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.drawToBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_imza.*
import kotlinx.android.synthetic.main.activity_imza.view.*
import java.io.ByteArrayOutputStream
import java.io.File

class ImzaActivity : AppCompatActivity() {

    private val imageView: ImageView by lazy {
        findViewById(R.id.imgViewImza)
    }

    private val frameLayoutContainer: FrameLayout by lazy {
        findViewById(R.id.frameLayoutImza)
    }

    private lateinit var imzaView : ImzaView

    private lateinit var bitmap: Bitmap

    val storage = Firebase.storage

    private lateinit var auth: FirebaseAuth
    val database = Firebase.database("https://sibernetik-3c2ef-default-rtdb.europe-west1.firebasedatabase.app")
    val myRef = database.getReference("Users")

    var uid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imza)

        actionBar?.hide()

        btnImzaKaydet.visibility = View.INVISIBLE

        auth = Firebase.auth
        val user = Firebase.auth.currentUser
        uid = user!!.uid

        imzaView = ImzaView(this).apply {
            setStrokeColor(Color.BLACK)
            setStrokeWidth(15f)
            setBackground(Color.WHITE)
            frameLayoutContainer.addView(this)
        }

        val storageRef = storage.reference
        val getJpgFileRef = storageRef.child("imza_gorseller/$uid.jpg")
        val localfile = File.createTempFile("tempImg","jpg")

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Resim Dosyas?? Y??kleniyor")
        progressDialog.setCancelable(false)
        progressDialog.show()

        getJpgFileRef.getFile(localfile).addOnSuccessListener {
            bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            imageView.setImageBitmap(bitmap)
            progressDialog.dismiss()
            Toast.makeText(this,"??nceki Olu??turdu??unuz ??mzan??z Bulunmu??tur!",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this,"??nceki Olu??turdu??unuz ??mzan??z Bulunmam????t??r!",Toast.LENGTH_SHORT).show()
        }

        btnTemizleImza.setOnClickListener {
            imzaView.clear()
            btnImzaKaydet.visibility = View.INVISIBLE
            imageView.setImageBitmap(null)
        }

        btnCreateImza.setOnClickListener {
            bitmap = imzaView.drawToBitmap(Bitmap.Config.ARGB_8888)
            imageView.setImageBitmap(bitmap)
            btnImzaKaydet.visibility = View.VISIBLE
        }

        btnImzaKaydet.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("??mzan??z Kaydediyor")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val saveJpgFileRef = storageRef.child("imza_gorseller/$uid.jpg")
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = saveJpgFileRef.putBytes(data)
            uploadTask.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Hata Olustu!",Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"??mzan??z Olu??turdu!",Toast.LENGTH_SHORT).show()
                myRef.child(uid).child("imzaExist").setValue(1)
            }
        }

        val anasayfaImzaBtn = findViewById<ImageButton>(R.id.anasayfaImzaBtn)

        anasayfaImzaBtn.setOnClickListener {
            val intent = Intent(this,EditBilgiActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}