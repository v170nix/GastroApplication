package net.arwix.gastro.boss.data.auth

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class CloudPrintInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) {
            // 403 // 401
            Log.e("errorResponse", response.code.toString())
        }
        return response
    }

}