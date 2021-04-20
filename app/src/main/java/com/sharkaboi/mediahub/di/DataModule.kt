package com.sharkaboi.mediahub.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.sharkaboi.mediahub.common.data.datastore.DataStoreRepository
import com.sharkaboi.mediahub.common.data.datastore.dataStore
import com.sharkaboi.mediahub.common.data.retrofit.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataModule {

    @Provides
    @Singleton
    fun getRetrofitBuilder(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.myanimelist.net/v2/")
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .addLast(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .build()

    @Provides
    @Singleton
    fun getDataStoreRepository(@ApplicationContext context: Context): DataStoreRepository =
        DataStoreRepository(context.dataStore)

    @Provides
    @Singleton
    fun getSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun getAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    fun getAnimeService(retrofit: Retrofit): AnimeService =
        retrofit.create(AnimeService::class.java)

    @Provides
    @Singleton
    fun getMangaService(retrofit: Retrofit): MangaService =
        retrofit.create(MangaService::class.java)

    @Provides
    @Singleton
    fun getUserAnimeService(retrofit: Retrofit): UserAnimeService =
        retrofit.create(UserAnimeService::class.java)

    @Provides
    @Singleton
    fun getUserMangaService(retrofit: Retrofit): UserMangaService =
        retrofit.create(UserMangaService::class.java)

    @Provides
    @Singleton
    fun getUserService(retrofit: Retrofit): UserService =
        retrofit.create(UserService::class.java)

}