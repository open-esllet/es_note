package com.es.note.utils

import android.util.Log

class LogUtil {

    companion object {
        val TAG = "esnote"

        @JvmStatic
        fun d(tag:String, msg:String) {
           Log.d(TAG, "${tag} $msg")
        }

        @JvmStatic
        fun d(tag:String, msg:String, e:Throwable) {
            Log.d(TAG, "${tag} $msg", e)
        }
    }
}