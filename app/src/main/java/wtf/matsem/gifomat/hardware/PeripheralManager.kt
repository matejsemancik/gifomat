package wtf.matsem.gifomat.hardware

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers

class PeripheralManager {

	private val peripheralManagerService = PeripheralManagerService()
	private val gpioList = mutableListOf<Gpio>()

	fun openInput(name: String): Flowable<Gpio> {
		val gpio = peripheralManagerService.openGpio(name)
		val publishProcessor = PublishProcessor.create<Gpio>()

		gpio.setDirection(Gpio.DIRECTION_IN)
		gpio.setEdgeTriggerType(Gpio.EDGE_BOTH)
		gpio.registerGpioCallback(object: GpioCallback() {
			override fun onGpioEdge(gpio: Gpio?): Boolean {
				gpio?.let {
					publishProcessor.onNext(gpio)
					return true
				}

				publishProcessor.onComplete()
				return false
			}

			override fun onGpioError(gpio: Gpio?, error: Int) {
				publishProcessor.onError(GpioError(name, error))
			}
		})

		gpioList.add(gpio)
		return publishProcessor
				.onBackpressureLatest()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	fun close() {
		for (gpio in gpioList) {
			gpio.close()
		}
	}

	class GpioError(gpioName: String, errorCode: Int): Throwable("Gpio error on port $gpioName with code $errorCode")
}
