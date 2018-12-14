package com.dew.ed828.aihuaPlayer.infolist.adapter

import android.app.Activity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.dew.ed828.aihuaPlayer.infolist.builder.InfoItemBuilder
import com.dew.ed828.aihuaPlayer.infolist.holder.*
import com.dew.ed828.aihuaPlayer.player.Helper.FallbackViewHolder
import com.dew.ed828.aihuaPlayer.util.OnClickGesture
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/**
 * Created by Edward on 12/13/2018.
 */

class InfoListAdapter(activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val infoItemBuilder: InfoItemBuilder
    val itemsList: ArrayList<InfoItem>
    private var useMiniVariant = false
    private var useGridVariant = false
    private var showFooter = false
    private var header: View? = null
    private var footer: View? = null

    inner class HFHolder(var view: View) : RecyclerView.ViewHolder(view)

    init {
        infoItemBuilder = InfoItemBuilder(activity)
        itemsList = ArrayList()
    }

    fun setOnStreamSelectedListener(listener: OnClickGesture<StreamInfoItem>) {
        infoItemBuilder.onStreamSelectedListener = listener
    }

    fun setOnChannelSelectedListener(listener: OnClickGesture<ChannelInfoItem>) {
        infoItemBuilder.onChannelSelectedListener = listener
    }

    fun setOnPlaylistSelectedListener(listener: OnClickGesture<PlaylistInfoItem>) {
        infoItemBuilder.onPlaylistSelectedListener = listener
    }

    fun useMiniItemVariants(useMiniVariant: Boolean) {
        this.useMiniVariant = useMiniVariant
    }

    fun setGridItemVariants(useGridVariant: Boolean) {
        this.useGridVariant = useGridVariant
    }

    fun addInfoItemList(data: List<InfoItem>?) {
        if (data != null) {

            Log.d(TAG, "addInfoItemList() before > infoItemList.size() = ${itemsList.size}, data.size() = ${data.size}")


            val offsetStart = sizeConsideringHeaderOffset()
            itemsList.addAll(data)

            Log.d(TAG, "addInfoItemList() after > offsetStart = " + offsetStart + ", infoItemList.size() = " + itemsList.size + ", header = " + header + ", footer = " + footer + ", showFooter = " + showFooter)

            notifyItemRangeInserted(offsetStart, data.size)

            if (footer != null && showFooter) {
                val footerNow = sizeConsideringHeaderOffset()
                notifyItemMoved(offsetStart, footerNow)

                Log.d(TAG, "addInfoItemList() footer from $offsetStart to $footerNow")
            }
        }
    }

    fun addInfoItem(data: InfoItem?) {
        if (data != null) {
            Log.d(TAG, "addInfoItem() before > infoItemList.size() = " + itemsList.size + ", thread = " + Thread.currentThread())

            val positionInserted = sizeConsideringHeaderOffset()
            itemsList.add(data)

            Log.d(TAG, "addInfoItem() after > position = " + positionInserted + ", infoItemList.size() = " + itemsList.size + ", header = " + header + ", footer = " + footer + ", showFooter = " + showFooter)
            notifyItemInserted(positionInserted)

            if (footer != null && showFooter) {
                val footerNow = sizeConsideringHeaderOffset()
                notifyItemMoved(positionInserted, footerNow)

                Log.d(TAG, "addInfoItem() footer from $positionInserted to $footerNow")
            }
        }
    }

    fun clearStreamItemList() {
        if (itemsList.isEmpty()) {
            return
        }
        itemsList.clear()
        notifyDataSetChanged()
    }

    fun setHeader(header: View?) {
        val changed = header !== this.header
        this.header = header
        if (changed) notifyDataSetChanged()
    }

    fun setFooter(view: View) {
        this.footer = view
    }

    fun showFooter(show: Boolean) {
        Log.d(TAG, "showFooter() called with: show = [$show]")
        if (show == showFooter) return

        showFooter = show
        if (show)
            notifyItemInserted(sizeConsideringHeaderOffset())
        else
            notifyItemRemoved(sizeConsideringHeaderOffset())
    }


    private fun sizeConsideringHeaderOffset(): Int {
        val i = itemsList.size + if (header != null) 1 else 0
        Log.d(TAG, "sizeConsideringHeaderOffset() called → $i")
        return i
    }

    override fun getItemCount(): Int {
        var count = itemsList.size
        if (header != null) count++
        if (footer != null && showFooter) count++

        Log.d(TAG, "getItemCount() called, count = " + count + ", infoItemList.size() = " + itemsList.size + ", header = " + header + ", footer = " + footer + ", showFooter = " + showFooter)

        return count
    }

    override fun getItemViewType(position: Int): Int {
        var position = position
        Log.d(TAG, "getItemViewType() called with: position = [$position]")

        if (header != null && position == 0) {
            return HEADER_TYPE
        } else if (header != null) {
            position--
        }
        if (footer != null && position == itemsList.size && showFooter) {
            return FOOTER_TYPE
        }
        val item = itemsList[position]
        return when (item.infoType) {
            InfoItem.InfoType.STREAM -> if (useGridVariant) GRID_STREAM_HOLDER_TYPE else if (useMiniVariant) MINI_STREAM_HOLDER_TYPE else STREAM_HOLDER_TYPE
            InfoItem.InfoType.CHANNEL -> if (useGridVariant) GRID_CHANNEL_HOLDER_TYPE else if (useMiniVariant) MINI_CHANNEL_HOLDER_TYPE else CHANNEL_HOLDER_TYPE
            InfoItem.InfoType.PLAYLIST -> if (useGridVariant) GRID_PLAYLIST_HOLDER_TYPE else if (useMiniVariant) MINI_PLAYLIST_HOLDER_TYPE else PLAYLIST_HOLDER_TYPE
            else -> {
                Log.e(TAG, "Trollolo")
                -1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "onCreateViewHolder() called with: parent = [$parent], type = [$type]")
        return when (type) {
            HEADER_TYPE -> HFHolder(header!!)
            FOOTER_TYPE -> HFHolder(footer!!)
            MINI_STREAM_HOLDER_TYPE -> StreamMiniInfoItemHolder(infoItemBuilder, parent)
            STREAM_HOLDER_TYPE -> StreamInfoItemHolder(infoItemBuilder, parent)
            GRID_STREAM_HOLDER_TYPE -> StreamGridInfoItemHolder(infoItemBuilder, parent)
            MINI_CHANNEL_HOLDER_TYPE -> ChannelMiniInfoItemHolder(infoItemBuilder, parent)
            CHANNEL_HOLDER_TYPE -> ChannelInfoItemHolder(infoItemBuilder, parent)
            GRID_CHANNEL_HOLDER_TYPE -> ChannelGridInfoItemHolder(infoItemBuilder, parent)
            MINI_PLAYLIST_HOLDER_TYPE -> PlaylistMiniInfoItemHolder(infoItemBuilder, parent)
            PLAYLIST_HOLDER_TYPE -> PlaylistInfoItemHolder(infoItemBuilder, parent)
            GRID_PLAYLIST_HOLDER_TYPE -> PlaylistGridInfoItemHolder(infoItemBuilder, parent)
            else -> {
                Log.e(TAG, "Trollolo")
                FallbackViewHolder(View(parent.context))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var position = position

        Log.d(TAG, "onBindViewHolder() called with: holder = [" + holder.javaClass.simpleName + "], position = [" + position + "]")
        if (holder is InfoItemHolder) {
            // If header isn't null, offset the items by -1
            if (header != null) position--

            holder.updateFromItem(itemsList[position])
        } else if (holder is HFHolder && position == 0 && header != null) {
            holder.view = header!!
        } else if (holder is HFHolder && position == sizeConsideringHeaderOffset() && footer != null && showFooter) {
            holder.view = footer!!
        }
    }

    fun getSpanSizeLookup(spanCount: Int): GridLayoutManager.SpanSizeLookup {
        return object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val type = getItemViewType(position)
                return if (type == HEADER_TYPE || type == FOOTER_TYPE) spanCount else 1
            }
        }
    }

    companion object {
        private val TAG = InfoListAdapter::class.java.simpleName

        private const val HEADER_TYPE = 0
        private const val FOOTER_TYPE = 1

        private const val MINI_STREAM_HOLDER_TYPE = 0x100
        private const val STREAM_HOLDER_TYPE = 0x101
        private const val GRID_STREAM_HOLDER_TYPE = 0x102
        private const val MINI_CHANNEL_HOLDER_TYPE = 0x200
        private const val CHANNEL_HOLDER_TYPE = 0x201
        private const val GRID_CHANNEL_HOLDER_TYPE = 0x202
        private const val MINI_PLAYLIST_HOLDER_TYPE = 0x300
        private const val PLAYLIST_HOLDER_TYPE = 0x301
        private const val GRID_PLAYLIST_HOLDER_TYPE = 0x302
    }
}
