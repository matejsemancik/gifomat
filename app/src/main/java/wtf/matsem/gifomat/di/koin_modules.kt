package wtf.matsem.gifomat.di

import android.content.Context
import android.hardware.camera2.CameraManager
import org.koin.android.module.AndroidModule
import wtf.matsem.gifomat.data.store.GifomatStore
import wtf.matsem.gifomat.hardware.PeripheralManager
import wtf.matsem.gifomat.tool.camera.GifomatCamera
import wtf.matsem.gifomat.tool.camera.ImageProcessor
import wtf.matsem.gifomat.ui.main.MainPresenter

fun getAppModules() = listOf(MainModule(), AppModule())

class MainModule : AndroidModule() {
	override fun context() = applicationContext {
		context(name = "MainActivity") {
			provide { MainPresenter(get(), get(), get()) }
			provide { GifomatCamera(get()) }
			provide { ImageProcessor() }
		}
	}
}

class AppModule : AndroidModule() {
	override fun context() = applicationContext {
		provide { PeripheralManager() }
		provide { GifomatStore() }
		provide { applicationContext.getSystemService(Context.CAMERA_SERVICE) } bind CameraManager::class
	}
}
