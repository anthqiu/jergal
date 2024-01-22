package xyz.qhurc.jergal.util

import com.google.gson.Gson

class GsonHelper {
    companion object {
        val gson = Gson()

        inline fun <reified T: Any> fromJson(json: String): T {
            return gson.fromJson(json, T::class.java)
        }

        fun <T: Any> toJson(obj: T): String {
            return gson.toJson(obj)
        }
    }
}