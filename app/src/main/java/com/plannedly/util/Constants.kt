package com.plannedly.util

import android.app.Activity
import android.net.Uri
import android.webkit.MimeTypeMap
import com.plannedly.R

object Constants {
    const val USERS: String = "users"
    const val BOARDS: String = "boards"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val ASSIGNED_TO: String = "assignedTo"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val DOCUMENT_ID: String = "documentID"
    const val TASK_LIST: String = "taskList"
    const val BOARD_DETAIL: String = "board_detail"
    const val ID: String = "id"
    const val EMAIL: String = "email"
    const val BOARD_MEMBERS_LIST: String = "board_members_list"
    const val SELECT: String = "select"
    const val UN_SELECT: String = "unselect"

    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"

    const val POJECTMANAGER_PREFERENCES = "Project_manager_preferences"
    const val FCM_TOKEN_UPDATED = "fcm_token_updated"
    const val FCM_TOKEN = "fcm_token"

    const val FCM_BASE_URL: String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION: String = "authorization"
    const val FCM_KEY: String = "key"
    const val FCM_SERVER_KEY: String = R.string.fcm_key.toString()
    const val FCM_KEY_TITLE: String = "title"
    const val FCM_KEY_MESSAGE: String = "message"
    const val FCM_KEY_DATA: String = "data"
    const val FCM_KEY_TO: String = "to"
    const val DATE_FORMAT: String = "dd/MM/yyyy"

    const val START_DATE_PICKER: String = "StartDatePicker"
    const val DUE_DATE_PICKER: String = "DueDatePicker"

    const val BOARD_IMAGE_PATH: String = "BOARD_IMAGE"
    const val FIREBASE_IMAGE_URL: String = "Firebase Image URL"
    const val DOWNLOADED_IMAGE_URL: String = "Downloadable Image URL"
    const val USER_IMAGE_PATH: String = "USER_IMAGE"


    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}