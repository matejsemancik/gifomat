package wtf.matsem.gifomat.ui.main

import android.app.Activity
import android.os.Bundle
import org.koin.android.ext.android.inject
import wtf.matsem.gifomat.R
import wtf.matsem.gifomat.tool.GifomatCamera

class MainActivity : Activity(), MainView {

	val presenter by inject<MainPresenter>()
	val camera2 by inject<GifomatCamera>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		actionBar.hide()
	}
}
