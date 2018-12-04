package com.dew.ed828.aihuaPlayer.report.model

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.StringRes

/**
 *
 * Created by Edward on 12/3/2018.
 *
 */

class ErrorInfo (val userAction: UserAction, val serviceName: String?, val request: String?, @StringRes val message: Int): Parcelable{
    constructor(parcel: Parcel) : this(
        UserAction.valueOf(parcel.readString()!!),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userAction.name)
        parcel.writeString(serviceName)
        parcel.writeString(request)
        parcel.writeInt(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ErrorInfo> {
        override fun createFromParcel(parcel: Parcel): ErrorInfo {
            return ErrorInfo(parcel)
        }

        override fun newArray(size: Int): Array<ErrorInfo?> {
            return arrayOfNulls(size)
        }

        fun make(userAction: UserAction, serviceName: String, request: String, @StringRes message: Int): ErrorInfo {
            return ErrorInfo(userAction, serviceName, request, message)
        }
    }
}