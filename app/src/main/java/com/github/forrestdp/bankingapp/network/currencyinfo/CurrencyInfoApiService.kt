package com.github.forrestdp.bankingapp.network.currencyinfo

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

const val BASE_URL = "https://www.cbr-xml-daily.ru/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface CurrencyInfoApiService {
    @GET("daily_json.js")
    suspend fun getCurrencyInfo(): CurrencyInfo
}

object CurrencyInfoApi {
    val retrofitService: CurrencyInfoApiService by lazy {
        retrofit.create(CurrencyInfoApiService::class.java)
    }
}