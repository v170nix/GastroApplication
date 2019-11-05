package net.arwix.gastro.boss.di

import android.content.Context
import com.google.gson.GsonBuilder
import net.arwix.gastro.boss.R
import net.arwix.gastro.boss.data.GoogleCloudPrintApi
import net.arwix.gastro.boss.data.auth.AccessTokenProvider
import net.arwix.gastro.boss.data.auth.GoogleAuth2Api
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

val mainModule = module {
    single(named("app_client_id")) { androidContext().resources.getString(R.string.app_client_id) }
    single(named("app_client_secret")) { androidContext().resources.getString(R.string.app_client_secret) }
    single {
        androidContext().getSharedPreferences("AppPref", Context.MODE_PRIVATE)
    }

    single {
        OkHttpClient.Builder()
//            .addInterceptor(HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            })
            .build()
    }

    single<GoogleAuth2Api> {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/oauth2/v4/")
            .client(get())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(GoogleAuth2Api::class.java)
    }

    single<GoogleCloudPrintApi> {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.google.com/cloudprint/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
        retrofit.create(GoogleCloudPrintApi::class.java)
    }

    single {
        AccessTokenProvider(
            get(named("app_client_id")),
            get(named("app_client_secret")),
            get(),
            get()
        )
    }
}