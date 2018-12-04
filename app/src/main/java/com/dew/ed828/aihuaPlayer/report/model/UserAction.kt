package com.dew.ed828.aihuaPlayer.report.model

/**
 *
 * Created by Edward on 12/3/2018.
 *
 */

enum class UserAction(val message: String?) {
    USER_REPORT("user report"),
    UI_ERROR("ui error"),
    SUBSCRIPTION("subscription"),
    LOAD_IMAGE("load image"),
    SOMETHING_ELSE("something"),
    SEARCHED("searched"),
    GET_SUGGESTIONS("get suggestions"),
    REQUESTED_STREAM("requested stream"),
    REQUESTED_CHANNEL("requested channel"),
    REQUESTED_PLAYLIST("requested playlist"),
    REQUESTED_KIOSK("requested kiosk"),
    DELETE_FROM_HISTORY("delete from history"),
    PLAY_STREAM("Play stream")
}