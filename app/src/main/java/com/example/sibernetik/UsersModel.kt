package com.example.sibernetik

data class UsersModel (
    var adSoyad : String? =null,
    var ePosta : String? = null,
    var telefon : String? =null,
    var tckn : String? =null,
    var durum : String? =null,
    var gorev : String? =null,
    var izin : Int? =null,
    var tarih : String? =null,
    var sifre : String? =null,
    var isUpdated : Int? =null,
    var bolum : String? = null,
    var bolumdekiGorev : String? = null,
    var yonetici : String? = null,
    var imzaExist : Int? = null,
    var aracKullanim : String? =null
)