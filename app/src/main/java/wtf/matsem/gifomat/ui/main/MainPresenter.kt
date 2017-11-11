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
import wtf.matsem.gifomat.hardware.PeripheralManager
import wtf.matsem.gifomat.mvp.BasePresenter
import wtf.matsem.gifomat.t
import wtf.matsem.gifomat.tool.camera.ImageProcessor
import java.util.concurrent.TimeUnit

class MainPresenter(private val peripheralManager: PeripheralManager,
					private val imageProcessor: ImageProcessor) : BasePresenter<MainView>() {

	companion object {
		const val TAG = "MainPresenter"
		const val BUTTON_PIN = "GPIO_37"
	}

	val disposables = CompositeDisposable()
	var imgSequenceDisposable: Disposable? = null

	override fun attachView(view: MainView) {
		super.attachView(view)

		getView()?.initCamera()
		getView()?.startCamPreview()
		initButton()
	}

	override fun detachView() {
		disposables.dispose()
		imgSequenceDisposable?.dispose()
		peripheralManager.close()
		super.detachView()
	}

	private fun initButton() {
		val btnDisposable = peripheralManager.openInput(BUTTON_PIN)
				.filter({ gpio: Gpio -> gpio.value == true })
				.debounce(500, TimeUnit.MILLISECONDS)
				.subscribe({ gpio: Gpio? ->
					captureBurst()
				}, { t: Throwable? ->
					Timber.tag(TAG).e(t)
				})

		disposables.add(btnDisposable)
	}

	private fun captureBurst() {
		imgSequenceDisposable?.dispose()
		imgSequenceDisposable = imageProcessor.getImageSequenceObservable().subscribe(
				{ sequence: ImageSequence? -> sequence?.let { playSequence(it) } },
				{ throwable: Throwable? -> t(TAG) { throwable } }
		)

		getView()?.triggerBurstCapture()
	}

	private fun playSequence(sequence: ImageSequence) {
		getView()?.showPlayer()

		Observable.fromIterable(sequence.images)
				.concatMap { frame: ImageFrame -> Observable.just(frame).delay(100, TimeUnit.MILLISECONDS) }
				.repeat(3)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						{ frame -> getView()?.playImageFrame(frame) },
						{ throwable -> Timber.tag(TAG).e(throwable) },
						{ getView()?.hidePlayer() })
	}
}