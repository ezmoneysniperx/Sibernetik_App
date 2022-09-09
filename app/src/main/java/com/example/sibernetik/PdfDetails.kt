package com.example.sibernetik

import android.graphics.Bitmap


data class PdfDetails(
    val studentName:String,
    val izinDetailsList: List<IzinDetails>,
    val talepEdenImza : Bitmap,
    val yoneticiImza : Bitmap,
    val ikImza : Bitmap
)