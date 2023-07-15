import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true) data class KspSample(val something: String)
