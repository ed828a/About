package com.dew.ed828.aihuaPlayer.database

import android.arch.persistence.room.TypeConverter
import org.schabi.newpipe.extractor.stream.StreamType
import java.util.*

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

object Converters {
    /**
     * Convert a long value to a date
     * @param value the long value
     * @return the date
     */
    @JvmStatic
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? =
        if (value == null) null else Date(value)


    /**
     * Convert a date to a long value
     * @param date the date
     * @return the long value
     */
    @JvmStatic
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @JvmStatic
    @TypeConverter
    fun streamTypeOf(value: String): StreamType = StreamType.valueOf(value)


    @JvmStatic
    @TypeConverter
    fun stringOf(streamType: StreamType): String = streamType.name

}