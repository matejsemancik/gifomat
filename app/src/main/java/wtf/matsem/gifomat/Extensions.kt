package wtf.matsem.gifomat

import android.view.View
import timber.log.Timber

fun d(tag: String = "Gifomat", msg: () -> String) {
	Timber.tag(tag).d(msg.invoke())
}

fun e(tag: String = "Gifomat", msg: () -> String) {
	Timber.tag(tag).e(msg.invoke())
}

fun t(tag: String = "Gifomat", throwable: () -> Throwable?) {
	Timber.tag(tag).e(throwable.invoke())
}

fun View.setVisible() {
	this.visibility = View.VISIBLE
}

fun View.setGone() {
	this.visibility = View.GONE
}

fun View.setInvisible() {
	this.visibility = View.INVISIBLE
}