package es.betsson.cups

import android.app.Application
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BetssonApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        // Logs
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal
        OneSignal.initWithContext(this);
        OneSignal.setAppId("636d73f3-d6ff-4b17-a9d0-7c6b3a4103b7");
    }
}