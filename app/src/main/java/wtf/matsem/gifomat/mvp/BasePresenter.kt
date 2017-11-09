package wtf.matsem.gifomat.mvp

abstract class BasePresenter<T : BaseView>() {

	private var view: T? = null

	open fun attachView(view: T) {
		this.view = view
	}

	open fun detachView() {
		this.view = null
	}

	protected fun isViewAttached(): Boolean = view != null

	fun getView(): T? = view
}