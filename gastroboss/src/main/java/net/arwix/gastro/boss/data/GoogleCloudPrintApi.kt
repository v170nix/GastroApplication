package net.arwix.gastro.boss.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*


interface GoogleCloudPrintApi {

    @GET("search")
    suspend fun getPrinters(@Header("Authorization") token: String): Printers

    @FormUrlEncoded
    @POST("submit")
    suspend fun submitPrintJob(
        @Header("Authorization") token: String,
        @Field("xsrf") xsrf: String,
        @Field("printerid") printerID: String,
        @Field("title") title: String,
        @Field("ticket") ticket: String,
        @Field("content") content: String,
        @Field("contentType") contentType: String
    )

    @FormUrlEncoded
    @POST("submit")
    suspend fun submitFilePrintJob(
        @Header("Authorization") token: String,
        @Field("contentTransferEncoding") encoded: String = "base64",
        @Field("xsrf") xsrf: String,
        @Field("printerid") printerID: String,
        @Field("title") title: String,
        @Field("ticket") ticket: String,
        @Field("content", encoded = false) content: String,
        @Field("contentType") contentType: String
    ): Me

    @Multipart
    @POST("submit")
    suspend fun submitFilePrintJob2(
        @Header("Authorization") token: String,
        @Part("xsrf") xsrf: String,
        @Part("printerid") printerID: String,
        @Part("title") title: String,
        @Part("ticket") ticket: String,
        @Part("content", encoding = "base64") content: RequestBody,
        @Part("contentType") contentType: String
    ): Me

    @POST("submit")
    suspend fun testSub(
        @Header("Authorization") token: String,
        @Body parts: MultipartBody
        ): Me
}