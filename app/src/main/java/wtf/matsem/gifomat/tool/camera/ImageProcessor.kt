package wtf.matsem.gifomat.tool.camera

import android.graphics.BitmapFactory
import android.media.Image
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import wtf.matsem.gifomat.data.model.ImageFrame
import wtf.matsem.gifomat.data.model.ImageSequence
import java.util.concurrent.TimeUnit

class ImageProcessor {

	private val imageRelay = PublishSubject.create<Image>()

	fun onBurstImageCaptured(image: Image?) {
		image?.let { imageRelay.onNext(image) }
	}

	fun getImageSequenceObservable(): Observable<ImageSequence> {
		return imageRelay
				.concatMap { image: Image ->
					val imageBuf = image.planes[0].buffer
					val byteArray = ByteArray(imageBuf.remaining())
					imageBuf.get(byteArray)

					image.close()

					return@concatMap Observable.just(ImageFrame(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)))
				}
				.buffer(GifomatCamera.NUMBER_IMAGES)
				.map { imageFrames: MutableList<ImageFrame> -> ImageSequence(imageFrames) }
				.takeUntil(Observable.just(true).delay(5, TimeUnit.SECONDS))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}
}