package com.example.samplewoundsdk.ui.screen.assesmentimage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.media.MediaModel
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_AREA_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_LENGTH_PREFIX
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_LINE_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_OUTLINE_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_WIDTH_PREFIX
import com.example.samplewoundsdk.databinding.SampleAppFragmentAssessmentImageBinding
import com.example.samplewoundsdk.ui.screen.base.AbsFragment
import com.example.woundsdk.data.pojo.camera.cameramod.CameraMods
import com.example.woundsdk.data.pojo.measurement.ImageResolution
import com.example.woundsdk.data.pojo.measurement.MeasurementMetadata
import com.example.woundsdk.data.pojo.measurement.Outline
import com.example.woundsdk.data.pojo.measurement.Vertices
import com.example.woundsdk.ui.screen.measurementfullscreen.MeasurementFullScreenActivity
import com.example.woundsdk.utils.image.drawstroke.StrokeScalableImageView
import java.io.File
import java.io.Serializable
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.math.max

class AssessmentMediaFragment : AbsFragment<AssessmentMediaViewModel>() {

    private val args by lazy { arguments?.getSerializable(ARGS_KEY) as? Args }
    private val getImageHandler = Handler()
    private var isStartingRequest = false
    private var currentPictureSize: ImageResolution? = null
    private var player: ExoPlayer? = null
    private var playbackPosition: Long = 0
    private var isPlaying: Boolean = true

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

    private var isFullScreenClicked = false;


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
        if (!isFullScreenClicked) {
            args?.apply {
                isFullScreenClicked = true
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
                            order = areaAnnotationItem?.order ?: (metadataList.lastIndex + 1),
                            id = areaAnnotationItem?.id ?: (metadataList.lastIndex + 1),
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
                                    lengthLine = MeasurementMetadata.Line(
                                        lengthA ?: -1,
                                        lengthB ?: -1
                                    ),
                                    widthLine = MeasurementMetadata.Line(
                                        widthA ?: -1,
                                        widthB ?: -1
                                    ),
                                    countPxInCm = (1.0 / (mediaModel.metadata?.measurementData?.calibration?.unitPerPixel
                                        ?: 1.0)).toInt(),
                                    order = annotationItem?.order ?: (metadataList.lastIndex + 1),
                                    id = annotationItem?.id ?: (metadataList.lastIndex + 1),
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
                            metadataList,
                            args?.isStoma ?: false
                        )
                    }
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
                            ?: 1.0)).toInt(),
                        order = areaAnnotationItem?.order ?: (metadataList.lastIndex + 1),
                        id = areaAnnotationItem?.id ?: (metadataList.lastIndex + 1),
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
                                ?: 1.0)).toInt(),
                            order = annotationItem?.order ?: (metadataList.lastIndex + 1),
                            id = annotationItem?.id ?: (metadataList.lastIndex + 1),
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
                imageSSIV.setVertices(ArrayList(allVertexesList.mapIndexed { index, vertices ->
                    Outline(
                        id = index,
                        vertices = vertices
                    )
                }))
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
                if (isStoma) {
                    imageSSIV.setMode(
                        StrokeScalableImageView.Mode.ViewStoma
                    )
                } else {
                    imageSSIV.setMode(
                        StrokeScalableImageView.Mode.ViewMeasurement
                    )
                }
                imageSSIV.setTouchListener(object : StrokeScalableImageView.ViewTouchListener {
                    override fun onDown(sourceCoords: PointF?) {}
                    override fun onZoomChanged(zoom: Float) {
                    }

                    override fun onTouch() {

                    }

                    override fun onUp() {}
                    override fun onVertexListChanged(
                        vertices: ArrayList<Outline>?,
                        closed: Boolean
                    ) {

                    }


                    override fun onUpdateAutoDetection(removedVertices: ArrayList<Int>) {

                    }

                    override fun onMove(viewCoord: PointF?) {}
                })
                imageSSIV.maxScale = 5f
                imageSSIV.isNeedFillPolygon(false)
                imageSSIV.isNeedWhiteStrokesOnVertex(true)

                if (context?.resources?.configuration?.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                    imageSSIV.post {
                        val layoutParams =
                            imageSSIV?.layoutParams as ConstraintLayout.LayoutParams
                        layoutParams.width =
                            (((imageSSIV?.measuredHeight ?: 0) * 4f) / 3f).toInt()
                        imageSSIV?.layoutParams = layoutParams
                    }
                } else {

                    imageSSIV.post {
                        val layoutParams =
                            imageSSIV?.layoutParams as ConstraintLayout.LayoutParams
                        layoutParams.height =
                            (((imageSSIV?.measuredWidth ?: 0) * 4f) / 3f).toInt()
                        imageSSIV?.layoutParams = layoutParams
                    }
                }
            }
        }
    }

    class RotateTransformation(private val rotationAngle: Float) : BitmapTransformation() {

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(("rotate$rotationAngle").toByteArray(Charset.forName("UTF-8")))
        }

        override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(rotationAngle)
            return Bitmap.createBitmap(toTransform, 0, 0, toTransform.width, toTransform.height, matrix, true)
        }

        override fun equals(other: Any?): Boolean {
            return other is RotateTransformation && other.rotationAngle == rotationAngle
        }

        override fun hashCode(): Int {
            return rotationAngle.hashCode()
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
            viewModel?.apply {
                args?.apply {
                    binding.apply {
                        activity?.let { ctx ->
                            Glide.with(ctx)
                                .asBitmap()
                                .load(file)
                                .listener(object : RequestListener<Bitmap> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Bitmap>,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        return true
                                    }

                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        model: Any,
                                        target: Target<Bitmap>?,
                                        dataSource: DataSource,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        resource.let { bitmap ->
                                            currentPictureSize =
                                                ImageResolution(bitmap.width, bitmap.height)
                                            imageSSIV.setImage(ImageSource.bitmap(bitmap))
                                            setUpMetadataUi()
                                        }
                                        return true
                                    }
                                }).into(hidePhotoACIV)
                        }

                    }
                }
            }
        }
        binding.apply {
            hidePhotoACIV.isVisible = args?.media?.measurementMethod != CameraMods.VIDEO_MODE
            imageSSIV.isVisible = args?.media?.measurementMethod != CameraMods.VIDEO_MODE
            videoPlayerContainerCl.isVisible = args?.media?.measurementMethod == CameraMods.VIDEO_MODE
        }
    }

    @OptIn(UnstableApi::class)
    private fun initPlayer(url: String?) {
        context?.let { context ->
            player = ExoPlayer.Builder(context).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    setRenderersFactory(DefaultRenderersFactory(context))
                    setUseLazyPreparation(true)
                }
            } .build().also { exoPlayer ->
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true

                exoPlayer.addListener(object : Player.Listener {

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        initPlayer(url)
                    }
                })
            }

            val playerView = PlayerView(context)
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            playerView.layoutParams = layoutParams
            binding.videoPlayerContainerCl.removeAllViews()
            binding.videoPlayerContainerCl.addView(playerView)

            playerView.player = player
        }
    }

    override fun onResume() {
        super.onResume()
        isFullScreenClicked = false
        getImageHandler.postDelayed({
            if (!isStartingRequest) {
                isStartingRequest = true
            }
        }, GET_IMAGE_DELAY)
    }

    override fun onPause() {
        super.onPause()
        // Pause the ExoPlayer when the fragment is paused
        getImageHandler.removeCallbacksAndMessages(null)
    }

    override fun onStop() {
        super.onStop()
        // Release the ExoPlayer when the fragment is stopped
        player?.let {
            playbackPosition = it.currentPosition
            isPlaying = it.isPlaying
            it.pause()
        }
    }

    override fun onDestroyView() {
        // Ensure proper release when view is destroyed
        player?.release()
        player = null
        super.onDestroyView()
    }

    companion object {
        private const val ARGS_KEY = "args_key"

        private data class Args(
            val media: MediaModel,
            val isStoma: Boolean
        ) : Serializable

        private const val GET_IMAGE_DELAY = 500L

        fun newInstance(draftMedia: MediaModel, isStoma: Boolean) =
            AssessmentMediaFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARGS_KEY, Args(draftMedia, isStoma))
                }
            }
    }

}
