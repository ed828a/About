package com.dew.ed828.aihuaPlayer.local.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.download.util.Utility
import com.dew.ed828.aihuaPlayer.util.Downloader
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.Stream
import org.schabi.newpipe.extractor.stream.VideoStream
import java.io.Serializable

/**
 * Created by Edward on 12/13/2018.
 *
 * A list adapter for a list of [streams][Stream], currently supporting [VideoStream] and [AudioStream].
 * Todo: this list should be a RecyclerViewList by Edward
 */
class StreamItemAdapter<T : Stream>(
    private val context: Context,
    private val streamsWrapper: StreamSizeWrapper<T>,
    private val showIconNoAudio: Boolean = false
) : BaseAdapter() {

    val all: List<T>
        get() = streamsWrapper.streamsList

    override fun getCount(): Int {
        return streamsWrapper.streamsList.size
    }

    override fun getItem(position: Int): T {
        return streamsWrapper.streamsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent, true)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView((parent as Spinner).selectedItemPosition, convertView, parent, false)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup, isDropdownItem: Boolean): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.stream_quality_item, parent, false)
        }

        val woSoundIconView = convertView!!.findViewById<ImageView>(R.id.wo_sound_icon)
        val formatNameView = convertView.findViewById<TextView>(R.id.stream_format_name)
        val qualityView = convertView.findViewById<TextView>(R.id.stream_quality)
        val sizeView = convertView.findViewById<TextView>(R.id.stream_size)

        val stream = getItem(position)

        var woSoundIconVisibility = View.GONE
        val qualityString: String

        if (stream is VideoStream) {
            qualityString = (stream as VideoStream).getResolution()

            if (!showIconNoAudio) {
                woSoundIconVisibility = View.GONE
            } else if ((stream as VideoStream).isVideoOnly()) {
                woSoundIconVisibility = View.VISIBLE
            } else if (isDropdownItem) {
                woSoundIconVisibility = View.INVISIBLE
            }
        } else if (stream is AudioStream) {
            qualityString = (stream as AudioStream).averageBitrate.toString() + "kbps"
        } else {
            qualityString = stream.getFormat().getSuffix()
        }

        if (streamsWrapper.getSizeInBytes(position) > 0) {
            sizeView.text = streamsWrapper.getFormattedSize(position)
            sizeView.visibility = View.VISIBLE
        } else {
            sizeView.visibility = View.GONE
        }

        formatNameView.text = stream.getFormat().getName()
        qualityView.text = qualityString
        woSoundIconView.visibility = woSoundIconVisibility

        return convertView
    }

    /**
     * A wrapper class that includes a way of storing the stream sizes.
     */
    class StreamSizeWrapper<T : Stream>(val streamsList: List<T>) : Serializable {
        private val streamSizes: LongArray

        init {
            this.streamSizes = LongArray(streamsList.size)

            for (i in streamSizes.indices) streamSizes[i] = -1
        }

        fun getSizeInBytes(streamIndex: Int): Long {
            return streamSizes[streamIndex]
        }

        fun getSizeInBytes(stream: T): Long {
            return streamSizes[streamsList.indexOf(stream)]
        }

        fun getFormattedSize(streamIndex: Int): String {
            return Utility.formatBytes(getSizeInBytes(streamIndex))
        }

        fun getFormattedSize(stream: T): String {
            return Utility.formatBytes(getSizeInBytes(stream))
        }

        fun setSize(streamIndex: Int, sizeInBytes: Long) {
            streamSizes[streamIndex] = sizeInBytes
        }

        fun setSize(stream: T, sizeInBytes: Long) {
            streamSizes[streamsList.indexOf(stream)] = sizeInBytes
        }

        companion object {
            private val EMPTY = StreamSizeWrapper(emptyList())

            /**
             * Helper method to fetch the sizes of all the streams in a wrapper.
             *
             * @param streamsWrapper the wrapper
             * @return a [Single] that returns a boolean indicating if any elements were changed
             */
            fun <X : Stream> fetchSizeForWrapper(streamsWrapper: StreamSizeWrapper<X>): Single<Boolean> {
                val fetchAndSet = {
                    var hasChanged = false
                    for (stream in streamsWrapper.streamsList) {
                        if (streamsWrapper.getSizeInBytes(stream) > 0) {
                            continue
                        }

                        val contentLength = Downloader.instance.getContentLength(stream.getUrl())
                        streamsWrapper.setSize(stream, contentLength)
                        hasChanged = true
                    }
                    hasChanged
                }

                return Single.fromCallable(fetchAndSet)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturnItem(true)
            }

            fun <X : Stream> empty(): StreamSizeWrapper<X> {

                return EMPTY as StreamSizeWrapper<X>
            }
        }
    }
}