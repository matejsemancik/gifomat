package wtf.matsem.gifomat.tool.camera

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.view.Surface
import wtf.matsem.gifomat.d
import wtf.matsem.gifomat.e

class GifomatCamera(private val cameraManager: CameraManager) {

	companion object {
		const val TAG = "GifomatCamera"
		const val IMAGE_WIDTH = 640
		const val IMAGE_HEIGHT = 480
		private const val MAX_IMGREADER_IMAGES = 2
		const val NUMBER_IMAGES = 20
	}

	private val imageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT, ImageFormat.JPEG, MAX_IMGREADER_IMAGES)
	private var cameraDevice: CameraDevice? = null
	private var previewSurface: Surface? = null
	private var previewSession: CameraCaptureSession? = null
	private var bgHandler: Handler? = null

	// region Camera init

	fun init(bgHandler: Handler, previewSurface: Surface, imgAvailableListener: ImageReader.OnImageAvailableListener) {
		this.bgHandler = bgHandler
		val camIds = cameraManager.cameraIdList

		if (camIds.isEmpty()) {
			e(TAG) { "No cameras found" }
			return
		}

		val camId = camIds[0]
		d(TAG) { "Using cam id $camId" }

		this.previewSurface = previewSurface
		imageReader.setOnImageAvailableListener(imgAvailableListener, bgHandler)
		cameraManager.openCamera(camId, openCameraCallback, bgHandler)
	}

	private val openCameraCallback = object : CameraDevice.StateCallback() {
		override fun onOpened(camera: CameraDevice?) {
			d(TAG) { "Camera opened" }
			cameraDevice = camera
		}

		override fun onDisconnected(camera: CameraDevice?) {
			e(TAG) { "Camera disconnected" }
		}

		override fun onError(camera: CameraDevice?, error: Int) {
			e(TAG) { "Camera open error, errorId: $error" }
		}
	}

	// endregion

	// region Preview capture

	fun startPreview() {
		previewSession?.close()

		if (cameraDevice == null) {
			e(TAG) { "Couldn't start preview, camera device not open" }
			return
		}

		cameraDevice?.createCaptureSession(
				listOf(previewSurface, imageReader.surface),
				createSessionCallback,
				null
		)
	}

	private val createSessionCallback = object : CameraCaptureSession.StateCallback() {
		override fun onConfigured(session: CameraCaptureSession?) {
			if (cameraDevice == null) {
				e(TAG) { "Session created, but camera is already closed" }
				return
			}

			previewSession = session
			triggerPreviewCapture()
		}

		override fun onConfigureFailed(session: CameraCaptureSession?) {
			e(TAG) { "Session configuration failed" }
		}
	}

	private fun triggerPreviewCapture() {
		d(TAG) { "Triggering preview capture request" }
		val requestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

		requestBuilder?.let {
			it.addTarget(previewSurface)
			it.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
			previewSession?.setRepeatingRequest(it.build(), object : CameraCaptureSession.CaptureCallback() { /* Meh */ }, bgHandler)
		}
	}

	// endregion

	// region Burst capture

	fun captureBurst() {
		if (previewSession == null) {
			e(TAG) { "Preview session not running" }
			return
		}

		previewSession.let {
			val requestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT)

			requestBuilder?.let {
				it.addTarget(previewSurface)
				it.addTarget(imageReader.surface)
				it.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)

				val requests = mutableListOf<CaptureRequest>()
				repeat(NUMBER_IMAGES) { requests.add(requestBuilder.build()) }
				previewSession?.captureBurst(requests, object : CameraCaptureSession.CaptureCallback() { /* Meh */ }, bgHandler)
			}
		}
	}

	// endregion

	fun shutDown() {
		cameraDevice?.close()
	}
}