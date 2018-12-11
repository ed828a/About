package com.dew.ed828.aihuaPlayer.local.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.database.EdPlayerDatabase
import com.dew.ed828.aihuaPlayer.database.stream.model.StreamEntity
import com.dew.ed828.aihuaPlayer.local.playlist.LocalPlaylistManager
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.dialog_playlist_name.view.*

/**
 *
 * Created by Edward on 12/10/2018.
 *
 */


class PlaylistCreationDialog : PlaylistDialog() {

    ///////////////////////////////////////////////////////////////////////////
    // Dialog
    ///////////////////////////////////////////////////////////////////////////

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (streams == null) return super.onCreateDialog(savedInstanceState)

        // custom Dialog layout.
        val dialogView = View.inflate(context, R.layout.dialog_playlist_name, null)
        val nameInput = dialogView.dialogPlaylistName

        val dialogBuilder = AlertDialog.Builder(context)
            .setTitle(R.string.create_playlist)
            .setView(dialogView) // custom Dialog layout
            .setCancelable(true)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.create) { dialogInterface, i ->
                val name = nameInput.text.toString().trim()
                val playlistManager = LocalPlaylistManager(EdPlayerDatabase.getInstance(context!!))
                val successToast = Toast.makeText(activity,
                    R.string.playlist_creation_success,
                    Toast.LENGTH_LONG)

                playlistManager.createPlaylist(name, streams!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { longs ->
                        successToast.show()
                        Log.d(TAG, "$name Playlist has been successfully created.")
                    }
            }

        return dialogBuilder.create()
    }

    companion object {
        private val TAG = PlaylistCreationDialog::class.java.canonicalName

        fun newInstance(streams: List<StreamEntity>): PlaylistCreationDialog {
            val dialog = PlaylistCreationDialog()
            dialog.setInfo(streams)
            return dialog
        }
    }
}
