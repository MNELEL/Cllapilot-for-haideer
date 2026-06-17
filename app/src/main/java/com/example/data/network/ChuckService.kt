package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

@JsonClass(generateAdapter = true)
data class ChuckJoke(
    @Json(name = "id") val id: String?,
    @Json(name = "value") val value: String?,
    @Json(name = "icon_url") val iconUrl: String?
)

interface ChuckNorrisApi {
    @GET("jokes/random")
    suspend fun getRandomJoke(): ChuckJoke
}

object ChuckNorrisClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.chucknorris.io/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: ChuckNorrisApi = retrofit.create(ChuckNorrisApi::class.java)
}
