package wtf.matsem.gifomat

import android.app.Application
import org.koin.android.ext.android.startAndroidContext
import timber.log.Timber
import wtf.matsem.gifomat.di.getAppModules

class App : Application() {

	override fun onCreate() {
		super.onCreate()

		Timber.plant(Timber.DebugTree())
		startAndroidContext(this, getAppModules())
	}
}