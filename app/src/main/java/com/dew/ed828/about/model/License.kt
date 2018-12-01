package com.dew.ed828.about.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

/**
 *
 * Created by Edward on 11/29/2018.
 *
 */

class License(val name: String?, val abbreviation: String?, val fileName: String?) : Parcelable {
    init {
        if (name == null) throw NullPointerException("name is null")
        if (abbreviation == null) throw NullPointerException("abbreviation is null")
        if (fileName == null) throw NullPointerException("filename is null")
    }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(abbreviation)
        parcel.writeString(fileName)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getContentUri(): Uri = Uri.Builder().scheme("file")
        .path("/android_asset")
        .appendPath(fileName)
        .build()

    companion object CREATOR : Parcelable.Creator<License> {
        override fun createFromParcel(parcel: Parcel): License = License(parcel)

        override fun newArray(size: Int): Array<License?> = arrayOfNulls(size)
    }
}