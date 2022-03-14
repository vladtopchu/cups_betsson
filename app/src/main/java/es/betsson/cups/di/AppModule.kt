package es.betsson.cups.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.betsson.cups.utils.ConnectionLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import es.betsson.cups.utils.SharedPref
import javax.inject.Qualifier
import javax.inject.Singleton

@Module()
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideConnectionLivedata(
        @ApplicationContext context: Context
    ) = ConnectionLiveData(context)

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideSharedPref(@ApplicationContext context: Context)
            = SharedPref(context)

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope