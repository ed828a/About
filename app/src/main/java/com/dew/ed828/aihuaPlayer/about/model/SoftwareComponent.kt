package com.dew.ed828.aihuaPlayer.about.model

import android.os.Parcel
import android.os.Parcelable

/**
 *
 * Created by Edward on 11/29/2018.
 *
 */

class SoftwareComponent(val name: String?, val years: String?, val copyrightOwner: String?, val link: String?, val license: License? = null): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(License::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(years)
        parcel.writeString(copyrightOwner)
        parcel.writeString(link)
        parcel.writeParcelable(license, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SoftwareComponent> {
        override fun createFromParcel(parcel: Parcel): SoftwareComponent = SoftwareComponent(parcel)

        override fun newArray(size: Int): Array<SoftwareComponent?> = arrayOfNulls(size)
    }
}