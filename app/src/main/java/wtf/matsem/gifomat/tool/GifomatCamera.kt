package wtf.matsem.gifomat.tool

import android.graphics.ImageFormat
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import wtf.matsem.gifomat.d
import wtf.matsem.gifomat.e

class GifomatCamera(private val cameraManager: CameraManager) {

	companion object {
		const val TAG = "GifomatCamera"
		const val IMAGE_WIDTH = 640
		const val IMAGE_HEIGHT = 480
		private const val MAX_IMGREADER_IMAGES = 1
	}

	private val imageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT, ImageFormat.JPEG, MAX_IMGREADER_IMAGES)
	private var cameraDevice: CameraDevice? = null

	fun init(bgHandler: Handler, imgAvailableListener: ImageReader.OnImageAvailableListener) {
		val camIds = cameraManager.cameraIdList

		if (camIds.isEmpty()) {
			e(TAG) { "No cameras found" }
			return
		}

		val camId = camIds[0]
		d(TAG) { "Using cam id $camId" }

		imageReader.setOnImageAvailableListener(imgAvailableListener, bgHandler)
		cameraManager.openCamera(camId, openCameraCallback, bgHandler)
	}

	val openCameraCallback = object : CameraDevice.StateCallback() {
		override fun onOpened(camera: CameraDevice?) {
			d(TAG) { "Camera opened" }
			cameraDevice = camera
		}

		override fun onDisconnected(camera: CameraDevice?) {
			e(TAG) { "Camera disconnected" }
		}

		override fun onError(camera: CameraDevice?, error: Int) {
			e(TAG) { "Camera open error, errorId: $error"}
		}
	}

	fun shutDown() {
		cameraDevice?.close()
	}
}