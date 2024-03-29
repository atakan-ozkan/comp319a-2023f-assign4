package com.example.myphonebookapp.client
import com.example.myphonebookapp.api.AvatarApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.multiavatar.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: AvatarApiService = retrofit.create(AvatarApiService::class.java)
}