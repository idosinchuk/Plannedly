package com.plannedly.model

import android.os.Parcel
import android.os.Parcelable

data class Card(
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    var labelColor: String = "",
    val startDate: Long = 0,
    val dueDate: Long = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeString(description)
        writeString(createdBy)
        writeStringList(assignedTo)
        writeString(labelColor)
        writeLong(startDate)
        writeLong(dueDate)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }


}
