package wtf.matsem.gifomat

import timber.log.Timber

fun d(tag: String = "Gifomat", msg: () -> String) {
	Timber.tag(tag).d(msg.invoke())
}

fun e(tag: String = "Gifomat", msg: () -> String) {
	Timber.tag(tag).e(msg.invoke())
}

fun t(tag: String = "Gifomat", throwable: () -> Throwable) {
	Timber.tag(tag).e(throwable.invoke())
}