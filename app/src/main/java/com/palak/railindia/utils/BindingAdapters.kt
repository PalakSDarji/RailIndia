package com.palak.railindia.utils

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("date")
fun setDateToEditText(et : TextInputEditText, date : Date?){
    println("date is: $date")
    if(date == null) {
        et.setText("")
        return
    }
    val newDate : String = SimpleDateFormat("dd MMM yyyy").format(date)
    if(et.text?.equals(newDate) == false){
        et.setText(newDate)
    }
}

/*@InverseBindingAdapter(attribute = "date")
fun getDateFromEditText(et : TextInputEditText, date: Date) : Date{
    return Calendar.getInstance().time//sdf.parse(et.text.toString())!!
}*/

