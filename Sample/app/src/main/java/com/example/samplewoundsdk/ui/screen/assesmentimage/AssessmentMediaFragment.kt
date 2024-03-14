package com.example.samplewoundsdk.ui.screen.assesmentimage

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.measurement.ImageResolution
import com.example.samplewoundsdk.data.pojo.measurement.MeasurementMetadata
import com.example.samplewoundsdk.data.pojo.media.MediaModel
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_AREA_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_LENGTH_PREFIX
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_LINE_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_OUTLINE_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_WIDTH_PREFIX
import com.example.samplewoundsdk.databinding.SampleAppFragmentAssessmentImageBinding
import com.example.samplewoundsdk.ui.screen.base.AbsFragment
import com.example.samplewoundsdk.ui.screen.measurementfullscreen.MeasurementFullScreenActivity
import com.example.samplewoundsdk.utils.image.drawstroke.StrokeScalableImageView
import com.example.samplewoundsdk.data.pojo.measurement.Vertices
import com.example.woundsdk.data.pojo.cameramod.CameraMods
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.Serializable
import kotlin.collections.ArrayList
import kotlin.math.max

class AssessmentMediaFragment : AbsFragment<AssessmentMediaViewModel>() {

    private val args by lazy { arguments?.getSerializable(ARGS_KEY) as? Args }
    private val getImageHandler = Handler()
    private var isStartingRequest = false
    private var currentPictureSize: ImageResolution? = null
    private var player: SimpleExoPlayer? = null
    private lateinit var videoSource: MediaSource

    override fun provideViewModelClass() = AssessmentMediaViewModel::class
    override fun provideLayoutId() = R.layout.sample_app_fragment_assessment_image

    private lateinit var binding: SampleAppFragmentAssessmentImageBinding

    override fun initListeners() {
        binding.imageSSIV.setOnClickListener {
            context?.let { context ->
                args?.apply {
                    openFull(mediaModel = media)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding = SampleAppFragmentAssessmentImageBinding.bind(view)
        return binding.root
    }

    private fun openFull(mediaModel: MediaModel) {
        args?.apply {
            val metadataList = ArrayList<MeasurementMetadata>()
            if (mediaModel.metadata?.measurementData?.annotationList?.find { it?.type == ANNOTATION_AREA_TYPE } != null) {
                val areaAnnotationItem =
                    mediaModel.metadata?.measurementData?.annotationList?.find { it?.type == ANNOTATION_AREA_TYPE }
                val pointsList = areaAnnotationItem?.points
                val lines =
                    mediaModel.metadata?.measurementData?.annotationList?.filter { it?.type == ANNOTATION_LINE_TYPE }
                val widthLine = lines?.find { it?.prefix == ANNOTATION_WIDTH_PREFIX }
                val lengthLine = lines?.find { it?.prefix == ANNOTATION_LENGTH_PREFIX }

                val widthA =
                    pointsList?.indexOfFirst { it.pointX == widthLine?.pointA?.pointX && it.pointY == widthLine?.pointA?.pointY }
                val widthB =
                    pointsList?.indexOfFirst { it.pointX == widthLine?.pointB?.pointX?.toInt() && it.pointY == widthLine?.pointB?.pointY?.toInt() }
                val lengthA =
                    pointsList?.indexOfFirst { it.pointX == lengthLine?.pointA?.pointX && it.pointY == lengthLine?.pointA?.pointY }
                val lengthB =
                    pointsList?.indexOfFirst { it.pointX == lengthLine?.pointB?.pointX?.toInt() && it.pointY == lengthLine?.pointB?.pointY?.toInt() }

                metadataList.add(
                    MeasurementMetadata(
                        area = areaAnnotationItem?.area ?: 0.0,
                        circumference = areaAnnotationItem?.circumference ?: 0.0,
                        length = lengthLine?.length ?: 0.0,
                        width = widthLine?.width ?: 0.0,
                        depth = areaAnnotationItem?.depth ?: 0.0,
                        vertices = pointsList?.map {
                            MeasurementMetadata.Point(
                                (it.pointX) ?: 0,
                                (it.pointY) ?: 0
                            )
                        } ?: emptyList(),
                        lengthLine = MeasurementMetadata.Line(lengthA ?: -1, lengthB ?: -1),
                        widthLine = MeasurementMetadata.Line(widthA ?: -1, widthB ?: -1),
                        countPxInCm = (1.0 / (mediaModel.metadata!!.measurementData?.calibration?.unitPerPixel
                            ?: 1.0)).toInt()
                    )
                )
            }
            if (mediaModel.metadata?.measurementData?.annotationList?.find { it?.type == ANNOTATION_OUTLINE_TYPE } != null) {
                mediaModel.metadata?.measurementData?.annotationList?.filter { it?.type == ANNOTATION_OUTLINE_TYPE }
                    ?.forEach { annotationItem ->
                        val pointsList = annotationItem?.points
                        val widthLine =
                            Pair(annotationItem?.widthPointA, annotationItem?.widthPointB)
                        val lengthLine =
                            Pair(annotationItem?.lengthPointA, annotationItem?.lengthPointB)

                        val widthA =
                            pointsList?.indexOfFirst { it.pointX == widthLine.first?.pointX?.toInt() && it.pointY == widthLine.first?.pointY?.toInt() }
                        val widthB =
                            pointsList?.indexOfFirst { it.pointX == widthLine.second?.pointX?.toInt() && it.pointY == widthLine.second?.pointY?.toInt() }
                        val lengthA =
                            pointsList?.indexOfFirst { it.pointX == lengthLine.first?.pointX?.toInt() && it.pointY == lengthLine.first?.pointY?.toInt() }
                        val lengthB =
                            pointsList?.indexOfFirst { it.pointX == lengthLine.second?.pointX?.toInt() && it.pointY == lengthLine.second?.pointY?.toInt() }

                        metadataList.add(
                            MeasurementMetadata(
                                area = annotationItem?.area ?: 0.0,
                                circumference = annotationItem?.circumference ?: 0.0,
                                length = annotationItem?.length ?: 0.0,
                                width = annotationItem?.width ?: 0.0,
                                depth = annotationItem?.depth ?: 0.0,
                                vertices = pointsList?.map {
                                    MeasurementMetadata.Point(
                                        (it.pointX) ?: 0,
                                        (it.pointY) ?: 0
                                    )
                                } ?: emptyList(),
                                lengthLine = MeasurementMetadata.Line(lengthA ?: -1, lengthB ?: -1),
                                widthLine = MeasurementMetadata.Line(widthA ?: -1, widthB ?: -1),
                                countPxInCm = (1.0 / (mediaModel.metadata?.measurementData?.calibration?.unitPerPixel
                                    ?: 1.0)).toInt()
                            )
                        )
                    }
            }

            context?.let {
                mediaModel.originalPictureSize?.let { it1 ->
                    MeasurementFullScreenActivity.open(
                        it,
                        mediaModel.imagePath ?: "",
                        it1,
                        metadataList
                    )
                }
            }
        }
    }

    private fun getScale(
        currentWidth: Int,
        currentHeight: Int,
        originalWidth: Int,
        originalHeight: Int
    ): Double {
        val currentMaxWidth = max(currentWidth, currentHeight)
        val originalMaxWidth = max(originalWidth, originalHeight)
        return originalMaxWidth / currentMaxWidth.toDouble()
    }

    private fun setUpMetadataUi() {
        var scale = 0.0
        args?.apply {
            currentPictureSize?.let { currentSize ->
                scale = getScale(
                    currentWidth = currentSize.width,
                    currentHeight = currentSize.height,
                    originalWidth = args?.media?.originalPictureSize?.width ?: 0,
                    originalHeight = args?.media?.originalPictureSize?.height ?: 0
                )
            }
            val metadataList = ArrayList<MeasurementMetadata>()
            val metadata = media.metadata
            if (metadata?.measurementData?.annotationList?.find { it?.type == MediaModel.Metadata.MeasurementData.Annotation.ANNOTATION_AREA_TYPE } != null) {
                val areaAnnotationItem =
                    metadata.measurementData.annotationList.find { it?.type == MediaModel.Metadata.MeasurementData.Annotation.ANNOTATION_AREA_TYPE }
                val pointsList = areaAnnotationItem?.points
                val lines =
                    metadata.measurementData.annotationList.filter { it?.type == ANNOTATION_LINE_TYPE }
                val widthLine = lines.find { it?.prefix == ANNOTATION_WIDTH_PREFIX }
                val lengthLine = lines.find { it?.prefix == ANNOTATION_LENGTH_PREFIX }

                val widthA =
                    pointsList?.indexOfFirst { it.pointX == widthLine?.pointA?.pointX && it.pointY == widthLine?.pointA?.pointY }
                val widthB =
                    pointsList?.indexOfFirst { it.pointX == widthLine?.pointB?.pointX?.toInt() && it.pointY == widthLine?.pointB?.pointY?.toInt() }
                val lengthA =
                    pointsList?.indexOfFirst { it.pointX == lengthLine?.pointA?.pointX && it.pointY == lengthLine?.pointA?.pointY }
                val lengthB =
                    pointsList?.indexOfFirst { it.pointX == lengthLine?.pointB?.pointX?.toInt() && it.pointY == lengthLine?.pointB?.pointY?.toInt() }

                metadataList.add(
                    MeasurementMetadata(
                        area = areaAnnotationItem?.area ?: 0.0,
                        circumference = areaAnnotationItem?.circumference ?: 0.0,
                        length = lengthLine?.length ?: 0.0,
                        width = widthLine?.width ?: 0.0,
                        depth = areaAnnotationItem?.depth ?: 0.0,
                        vertices = pointsList?.map {
                            MeasurementMetadata.Point(
                                it.pointX ?: 0,
                                it.pointY ?: 0
                            )
                        } ?: emptyList(),
                        lengthLine = MeasurementMetadata.Line(
                            lengthA ?: -1,
                            lengthB ?: -1
                        ),
                        widthLine = MeasurementMetadata.Line(
                            widthA ?: -1,
                            widthB ?: -1
                        ),
                        countPxInCm = (1.0 / (metadata.measurementData?.calibration?.unitPerPixel
                            ?: 1.0)).toInt()
                    )
                )
            }
            metadata?.measurementData?.annotationList?.filter { it?.type == ANNOTATION_OUTLINE_TYPE }
                ?.forEach { annotationItem ->
                    val pointsList = annotationItem?.points
                    val widthLine = Pair(annotationItem?.widthPointA, annotationItem?.widthPointB)
                    val lengthLine =
                        Pair(annotationItem?.lengthPointA, annotationItem?.lengthPointB)

                    val widthA =
                        pointsList?.indexOfFirst { it.pointX == widthLine.first?.pointX?.toInt() && it.pointY == widthLine.first?.pointY?.toInt() }
                    val widthB =
                        pointsList?.indexOfFirst { it.pointX == widthLine.second?.pointX?.toInt() && it.pointY == widthLine.second?.pointY?.toInt() }
                    val lengthA =
                        pointsList?.indexOfFirst { it.pointX == lengthLine.first?.pointX?.toInt() && it.pointY == lengthLine.first?.pointY?.toInt() }
                    val lengthB =
                        pointsList?.indexOfFirst { it.pointX == lengthLine.second?.pointX?.toInt() && it.pointY == lengthLine.second?.pointY?.toInt() }

                    metadataList.add(
                        MeasurementMetadata(
                            area = annotationItem?.area ?: 0.0,
                            circumference = annotationItem?.circumference ?: 0.0,
                            length = annotationItem?.length ?: 0.0,
                            width = annotationItem?.width ?: 0.0,
                            depth = annotationItem?.depth ?: 0.0,
                            vertices = pointsList?.map {
                                MeasurementMetadata.Point(
                                    it.pointX ?: 0,
                                    it.pointY ?: 0
                                )
                            } ?: emptyList(),
                            lengthLine = MeasurementMetadata.Line(
                                lengthA ?: -1,
                                lengthB ?: -1
                            ),
                            widthLine = MeasurementMetadata.Line(
                                widthA ?: -1,
                                widthB ?: -1
                            ),
                            countPxInCm = (1.0 / (metadata.measurementData?.calibration?.unitPerPixel
                                ?: 1.0)).toInt()
                        )
                    )
                }

            val allVertexesList = ArrayList<ArrayList<Vertices>>()
            val widthIndexes = ArrayList<Pair<Int?, Int?>>()
            val lengthIndexes = ArrayList<Pair<Int?, Int?>>()
            val areaList = ArrayList<Double>()
            metadataList.forEachIndexed { index, boundaryMetadata ->
                boundaryMetadata.apply {
                    boundaryMetadata.vertices?.let {
                        allVertexesList.add(ArrayList(it.map {
                            Vertices(
                                Point(
                                    (it.x / scale).toInt(),
                                    (it.y / scale).toInt()
                                )
                            )
                        }))
                    }
                    widthIndexes.add(
                        Pair(
                            boundaryMetadata.widthLine?.pointAIndex,
                            boundaryMetadata.widthLine?.pointBIndex
                        )
                    )
                    lengthIndexes.add(
                        Pair(
                            boundaryMetadata.lengthLine?.pointAIndex,
                            boundaryMetadata.lengthLine?.pointBIndex
                        )
                    )
                    boundaryMetadata.area?.let { areaList.add(it) }
                }
            }
            binding.apply {
                imageSSIV.setVertices(allVertexesList)
                imageSSIV.setDiameter(metadataList.firstOrNull()?.length ?: 0.0)
                imageSSIV.setWidthAndLength(
                    widthIndexes,
                    lengthIndexes,
                    areaList
                )
            }
        }
    }

    private fun initStrokeScalableImageView() {
        args?.apply {
            binding.apply {
                imageSSIV.setMode(
                    StrokeScalableImageView.Mode.ViewMeasurement
                )
                imageSSIV.setTouchListener(object : StrokeScalableImageView.ViewTouchListener {
                    override fun onDown(sourceCoords: PointF?) {}
                    override fun onZoomChanged(zoom: Float) {
                    }

                    override fun onUp() {}
                    override fun onVertexListChanged(
                        vertices: ArrayList<ArrayList<Vertices>>?,
                        closed: Boolean
                    ) {
                    }

                    override fun onMove(viewCoord: PointF?) {}
                })
                imageSSIV.maxScale = 5f
                imageSSIV.isNeedFillPolygon(false)
                imageSSIV.isNeedWhiteStrokesOnVertex(true)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMediaFileUi()
    }

    private fun setupMediaFileUi() {
        val file = args?.media?.imagePath
        if (args?.media?.measurementMethod == CameraMods.VIDEO_MODE) {
            initPlayer(file)
        } else {
            initStrokeScalableImageView()
            val bitmap = BitmapFactory.decodeFile(file)
            viewModel?.apply {
                args?.apply {
                    binding.apply {
                        activity?.let { ctx ->
                            Glide.with(ctx)
                                .asBitmap()
                                .load(bitmap)
                                .listener(object : RequestListener<Bitmap> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Bitmap>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        return true
                                    }

                                    override fun onResourceReady(
                                        resource: Bitmap?,
                                        model: Any?,
                                        target: Target<Bitmap>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        resource?.let { bitmap ->
                                            currentPictureSize =
                                                ImageResolution(bitmap.width, bitmap.height)
                                            imageSSIV.setImage(ImageSource.bitmap(bitmap))
                                            setUpMetadataUi()
                                        }
                                        return true
                                    }
                                }).into(hidePhotoACIV)
                        }

                        media.metadata?.let {
                            Glide.with(this@AssessmentMediaFragment)
                                .load(media.imagePath)
                                .into(measurementImageACIV)
                        }
                    }
                }
            }
        }
        binding.apply {
            hidePhotoACIV.isVisible = args?.media?.measurementMethod != CameraMods.VIDEO_MODE
            imageSSIV.isVisible = args?.media?.measurementMethod != CameraMods.VIDEO_MODE
            measurementImageCL.isVisible = args?.media?.measurementMethod != CameraMods.VIDEO_MODE
            videoView.isVisible = args?.media?.measurementMethod == CameraMods.VIDEO_MODE
        }
    }

    private fun initPlayer(url: String?) {
        context?.let {
            player = SimpleExoPlayer.Builder(it).build()
            binding.videoView.player = player
            val dataSourceFactory: com.google.android.exoplayer2.upstream.DataSource.Factory =
                DefaultDataSourceFactory(
                    it, Util.getUserAgent(it, resources.getString(R.string.APP_NAME))
                )
            videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(url)))

            player?.playWhenReady = false
            player?.prepare(videoSource)
        }
    }

    override fun onResume() {
        super.onResume()
        getImageHandler.postDelayed({
            if (!isStartingRequest) {
                isStartingRequest = true
            }
        }, GET_IMAGE_DELAY)
    }

    override fun onPause() {
        super.onPause()
        getImageHandler.removeCallbacksAndMessages(null)
    }

    override fun onStop() {
        player?.release()
        super.onStop()
    }

    override fun onDestroy() {
        player?.stop()
        super.onDestroy()
    }

    companion object {
        private const val ARGS_KEY = "args_key"

        private data class Args(
            val media: MediaModel
        ) : Serializable

        private const val GET_IMAGE_DELAY = 500L

        fun newInstance(draftMedia: MediaModel) =
            AssessmentMediaFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARGS_KEY, Args(draftMedia))
                }
            }
    }

}
