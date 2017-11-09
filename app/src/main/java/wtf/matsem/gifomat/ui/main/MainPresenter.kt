package wtf.matsem.gifomat.ui.main

import wtf.matsem.gifomat.mvp.BasePresenter

class MainPresenter: BasePresenter<MainView>() {

	override fun attachView(view: MainView) {
		super.attachView(view)

		getView()?.initCamera()
		getView()?.startCamPreview()
	}
}