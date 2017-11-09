package wtf.matsem.gifomat.di

import android.content.Context
import android.hardware.camera2.CameraManager
import org.koin.android.module.AndroidModule
import wtf.matsem.gifomat.tool.camera.GifomatCamera
import wtf.matsem.gifomat.ui.main.MainPresenter

fun getAppModules() = listOf(MainModule())

class MainModule : AndroidModule() {
	override fun context() = applicationContext {
		context(name = "MainActivity") {
			provide { MainPresenter() }
		}

		provide { GifomatCamera(get()) }
		provide { applicationContext.getSystemService(Context.CAMERA_SERVICE) } bind CameraManager::class
	}
}