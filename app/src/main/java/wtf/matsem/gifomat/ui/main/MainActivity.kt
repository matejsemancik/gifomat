package wtf.matsem.gifomat.ui.main

import android.animation.ValueAnimator
import android.app.Activity
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import org.koin.android.ext.android.inject
import wtf.matsem.gifomat.*
import wtf.matsem.gifomat.data.model.ImageFrame
import wtf.matsem.gifomat.tool.callback.SimpleSurfaceHolderCallback
import wtf.matsem.gifomat.tool.camera.GifomatCamera
import wtf.matsem.gifomat.tool.camera.ImageProcessor

class MainActivity : Activity(), MainView {

	@BindView(R.id.preview_surface) lateinit var previewSurface: SurfaceView
	@BindView(R.id.playback_surface) lateinit var playbackSurface: SurfaceView
	@BindView(R.id.status_text) lateinit var statusText: TextView

	@BindView(R.id.playback_info_layout) lateinit var playbackInfoLayout: LinearLayout
	@BindView(R.id.playback_sequence_info) lateinit var playbackSequenceInfo: TextView
	@BindView(R.id.playback_frame_info) lateinit var playbackFrameInfo: TextView

	private val presenter by inject<MainPresenter>()
	private val camera2 by inject<GifomatCamera>()
	private val imageProcessor by inject<ImageProcessor>()

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

	override fun setStatusIdle() {
		statusText.text = resources.getString(R.string.status_idle)
		statusText.setBackgroundColor(resources.getColor(R.color.bluegreen, theme))
	}

	override fun setStatusRecording() {
		statusText.text = resources.getString(R.string.status_recording)
		statusText.setBackgroundColor(resources.getColor(R.color.red_rose, theme))
	}

	override fun setStatusPlayback() {
		statusText.text = resources.getString(R.string.status_playback)
		statusText.setBackgroundColor(resources.getColor(R.color.yellow_crayola, theme))
	}

	override fun setStatusCountdown(howMuch: Int) {
		statusText.scaleX = 1.5f
		statusText.scaleY = 1.5f

		val animator = ValueAnimator.ofFloat(1.5f, 1.0f).setDuration(300)
		animator.interpolator = DecelerateInterpolator()
		animator.addUpdateListener({ animation ->
			statusText.scaleX = animation.animatedValue as Float
			statusText.scaleY = animation.animatedValue as Float
		})

		statusText.text = resources.getString(R.string.status_countdown, howMuch)
		statusText.setBackgroundColor(resources.getColor(R.color.bluegreen, theme))
		animator.start()
	}

	override fun showPlaybackInfo() {
		playbackInfoLayout.setVisible()
		playbackSequenceInfo.text = ""
		playbackFrameInfo.text = ""
	}

	override fun hidePlaybackInfo() {
		playbackInfoLayout.setGone()
	}

	override fun setPlaybackSeqInfo(text: String) {
		playbackSequenceInfo.text = text
	}

	override fun setPlaybackFrameInfo(text: String) {
		playbackFrameInfo.text = text
	}

	override fun initCamera() {
		cameraThread.start()

		val cameraHandler = Handler(cameraThread.looper)
		camera2.init(cameraHandler, previewSurface.holder.surface, ImageReader.OnImageAvailableListener { reader: ImageReader? ->
			// Burst captures arrive here
			imageProcessor.onBurstImageCaptured(reader?.acquireLatestImage())
		})
	}

	override fun startCamPreview() {
		previewSurface.holder.addCallback(object : SimpleSurfaceHolderCallback() {
			override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
				if (holder == null) {
					e { "Preview SurfaceHolder is NULL" }
					return
				}

				d { "Preview surface changed to ${width}x${height}" }
				val rect = holder.surfaceFrame
				if (rect.width() == GifomatCamera.IMAGE_WIDTH && rect.height() == GifomatCamera.IMAGE_HEIGHT) {
					camera2.startPreview()
				}
			}
		})

		previewSurface.holder.setFixedSize(GifomatCamera.IMAGE_WIDTH, GifomatCamera.IMAGE_HEIGHT)
		playbackSurface.holder.setFixedSize(GifomatCamera.IMAGE_WIDTH, GifomatCamera.IMAGE_HEIGHT)
	}

	override fun triggerBurstCapture() {
		camera2.captureBurst()
	}

	override fun showPlayer() {
		playbackSurface.visibility = View.VISIBLE
	}

	override fun playImageFrame(frame: ImageFrame) {
		val canvas = playbackSurface.holder.lockCanvas()
		canvas.drawBitmap(frame.bitmap, 0f, 0f, null)
		playbackSurface.holder.unlockCanvasAndPost(canvas)
	}

	override fun hidePlayer() {
		playbackSurface.visibility = View.GONE
	}

	// endregion

	// region UI events

	// endregion
}
