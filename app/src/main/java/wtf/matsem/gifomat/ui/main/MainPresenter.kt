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
		const val PLAYBACK_DELAY = 110L
	}

	enum class State {
		IDLE, COUNTDOWN, RECORDING, PLAYBACK
	}

	var state = State.IDLE

	val disposables = CompositeDisposable()
	var imgSequenceDisposable: Disposable? = null
	var imageLooper: Disposable? = null
	var countdownTimer: Disposable? = null

	override fun attachView(view: MainView) {
		super.attachView(view)

		state = State.IDLE
		getView()?.setStatusIdle()
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
				.subscribe({ _: Gpio? ->
					startCountdown()
				}, { t: Throwable? ->
					Timber.tag(TAG).e(t)
				})

		disposables.add(btnDisposable)
	}

	private fun startCountdown() {
		stopPlayback()
		countdownTimer?.dispose()
		getView()?.setStatusIdle()
		state = State.IDLE

		val seconds = 3
		countdownTimer = Observable.interval(0, 1000L, TimeUnit.MILLISECONDS)
				.take(seconds.toLong() + 1)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnNext({ time ->
					getView()?.setStatusCountdown((seconds - time).toInt())
					state = State.COUNTDOWN
				})
				.doOnComplete({
					startCapture()
				})
				.subscribe()
	}

	private fun startCapture() {
		stopPlayback()
		getView()?.setStatusRecording()
		state = State.RECORDING

		imgSequenceDisposable?.dispose()
		imgSequenceDisposable = imageProcessor.getImageSequenceObservable().subscribe(
				{ sequence: ImageSequence? -> sequence?.let { onSequenceCaptured(it) } },
				{ throwable: Throwable? -> t(TAG) { throwable } }
		)

		getView()?.triggerBurstCapture()
	}

	private fun onSequenceCaptured(sequence: ImageSequence) {
		imageProcessor.createGif(sequence).subscribe(
				{ file ->
					Timber.tag(TAG).d("Created new GIF file ${file.path}")
				},
				{ error ->
					Timber.tag(TAG).e(error)
				}
		)

		gifomatStore.addSequence(sequence)
		startPlayback()
	}

	private fun startPlayback() {
		if (gifomatStore.getSequences().isEmpty()) {
			setIdle()
		}

		imageLooper?.dispose()

		getView()?.showPlayer()
		getView()?.setStatusPlayback()
		getView()?.showPlaybackInfo()
		state = State.PLAYBACK

		val sequences = gifomatStore.getSequences()
		imageLooper = Observable.fromIterable(sequences)
				.concatMap { imageSequence ->
					Observable.fromIterable(imageSequence.images)
				}
				.concatMap { frame: ImageFrame ->
					Observable.just(frame).delay(PLAYBACK_DELAY, TimeUnit.MILLISECONDS)
				}
				.repeat()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						{ frame ->
							getView()?.playImageFrame(frame)
//							getView()?.setPlaybackFrameInfo("${frame.timestamp}")
//							getView()?.setPlaybackSeqInfo("meh")
						},
						{ throwable ->
							Timber.tag(TAG).e(throwable)
						}
				)
	}

	private fun stopPlayback() {
		imageLooper?.dispose()
		getView()?.hidePlayer()
		getView()?.hidePlaybackInfo()
	}

	private fun setIdle() {
		stopPlayback()
		getView()?.setStatusIdle()
		state = State.IDLE
	}

	// region UI events

	fun onStatusClick() {
		if (state == State.RECORDING || state == State.COUNTDOWN) {
			return
		}

		if (state == State.IDLE && gifomatStore.getSequences().isNotEmpty()) {
			startPlayback()
		} else {
			setIdle()
		}
	}

	// endregion
}