package com.example.myphonebookapp.avatarapi

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface AvatarApiService {
    @GET("{avatarKey}.png")
    fun getAvatar(@Path("avatarKey") avatarKey: String): Call<ResponseBody>
}