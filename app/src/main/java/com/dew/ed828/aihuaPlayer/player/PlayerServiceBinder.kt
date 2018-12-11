package com.dew.ed828.aihuaPlayer.player

import android.os.Binder

/**
 *
 * Created by Edward on 12/7/2018.
 *
 */

class PlayerServiceBinder(val playerInstance: BasePlayer) : Binder()