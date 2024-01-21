package com.example.myphonebookapp.avatarapi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.multiavatar.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: AvatarApiService = retrofit.create(AvatarApiService::class.java)
}