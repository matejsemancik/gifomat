package wtf.matsem.gifomat.domain.api

import io.reactivex.Completable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface GifomatService {

	@FormUrlEncoded
	@POST("chat.postMessage")
	fun postSlackMessage(
			@Field("channel") channel: String,
			@Field("text") text: String,
			@Field("as_user") asUser: Boolean
	): Completable

	@Multipart
	@POST("files.upload")
	fun uploadSlackFile(
			@Part file: MultipartBody.Part,
			@Part("channels") channels: RequestBody
	): Completable
}