package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface SupabaseAuthService {
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<AuthResponse>

    @POST("auth/v1/token")
    suspend fun signInWithPassword(
        @Query("grant_type") grantType: String = "password",
        @Body request: SignInRequest
    ): Response<AuthResponse>

    @POST("auth/v1/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("auth/v1/user")
    suspend fun getUser(
        @Header("Authorization") token: String
    ): Response<SupabaseUser>
}

interface SupabaseRestService {
    // Bookmarks PostgREST endpoints
    @GET("rest/v1/bookmarks")
    suspend fun getBookmarks(
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<RemoteBookmarkDto>>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/bookmarks")
    suspend fun addBookmark(
        @Header("Authorization") authHeader: String,
        @Body bookmark: RemoteBookmarkDto
    ): Response<List<RemoteBookmarkDto>>

    @DELETE("rest/v1/bookmarks")
    suspend fun deleteBookmark(
        @Header("Authorization") authHeader: String,
        @Query("video_id") videoId: String,
        @Query("user_id") userId: String
    ): Response<Unit>

    // Video Comments PostgREST endpoints
    @GET("rest/v1/video_comments")
    suspend fun getComments(
        @Query("video_id") videoIdFilter: String,
        @Query("order") order: String = "created_at.desc",
        @Query("select") select: String = "*"
    ): Response<List<RemoteCommentDto>>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/video_comments")
    suspend fun postComment(
        @Header("Authorization") authHeader: String,
        @Body comment: RemoteCommentDto
    ): Response<List<RemoteCommentDto>>
}

object SupabaseNetworkClient {
    // Read from BuildConfig (injected via .env / secrets panel) or use standard mockable endpoint
    val baseUrl: String = try {
        val url = BuildConfig.SUPABASE_URL
        if (url.isNotBlank() && url.startsWith("http")) {
            if (url.endsWith("/")) url else "$url/"
        } else {
            "https://demo-streamvault.supabase.co/"
        }
    } catch (e: Throwable) {
        "https://demo-streamvault.supabase.co/"
    }

    val anonKey: String = try {
        val key = BuildConfig.SUPABASE_ANON_KEY
        if (key.isNotBlank()) key else "demo-anon-key"
    } catch (e: Throwable) {
        "demo-anon-key"
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("apikey", anonKey)
            .header("Content-Type", "application/json")
        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val authService: SupabaseAuthService by lazy {
        retrofit.create(SupabaseAuthService::class.java)
    }

    val restService: SupabaseRestService by lazy {
        retrofit.create(SupabaseRestService::class.java)
    }
}
