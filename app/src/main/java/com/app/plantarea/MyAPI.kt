package com.app.plantarea

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MyAPI {

    @Multipart
    @POST("disease")
    fun uploadImage(
        @Part image: MultipartBody.Part
    ): Call<Number>

    companion object{
        operator fun invoke(): MyAPI{
            return Retrofit.Builder()
                .baseUrl("https://plant-area.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MyAPI::class.java)
        }
    }
}