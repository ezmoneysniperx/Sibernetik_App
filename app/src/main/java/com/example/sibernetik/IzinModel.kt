package com.example.sibernetik

data class IzinModel (
    var izinId : String? =null,
    var adsoyad : String? =null,
    var sebeb : String? =null,
    var bastarih : String? =null,
    var bittarih : String? =null,
    var yonetici1 : String? =null,
    var yonetici2 : String? =null,
    var mesaj : String? =null,
    var izinTipi : String? =null,
    var izinMazeret : String? =null,
    val day : Int? =null,
)