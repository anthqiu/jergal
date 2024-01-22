package xyz.qhurc.jergal.util

import android.util.Log

class JergalLog {
    companion object {
        fun verbose(module: String, text: String) {
            Log.v(module, text)
        }

        fun debug(module: String, text: String) {
            Log.d(module, text)
        }

        fun warning(module: String, text: String) {
            Log.w(module, text)
        }

        fun error(module: String, text: String) {
            Log.e(module, text)
        }
    }
}