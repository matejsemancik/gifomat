package wtf.matsem.gifomat.ui.main

import wtf.matsem.gifomat.data.model.ImageFrame
import wtf.matsem.gifomat.mvp.BaseView

interface MainView: BaseView {

	fun initCamera()

	fun startCamPreview()

	fun triggerBurstCapture()

	fun showPlayer()

	fun playImageFrame(frame: ImageFrame)

	fun hidePlayer()

	fun showCountdown()

	fun setCountdownText(text: String)

	fun hideCountdown()
}