package wtf.matsem.gifomat.ui.main

import com.google.android.things.pio.Gpio
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import wtf.matsem.gifomat.data.model.ImageFrame
import wtf.matsem.gifomat.data.model.ImageSequence
import wtf.matsem.gifomat.data.store.GifomatStore
import wtf.matsem.gifomat.hardware.PeripheralManager
import wtf.matsem.gifomat.mvp.BasePresenter
import wtf.matsem.gifomat.t
import wtf.matsem.gifomat.tool.camera.ImageProcessor
import java.util.concurrent.TimeUnit

class MainPresenter(private val peripheralManager: PeripheralManager,
					private val imageProcessor: ImageProcessor,
					private val gifomatStore: GifomatStore) : BasePresenter<MainView>() {

	companion object {
		const val TAG = "MainPresenter"
		const val BUTTON_PIN = "GPIO_37"
	}

	val disposables = CompositeDisposable()
	var imgSequenceDisposable: Disposable? = null
	var imageLooper: Disposable? = null
	var countdownTimer: Disposable? = null
	var playbackDelay = 90L

	override fun attachView(view: MainView) {
		super.attachView(view)

		getView()?.initCamera()
		getView()?.startCamPreview()
		initButton()
	}

	override fun detachView() {
		disposables.dispose()
		imgSequenceDisposable?.dispose()
		imageLooper?.dispose()
		countdownTimer?.dispose()

		peripheralManager.close()
		super.detachView()
	}

	private fun initButton() {
		val btnDisposable = peripheralManager.openInput(BUTTON_PIN)
				.filter({ gpio: Gpio -> gpio.value == true })
				.debounce(500, TimeUnit.MILLISECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({ gpio: Gpio? ->
					startCountdown()
				}, { t: Throwable? ->
					Timber.tag(TAG).e(t)
				})

		disposables.add(btnDisposable)
	}

	private fun startCountdown() {
		imageLooper?.dispose()
		countdownTimer?.dispose()
		getView()?.hidePlayer()

		val seconds = 3
		countdownTimer = Observable.interval(0, 1000L, TimeUnit.MILLISECONDS)
				.take(seconds.toLong() + 1)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnSubscribe({ getView()?.showCountdown() })
				.doOnNext({ time -> getView()?.setCountdownText("${seconds - time}") })
				.doOnComplete({
					getView()?.hideCountdown()
					captureBurst()
				})
				.subscribe()
	}

	private fun captureBurst() {
		imageLooper?.dispose()
		getView()?.hidePlayer()

		imgSequenceDisposable?.dispose()
		imgSequenceDisposable = imageProcessor.getImageSequenceObservable().subscribe(
				{ sequence: ImageSequence? -> sequence?.let { onSequenceCaptured(it) } },
				{ throwable: Throwable? -> t(TAG) { throwable } }
		)

		getView()?.triggerBurstCapture()
	}

	private fun onSequenceCaptured(sequence: ImageSequence) {
		gifomatStore.addSequence(sequence)
		restartPlayback()
	}

	private fun restartPlayback() {
		imageLooper?.dispose()

		getView()?.showPlayer()
		imageLooper = Observable.fromIterable(gifomatStore.getSequences())
				.concatMap { imageSequence -> Observable.fromIterable(imageSequence.images) }
				.concatMap { frame: ImageFrame -> Observable.just(frame).delay(playbackDelay, TimeUnit.MILLISECONDS) }
				.repeat()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						{ frame -> getView()?.playImageFrame(frame) },
						{ throwable -> Timber.tag(TAG).e(throwable) }
				)
	}
}