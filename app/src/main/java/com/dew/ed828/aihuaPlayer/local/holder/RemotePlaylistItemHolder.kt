package com.dew.ed828.aihuaPlayer.local.holder

import android.view.ViewGroup
import com.dew.ed828.aihuaPlayer.database.LocalItem
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistRemoteEntity
import com.dew.ed828.aihuaPlayer.local.builder.LocalItemBuilder
import com.dew.ed828.aihuaPlayer.player.Helper.ImageDisplayConstants
import com.dew.ed828.aihuaPlayer.util.Localization
import org.schabi.newpipe.extractor.NewPipe
import java.text.DateFormat

/**
 *
 * Created by Edward on 12/10/2018.
 *
 */

open class RemotePlaylistItemHolder : PlaylistItemHolder {
    constructor(infoItemBuilder: LocalItemBuilder, parent: ViewGroup) : super(infoItemBuilder, parent)

    internal constructor(infoItemBuilder: LocalItemBuilder, layoutId: Int, parent: ViewGroup) : super(infoItemBuilder, layoutId, parent)

    override fun updateFromItem(localItem: LocalItem, dateFormat: DateFormat) {
        if (localItem !is PlaylistRemoteEntity) return

        itemTitleView.text = localItem.name
        itemStreamCountView.text = localItem.streamCount.toString()
        itemUploaderView.text = if (localItem.uploader == null) ""
        else Localization.concatenateStrings(localItem.uploader!!,
            NewPipe.getNameOfService(localItem.serviceId))

        itemBuilder.displayImage(
            localItem.thumbnailUrl!!,
            itemThumbnailView,
            ImageDisplayConstants.DISPLAY_PLAYLIST_OPTIONS)

        super.updateFromItem(localItem, dateFormat)
    }
}
