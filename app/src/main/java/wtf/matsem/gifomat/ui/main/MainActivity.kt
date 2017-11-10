package wtf.matsem.gifomat.ui.main

import android.app.Activity
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import android.view.SurfaceView
import butterknife.BindView
import butterknife.ButterKnife
import org.koin.android.ext.android.inject
import wtf.matsem.gifomat.R
import wtf.matsem.gifomat.d
import wtf.matsem.gifomat.e
import wtf.matsem.gifomat.tool.callback.SimpleSurfaceHolderCallback
import wtf.matsem.gifomat.tool.camera.GifomatCamera

class MainActivity : Activity(), MainView {

	@BindView(R.id.preview_surface) lateinit var previewSurface: SurfaceView
	@BindView(R.id.playback_surface) lateinit var playbackSurface: SurfaceView

	private val presenter by inject<MainPresenter>()
	private val camera2 by inject<GifomatCamera>()

	val cameraThread = HandlerThread("CameraBackground")

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
		cameraThread.quitSafely()
		super.onDestroy()
	}

	// region View impl

	override fun initCamera() {
		cameraThread.start()

		val cameraHandler = Handler(cameraThread.looper)
		camera2.init(cameraHandler, ImageReader.OnImageAvailableListener { reader: ImageReader? ->
			// Captured images will arrive here
		})
	}

	override fun startCamPreview() {
		previewSurface.holder.addCallback(object: SimpleSurfaceHolderCallback() {
			override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
				if (holder == null) {
					e { "Preview SurfaceHolder is NULL" }
					return
				}

				d { "Preview surface changed to ${width}x${height}"}
				val rect = holder.surfaceFrame
				if (rect.width() == GifomatCamera.IMAGE_WIDTH && rect.height() == GifomatCamera.IMAGE_HEIGHT) {
					camera2.startPreview(holder.surface)
				}
			}
		})

		previewSurface.holder.setFixedSize(GifomatCamera.IMAGE_WIDTH, GifomatCamera.IMAGE_HEIGHT)
		playbackSurface.holder.setFixedSize(GifomatCamera.IMAGE_WIDTH, GifomatCamera.IMAGE_HEIGHT)
	}

	override fun triggerBurstCapture() {

	}

	// endregion
}
