package wtf.matsem.gifomat.tool.rx

import io.reactivex.FlowableSubscriber
import org.reactivestreams.Subscription

abstract class BaseFlowableSubscriber<T>: FlowableSubscriber<T> {
	override fun onError(t: Throwable?) {

	}

	override fun onComplete() {

	}

	abstract override fun onNext(t: T);

	override fun onSubscribe(s: Subscription) {

	}
}