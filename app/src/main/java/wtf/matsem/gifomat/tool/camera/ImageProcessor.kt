package wtf.matsem.gifomat.tool.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import com.waynejo.androidndkgif.GifEncoder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import wtf.matsem.gifomat.data.model.ImageFrame
import wtf.matsem.gifomat.data.model.ImageSequence
import wtf.matsem.gifomat.ui.main.MainPresenter
import java.io.File
import java.util.concurrent.TimeUnit

class ImageProcessor(private val cacheDir: File) {

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

					val options = BitmapFactory.Options()
					options.inPreferredConfig = Bitmap.Config.ARGB_8888

					return@concatMap Observable.just(ImageFrame(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)))
				}
				.buffer(GifomatCamera.NUMBER_IMAGES)
				.map { imageFrames: MutableList<ImageFrame> -> ImageSequence(imageFrames) }
				.takeUntil(Observable.just(true).delay(5, TimeUnit.SECONDS))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	fun createGif(imgSequence: ImageSequence): Observable<File> {
		return Observable
				.fromCallable {
					val encoder = GifEncoder()
					val outFile = File(cacheDir, "${imgSequence.images[0].timestamp.toString()}.gif")
					outFile.createNewFile()

					encoder.init(GifomatCamera.IMAGE_WIDTH, GifomatCamera.IMAGE_HEIGHT, outFile.path, GifEncoder.EncodingType.ENCODING_TYPE_SIMPLE_FAST)
					encoder.setThreadCount(2)
					encoder.setDither(true)

					for (frame in imgSequence.images) {
						encoder.encodeFrame(frame.bitmap, MainPresenter.PLAYBACK_DELAY.toInt())
					}
					encoder.close()

					return@fromCallable outFile
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}
}