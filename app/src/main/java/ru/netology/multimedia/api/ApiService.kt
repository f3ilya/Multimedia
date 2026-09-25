package ru.netology.multimedia.api

import ru.netology.multimedia.dto.Album
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

object RetrofitClient {
    private const val BASE_URL =  "https://github.com/netology-code/andad-homeworks/raw/master/"

    val apiService: ApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("09_multimedia/data/album.json")
    suspend fun getAlbum(): Response<Album>
}