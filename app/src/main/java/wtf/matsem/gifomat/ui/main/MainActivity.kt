package wtf.matsem.gifomat.ui.main

import android.app.Activity
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import butterknife.ButterKnife
import org.koin.android.ext.android.inject
import wtf.matsem.gifomat.R
import wtf.matsem.gifomat.tool.GifomatCamera

class MainActivity : Activity(), MainView {

	private val presenter by inject<MainPresenter>()
	private val camera2 by inject<GifomatCamera>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		ButterKnife.bind(this)

		actionBar.hide()
		presenter.attachView(this)
	}

	override fun onDestroy() {
		presenter.detachView()
		camera2.shutDown()
		super.onDestroy()
	}

	// region View impl

	override fun initCamera() {
		val cameraThread = HandlerThread("CameraBackground")
		cameraThread.start()

		val cameraHandler = Handler(cameraThread.looper)
		camera2.init(cameraHandler, ImageReader.OnImageAvailableListener { reader: ImageReader? ->
			// Captured images will arrive here
		})
	}

	// endregion
}
