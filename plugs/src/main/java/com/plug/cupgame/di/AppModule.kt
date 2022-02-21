package com.plug.cupgame.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import com.plug.cupgame.data.local.CupsDatabase
import com.plug.cupgame.data.local.daos.ProgressDao
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: CupsDatabase.Callback
    ) = Room.databaseBuilder(app, CupsDatabase::class.java, "cupgame_database")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Singleton
    @Provides
    fun provideProgressDao(db: CupsDatabase): ProgressDao = db.progressDao()

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope