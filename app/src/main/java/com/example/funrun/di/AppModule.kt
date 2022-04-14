package com.example.funrun.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.funrun.database.RunDatabase
import com.example.funrun.utils.Constants.DATABASE_NAME
import com.example.funrun.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.funrun.utils.Constants.KEY_NAME
import com.example.funrun.utils.Constants.KEY_WEIGHT
import com.example.funrun.utils.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provide RunDatabase
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        RunDatabase::class.java,
        DATABASE_NAME
    ).build()

    // Provide RunDao
    @Singleton
    @Provides
    fun provideDao(database: RunDatabase) = database.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPref(
        @ApplicationContext app: Context
    ) = app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) = sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)

}