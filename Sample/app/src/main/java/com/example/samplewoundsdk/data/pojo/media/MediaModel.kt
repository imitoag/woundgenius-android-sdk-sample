package com.example.samplewoundsdk.data.pojo.media

import androidx.room.TypeConverter
import com.example.woundsdk.data.pojo.bodypart.WoundGeniusBodyPart
import com.example.woundsdk.data.pojo.camera.cameramod.CameraMods
import com.example.woundsdk.data.pojo.measurement.ImageResolution
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MediaModel(

    @SerializedName("metadata")
    val metadata: Metadata? = null,

    @SerializedName("imagePath")
    var imagePath: String? = null,

    @SerializedName("originalPictureSize")
    val originalPictureSize: ImageResolution? = null,

    @SerializedName("measurementMethod")
    var measurementMethod: CameraMods? = null,

    @SerializedName("bodyPart")
    var bodyPart: List<WoundGeniusBodyPart>? = null

    ) : Serializable {
    data class Metadata(

        @SerializedName("measurementData")
        val measurementData: MeasurementData? = null,

        ) : Serializable {

        data class MeasurementData(

            @SerializedName("annotationList")
            val annotationList: List<Annotation?>? = null,

            @SerializedName("calibration")
            val calibration: Calibration? = null

        ) : Serializable {


            class Converter {

                @TypeConverter
                fun fromMetadata(value: MeasurementData?): String {
                    return Gson().toJson(value)
                }

                @TypeConverter
                fun toMetadata(value: String): MeasurementData? {
                    return Gson().fromJson<MeasurementData>(value, MeasurementData::class.java)
                }
            }

            data class Annotation(

                @field:SerializedName("area")
                val area: Double? = null,

                @field:SerializedName("circumference")
                val circumference: Double? = null,

                @field:SerializedName("type")
                val type: String? = null,

                @field:SerializedName("order")
                val order: Int? = null,

                @field:SerializedName("id")
                val id: Int? = null,

                @field:SerializedName("points")
                var points: List<PointsItem>? = null,

                @field:SerializedName("widthPointA")
                val widthPointA: PointDouble? = null,

                @field:SerializedName("widthPointB")
                val widthPointB: PointDouble? = null,

                @field:SerializedName("lengthPointA")
                val lengthPointA: PointDouble? = null,

                @field:SerializedName("lengthPointB")
                val lengthPointB: PointDouble? = null,

                @field:SerializedName("prefix")
                val prefix: String? = null,

                @field:SerializedName("length")
                val length: Double? = null,

                @field:SerializedName("width")
                val width: Double? = null,

                @field:SerializedName("pointA")
                val pointA: PointA? = null,

                @field:SerializedName("pointB")
                val pointB: PointDouble? = null,

                @field:SerializedName("depth")
                val depth: Double? = null

            ) : Serializable {

                data class PointA(

                    @SerializedName("pointX")
                    val pointX: Int? = null,

                    @SerializedName("pointY")
                    val pointY: Int? = null

                ) : Serializable

                data class PointDouble(

                    @SerializedName("pointX")
                    val pointX: Double? = null,

                    @SerializedName("pointY")
                    val pointY: Double? = null

                ) : Serializable

                data class PointsItem(

                    @field:SerializedName("pointX")
                    val pointX: Int? = null,

                    @field:SerializedName("pointY")
                    val pointY: Int? = null

                ) : Serializable

                class Converter {

                    @TypeConverter
                    fun fromMetadata(value: Annotation?): String {
                        return Gson().toJson(value)
                    }

                    @TypeConverter
                    fun toMetadata(value: String): Annotation? {
                        return Gson().fromJson<Annotation>(value, Annotation::class.java)
                    }
                }

                companion object {
                    const val ANNOTATION_DEPTH_TYPE = "depth"
                    const val ANNOTATION_LINE_TYPE = "line"
                    const val ANNOTATION_AREA_TYPE = "area"
                    const val ANNOTATION_WIDTH_PREFIX = "width"
                    const val ANNOTATION_LENGTH_PREFIX = "length"
                    const val CALIBRATION_UNIT_CM = "CM"
                    const val ANNOTATION_OUTLINE_TYPE = "outline"
                }

            }

            data class Calibration(

                @SerializedName("unit")
                val unit: String? = null,

                @SerializedName("unitPerPixel")
                val unitPerPixel: Double? = null

            ) : Serializable {
                class Converter {

                    @TypeConverter
                    fun fromMetadata(value: Calibration?): String {
                        return Gson().toJson(value)
                    }

                    @TypeConverter
                    fun toMetadata(value: String): Calibration? {
                        return Gson().fromJson<Calibration>(value, Calibration::class.java)
                    }
                }
            }

        }

        class Converter {

            @TypeConverter
            fun fromMetadata(value: Metadata?): String {
                return Gson().toJson(value)
            }

            @TypeConverter
            fun toMetadata(value: String): Metadata? {
                return Gson().fromJson<Metadata>(value, Metadata::class.java)
            }
        }
    }

    class Converter {

        @TypeConverter
        fun fromMetadata(value: MediaModel?): String? {
            return Gson().toJson(value)
        }

        @TypeConverter
        fun toMetadata(value: String?): MediaModel? {
            return try {
                Gson().fromJson(value, MediaModel::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

}
