package com.example.sibernetik

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PDFConverterAracKullanim {

    val storage = Firebase.storage

    private fun createBitmapFromView(
        context: Context,
        view: View,
        pdfDetails: PdfDetails,
        adapter: MarksRecyclerAdapter,
        activity: Activity
    ): Bitmap {
        val studentName = view.findViewById<TextView>(R.id.txt_student_name)
        val recyclerView = view.findViewById<RecyclerView>(R.id.pdf_marks)
        val imzaTalepEden = view.findViewById<ImageView>(R.id.imgImzaTalepEden)
        val imzaIK = view.findViewById<ImageView>(R.id.imgImzaIK)

        studentName.text = pdfDetails.studentName
        recyclerView.adapter = adapter
        imzaTalepEden.setImageBitmap(pdfDetails.talepEdenImza)
        imzaIK.setImageBitmap(pdfDetails.ikImza)

        return createBitmap(context, view, activity)
    }

    private fun createBitmap(
        context: Context,
        view: View,
        activity: Activity
    ): Bitmap {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealMetrics(displayMetrics)
            displayMetrics.densityDpi
        } else {
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        view.measure(
            View.MeasureSpec.makeMeasureSpec(
                1748, View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                2480, View.MeasureSpec.EXACTLY
            )
        )
        //view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, 1748 , 2480 )
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight, Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return Bitmap.createScaledBitmap(bitmap, 900 , 1442 , true)
    }

    private fun convertBitmapToPdf(bitmap: Bitmap, context: Context, fileName : String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0F, 0F, null)
        pdfDocument.finishPage(page)
        val filePath = File(context.getExternalFilesDir(null), "$fileName.pdf")
        pdfDocument.writeTo(FileOutputStream(filePath))
        pdfDocument.close()
        renderPdf(context, filePath, fileName)
    }

    fun createPdf(
        context: Context,
        pdfDetails: PdfDetails,
        activity: Activity,
        fileName: String
    ) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_pdf_arac, null)

        val adapter = MarksRecyclerAdapter(pdfDetails.izinDetailsList)
        val bitmap = createBitmapFromView(context, view, pdfDetails, adapter, activity)
        convertBitmapToPdf(bitmap, activity, fileName)
    }


    private fun renderPdf(context: Context, filePath: File, fileName : String) {
        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            filePath
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, "application/pdf")

        var storageRef = storage.reference
        val directory = "Arac_Kullanim"
        val pdfFileRef = storageRef.child("$directory/$fileName.pdf")
        val stream = FileInputStream(File(filePath.toString()))
        val uploadTask = pdfFileRef.putStream(stream)
        uploadTask.addOnFailureListener {
            Log.w("Hata","Gagal Upload")
        }.addOnSuccessListener { taskSnapshot ->
            Log.w("Success","Sukses Upload")
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {

        }
    }

}