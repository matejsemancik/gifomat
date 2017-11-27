package wtf.matsem.gifomat.di

import android.content.Context
import android.hardware.camera2.CameraManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.module.AndroidModule
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import wtf.matsem.gifomat.R
import wtf.matsem.gifomat.data.store.GifomatStore
import wtf.matsem.gifomat.domain.api.GifomatService
import wtf.matsem.gifomat.hardware.PeripheralManager
import wtf.matsem.gifomat.tool.camera.GifomatCamera
import wtf.matsem.gifomat.tool.camera.ImageProcessor
import wtf.matsem.gifomat.ui.main.MainPresenter
import java.io.File

fun getAppModules() = listOf(MainModule(), AppModule())

class MainModule : AndroidModule() {
	override fun context() = applicationContext {
		context(name = "MainActivity") {
			provide { MainPresenter(get(), get(), get(), get()) }
			provide { GifomatCamera(get()) }
			provide { ImageProcessor(get()) }
			provide { context.externalCacheDir } bind File::class
		}
	}
}

class AppModule : AndroidModule() {
	override fun context() = applicationContext {
		provide { PeripheralManager() }
		provide { GifomatStore() }
		provide { applicationContext.getSystemService(Context.CAMERA_SERVICE) } bind CameraManager::class

		provide {
			val logger = HttpLoggingInterceptor()
			logger.level = HttpLoggingInterceptor.Level.HEADERS

			Retrofit.Builder()
					.baseUrl("https://slack.com/api/")
					.addConverterFactory(GsonConverterFactory.create())
					.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
					.client(OkHttpClient.Builder()
							.addInterceptor(Interceptor { chain ->
								var request = chain.request()
								request = request
										.newBuilder()
										.addHeader("Authorization", context.resources.getString(R.string.slack_auth_key))
										.build()
								return@Interceptor chain.proceed(request)
							})
							.addInterceptor(logger)
							.build())
					.build()
					.create(GifomatService::class.java)
		} bind GifomatService::class
	}
}
