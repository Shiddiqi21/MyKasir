package com.example.mykasir.core_ui

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(value: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(value).replace(
        "Rp", "Rp"
    ).replace(
        ",00", ""
    )
}
