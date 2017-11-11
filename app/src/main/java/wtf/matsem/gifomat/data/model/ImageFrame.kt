package wtf.matsem.gifomat.data.model

import android.graphics.Bitmap

data class ImageFrame(
		val bitmap: Bitmap,
		val timestamp: Long = System.currentTimeMillis()
)