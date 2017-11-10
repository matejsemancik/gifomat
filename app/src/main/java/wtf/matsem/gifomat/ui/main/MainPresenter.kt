package wtf.matsem.gifomat.ui.main

import com.google.android.things.pio.Gpio
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import wtf.matsem.gifomat.hardware.PeripheralManager
import wtf.matsem.gifomat.mvp.BasePresenter
import java.util.concurrent.TimeUnit

class MainPresenter(private val peripheralManager: PeripheralManager) : BasePresenter<MainView>() {

	companion object {
		const val TAG = "MainPresenter"
		const val BUTTON_PIN = "GPIO_37"
	}

	val disposables = CompositeDisposable()

	override fun attachView(view: MainView) {
		super.attachView(view)

		getView()?.initCamera()
		getView()?.startCamPreview()
		initButton()
	}

	override fun detachView() {
		disposables.dispose()
		peripheralManager.close()
		super.detachView()
	}

	private fun initButton() {
		val btnDisposable = peripheralManager.openInput(BUTTON_PIN)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.debounce(500, TimeUnit.MILLISECONDS)
				.subscribe({ gpio: Gpio? ->
					getView()?.triggerBurstCapture()
				}, { t: Throwable? ->
					Timber.tag(TAG).e(t)
				})

		disposables.add(btnDisposable)
	}
}