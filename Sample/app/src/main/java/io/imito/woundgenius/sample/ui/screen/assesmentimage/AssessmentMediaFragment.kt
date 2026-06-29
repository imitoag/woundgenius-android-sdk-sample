package io.imito.woundgenius.sample.ui.screen.assesmentimage

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.davemorrissey.labs.subscaleview.ImageSource
import io.imito.woundgenius.sample.R
import io.imito.woundgenius.sample.databinding.SampleAppFragmentAssessmentImageBinding
import io.imito.woundgenius.sample.ui.screen.base.AbsFragment
import io.imito.woundgenius.sdk.internal.data.pojo.mode.StrokeScalableMode
import io.imito.woundgenius.sdk.internal.data.pojo.camera.mode.ImitoCameraMode
import io.imito.woundgenius.sdk.internal.data.pojo.media.MediaModel
import io.imito.woundgenius.sdk.internal.data.pojo.image.ImageResolution
import io.imito.woundgenius.sdk.internal.data.pojo.measurement.MeasurementMetadata
import io.imito.woundgenius.sdk.internal.data.pojo.measurement.OutlineModel
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_AREA_TYPE
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_LENGTH_PREFIX
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_LINE_TYPE
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_MEASUREMENT_LINE_TYPE
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_OUTLINE_TYPE
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_WIDTH_PREFIX
import io.imito.woundgenius.sdk.internal.data.pojo.measurement.Vertices
import io.imito.woundgenius.sdk.internal.ui.screen.measurementfullscreen.MeasurementFullScreenActivity
import io.imito.woundgenius.sdk.internal.utils.media.MediaIUtils.isVideoFile
import kotlinx.parcelize.Parcelize
import kotlin.math.max

class AssessmentMediaFragment : AbsFragment<AssessmentMediaViewModel>() {

    private val args by lazy { arguments?.getParcelable(ARGS_KEY) as? Args }
    private val getImageHandler = Handler()
    private var isStartingRequest = false
    private var currentPictureSize: ImageResolution? = null
    private var player: ExoPlayer? = null

    private var playerView: PlayerView? = null
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

    private fun openFull(mediaModel: MediaModel) { // NOSONAR Cognitive Complexity — UI/view code, refactor requires on-device verification
        if (!isFullScreenClicked) {
            args?.apply {
                isFullScreenClicked = true
                val metadataList = ArrayList<MeasurementMetadata>()
                mediaModel.metadata?.measurementData?.annotationList?.sortedBy { it?.id }
                    ?.forEach { annotationItem ->
                        if (annotationItem?.type == ANNOTATION_AREA_TYPE) {
                            val areaAnnotationItem =
                                mediaModel.metadata?.measurementData?.annotationList?.find { it?.type == ANNOTATION_AREA_TYPE }
                            val pointsList = areaAnnotationItem?.points
                            val lines =
                                mediaModel.metadata?.measurementData?.annotationList?.filter { it?.type == ANNOTATION_LINE_TYPE }
                            val widthLine = lines?.find { it?.prefix == ANNOTATION_WIDTH_PREFIX }
                            val lengthLine = lines?.find { it?.prefix == ANNOTATION_LENGTH_PREFIX }

                            val widthA =
                                pointsList?.indexOfFirst { it.x == widthLine?.pointA?.pointX?.toDouble() && it.y == widthLine?.pointA?.pointY?.toDouble() }
                            val widthB =
                                pointsList?.indexOfFirst { it.x == widthLine?.pointB?.pointX?.toDouble() && it.y == widthLine?.pointB?.pointY?.toDouble() }
                            val lengthA =
                                pointsList?.indexOfFirst { it.x == lengthLine?.pointA?.pointX?.toDouble() && it.y == lengthLine?.pointA?.pointY?.toDouble() }
                            val lengthB =
                                pointsList?.indexOfFirst { it.x == lengthLine?.pointB?.pointX?.toDouble() && it.y == lengthLine?.pointB?.pointY?.toDouble() }

                            metadataList.add(
                                MeasurementMetadata(
                                    order = areaAnnotationItem?.order
                                        ?: (metadataList.lastIndex + 1),
                                    id = areaAnnotationItem?.id ?: (metadataList.lastIndex + 1),
                                    area = areaAnnotationItem?.area ?: 0.0,
                                    circumference = areaAnnotationItem?.circumference ?: 0.0,
                                    length = lengthLine?.length ?: 0.0,
                                    width = widthLine?.width ?: 0.0,
                                    depth = (areaAnnotationItem?.depth ?: 0.0f) / 10,
                                    vertices = pointsList?.map {
                                        PointD(
                                            (it.x) ?: 0.0,
                                            (it.y) ?: 0.0
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
                                    countPxInCm = (1.0 / (mediaModel.metadata!!.measurementData?.calibration?.unitPerPixel
                                        ?: 1.0)).toInt()
                                )
                            )
                        } else {
                            if (annotationItem?.type == ANNOTATION_MEASUREMENT_LINE_TYPE) {
                                val pointsList = annotationItem?.points

                                metadataList.add(
                                    MeasurementMetadata(
                                        length = annotationItem?.length ?: 0.0,
                                        vertices = pointsList?.map {
                                            PointD(
                                                (it.x) ?: 0.0,
                                                (it.y) ?: 0.0
                                            )
                                        } ?: emptyList(),
                                        countPxInCm = (1.0 / (mediaModel.metadata?.measurementData?.calibration?.unitPerPixel
                                            ?: 1.0)).toInt(),
                                        order = annotationItem?.order
                                            ?: (metadataList.lastIndex + 1),
                                        id = annotationItem?.id ?: (metadataList.lastIndex + 1),
                                        type = OutlineModel.OutlineType.MEASUREMENT_LINE
                                    )
                                )
                            } else {
                                if (annotationItem?.type == ANNOTATION_OUTLINE_TYPE) {
                                    val pointsList = annotationItem?.points
                                    val widthLine =
                                        Pair(
                                            annotationItem?.widthPointA,
                                            annotationItem?.widthPointB
                                        )
                                    val lengthLine =
                                        Pair(
                                            annotationItem?.lengthPointA,
                                            annotationItem?.lengthPointB
                                        )

                                    val widthA =
                                        pointsList?.indexOfFirst { it.x == widthLine.first?.pointX?.toDouble() && it.y == widthLine.first?.pointY?.toDouble() }
                                    val widthB =
                                        pointsList?.indexOfFirst { it.x == widthLine.second?.pointX?.toDouble() && it.y == widthLine.second?.pointY?.toDouble() }
                                    val lengthA =
                                        pointsList?.indexOfFirst { it.x == lengthLine.first?.pointX?.toDouble() && it.y == lengthLine.first?.pointY?.toDouble() }
                                    val lengthB =
                                        pointsList?.indexOfFirst { it.x == lengthLine.second?.pointX?.toDouble() && it.y == lengthLine.second?.pointY?.toDouble() }

                                    metadataList.add(
                                        MeasurementMetadata(
                                            area = annotationItem?.area ?: 0.0,
                                            circumference = annotationItem?.circumference ?: 0.0,
                                            length = annotationItem?.length ?: 0.0,
                                            width = annotationItem?.width ?: 0.0,
                                            depth = (annotationItem?.depth ?: 0.0f) / 10,
                                            vertices = pointsList?.map {
                                                PointD(
                                                    (it.x) ?: 0.0,
                                                    (it.y) ?: 0.0
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
                                            order = annotationItem?.order
                                                ?: (metadataList.lastIndex + 1),
                                            id = annotationItem?.id ?: (metadataList.lastIndex + 1),
                                            type = if (args?.isStoma == true) {
                                                OutlineModel.OutlineType.STOMA
                                            } else {
                                                OutlineModel.OutlineType.WOUND
                                            }
                                        )
                                    )
                                }
                            }
                        }
                    }

                context?.let {
                    mediaModel.originalPictureSize?.let { it1 ->

                        MeasurementFullScreenActivity.open(
                            it,
                            mediaModel.image ?: "",
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

    private fun setUpMetadataUi() { // NOSONAR Cognitive Complexity — UI/view code, refactor requires on-device verification
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
            metadata?.measurementData?.annotationList?.sortedBy { it?.id }
                ?.forEach { annotationItem ->
                    if (annotationItem?.type == ANNOTATION_AREA_TYPE) {
                        val pointsList = annotationItem?.points
                        val lines =
                            metadata.measurementData?.annotationList?.filter { it?.type == ANNOTATION_LINE_TYPE }
                        val widthLine = lines?.find { it?.prefix == ANNOTATION_WIDTH_PREFIX }
                        val lengthLine = lines?.find { it?.prefix == ANNOTATION_LENGTH_PREFIX }

                        val widthA =
                            pointsList?.indexOfFirst { it.x == widthLine?.pointA?.pointX?.toDouble() && it.y == widthLine?.pointA?.pointY?.toDouble() }
                        val widthB =
                            pointsList?.indexOfFirst { it.x == widthLine?.pointB?.pointX?.toDouble() && it.y == widthLine?.pointB?.pointY?.toDouble() }
                        val lengthA =
                            pointsList?.indexOfFirst { it.x == lengthLine?.pointA?.pointX?.toDouble() && it.y == lengthLine?.pointA?.pointY?.toDouble() }
                        val lengthB =
                            pointsList?.indexOfFirst { it.x == lengthLine?.pointB?.pointX?.toDouble() && it.y == lengthLine?.pointB?.pointY?.toDouble() }

                        metadataList.add(
                            MeasurementMetadata(
                                area = annotationItem?.area ?: 0.0,
                                circumference = annotationItem?.circumference ?: 0.0,
                                length = lengthLine?.length ?: 0.0,
                                width = widthLine?.width ?: 0.0,
                                depth = (annotationItem?.depth ?: 0.0f) / 10,
                                vertices = pointsList?.map {
                                    PointD(it.x ?: 0.0, it.y ?: 0.0)
                                } ?: emptyList(),
                                lengthLine = MeasurementMetadata.Line(lengthA ?: -1, lengthB ?: -1),
                                widthLine = MeasurementMetadata.Line(widthA ?: -1, widthB ?: -1),
                                countPxInCm = (1.0 / (metadata.measurementData?.calibration?.unitPerPixel
                                    ?: 1.0)).toInt(),
                                order = annotationItem?.order ?: (metadataList.lastIndex + 1),
                                id = annotationItem?.id ?: (metadataList.lastIndex + 1),
                                type = if (args?.isStoma == true) {
                                    OutlineModel.OutlineType.STOMA
                                } else {
                                    OutlineModel.OutlineType.WOUND
                                }
                            )
                        )
                    } else if (annotationItem?.type == ANNOTATION_MEASUREMENT_LINE_TYPE) {
                        val pointsList = annotationItem?.points

                        metadataList.add(
                            MeasurementMetadata(
                                length = annotationItem?.length ?: 0.0,
                                vertices = pointsList?.map {
                                    PointD(it.x ?: 0.0, it.y ?: 0.0)
                                } ?: emptyList(),
                                countPxInCm = (1.0 / (metadata.measurementData?.calibration?.unitPerPixel
                                    ?: 1.0)).toInt(),
                                order = annotationItem?.order ?: (metadataList.lastIndex + 1),
                                id = annotationItem?.id ?: (metadataList.lastIndex + 1),
                                type = OutlineModel.OutlineType.MEASUREMENT_LINE
                            )
                        )
                    } else if (annotationItem?.type == ANNOTATION_OUTLINE_TYPE) {
                        val pointsList = annotationItem?.points
                        val widthLine =
                            Pair(annotationItem?.widthPointA, annotationItem?.widthPointB)
                        val lengthLine =
                            Pair(annotationItem?.lengthPointA, annotationItem?.lengthPointB)

                        val widthA =
                            pointsList?.indexOfFirst { it.x == widthLine.first?.pointX && it.y == widthLine.first?.pointY?.toDouble() }
                        val widthB =
                            pointsList?.indexOfFirst { it.x == widthLine.second?.pointX?.toDouble() && it.y == widthLine.second?.pointY?.toDouble() }
                        val lengthA =
                            pointsList?.indexOfFirst { it.x == lengthLine.first?.pointX?.toDouble() && it.y == lengthLine.first?.pointY?.toDouble() }
                        val lengthB =
                            pointsList?.indexOfFirst { it.x == lengthLine.second?.pointX?.toDouble() && it.y == lengthLine.second?.pointY?.toDouble() }

                        metadataList.add(
                            MeasurementMetadata(
                                area = annotationItem?.area ?: 0.0,
                                circumference = annotationItem?.circumference ?: 0.0,
                                length = annotationItem?.length ?: 0.0,
                                width = annotationItem?.width ?: 0.0,
                                depth = (annotationItem?.depth ?: 0.0f) / 10,
                                vertices = pointsList?.map {
                                    PointD(it.x ?: 0.0, it.y ?: 0.0)
                                } ?: emptyList(),
                                lengthLine = MeasurementMetadata.Line(lengthA ?: -1, lengthB ?: -1),
                                widthLine = MeasurementMetadata.Line(widthA ?: -1, widthB ?: -1),
                                countPxInCm = (1.0 / (metadata.measurementData?.calibration?.unitPerPixel
                                    ?: 1.0)).toInt(),
                                order = annotationItem?.order ?: (metadataList.lastIndex + 1),
                                id = annotationItem?.id ?: (metadataList.lastIndex + 1),
                                type = if (args?.isStoma == true) {
                                    OutlineModel.OutlineType.STOMA
                                } else {
                                    OutlineModel.OutlineType.WOUND
                                }
                            )
                        )
                    }
                }

            val allOutlineList = ArrayList<Pair<ArrayList<Vertices>, OutlineModel.OutlineType?>>()
            val widthIndexes = ArrayList<Pair<Int?, Int?>>()
            val lengthIndexes = ArrayList<Pair<Int?, Int?>>()
            val areaList = ArrayList<Double>()
            metadataList.forEachIndexed { index, boundaryMetadata ->
                boundaryMetadata.apply {
                    boundaryMetadata.vertices?.let {
                        allOutlineList.add(ArrayList(it.map {
                            Vertices(
                                PointD(
                                    (it.x / scale).toInt(),
                                    (it.y / scale).toInt()
                                )
                            )
                        }) to boundaryMetadata.type)
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



                imageSSIV.setVertices(ArrayList(allOutlineList.mapIndexed { index, outline ->

                    OutlineModel(
                        id = index,
                        vertices = outline.first,
                        type = outline.second ?: OutlineModel.OutlineType.WOUND
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
                        StrokeScalableMode.ViewStoma
                    )
                } else {
                    imageSSIV.setMode(
                        StrokeScalableMode.ViewMeasurement
                    )
                }
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
        val file = args?.media?.image
        if (args?.media?.measurementMethod != ImitoCameraMode.VIDEO_MODE && !isVideoFile(args?.media?.image?:"")) {
                releasePlayer()
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
                                                hidePhotoACIV.setImageBitmap(bitmap)
                                                imageSSIV.setImage(ImageSource.bitmap(bitmap))
                                                zoomenContainerCL.isVisible = true
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
            hidePhotoACIV.isVisible = true
            imageSSIV.isVisible =  args?.media?.measurementMethod != ImitoCameraMode.VIDEO_MODE && !isVideoFile(args?.media?.image?:"")
            videoPlayerContainerCl.isVisible = args?.media?.measurementMethod == ImitoCameraMode.VIDEO_MODE || isVideoFile(args?.media?.image?:"")
        }
    }

    private fun releasePlayer() {
        player?.let {
            it.stop()
            it.release()
        }
        player = null
        playerView?.player = null
    }

    @OptIn(UnstableApi::class)
    private fun initPlayer(url: String?) {
        context?.let { context ->

            releasePlayer()

            if (playerView == null) {
                playerView = PlayerView(context).apply {
                    layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT
                    )
                }
                binding.videoPlayerContainerCl.removeAllViews()
                binding.videoPlayerContainerCl.addView(playerView)
            }
            val trackSelector = DefaultTrackSelector(context).apply {
                setParameters(buildUponParameters().setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true))
            }

            player = ExoPlayer.Builder(context).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    setRenderersFactory(DefaultRenderersFactory(context))
                    setUseLazyPreparation(true)
                }
                    setTrackSelector(trackSelector)
            }.build().also { exoPlayer ->
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
                exoPlayer.prepare()
                exoPlayer.volume = 0f
//                exoPlayer.playWhenReady = true

                exoPlayer.addListener(object : Player.Listener {

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        Log.e("ExoPlayer", "Error playing: $url", error)
                    }
                })
            }

            playerView?.player = player
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


        if (args?.media?.measurementMethod == ImitoCameraMode.VIDEO_MODE || isVideoFile(args?.media?.image?:"")) {
            val file = args?.media?.image
            initPlayer(file)
            player?.playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()
        getImageHandler.removeCallbacksAndMessages(null)


        releasePlayer()
    }


    override fun onDestroyView() {
        releasePlayer()
        super.onDestroyView()
    }

    companion object {
        private const val ARGS_KEY = "args_key"

        @Parcelize
        private data class Args(
            val media: MediaModel,
            val isStoma: Boolean
        ) : Parcelable

        private const val GET_IMAGE_DELAY = 500L

        fun newInstance(draftMedia: MediaModel, isStoma: Boolean) =
            AssessmentMediaFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARGS_KEY, Args(draftMedia, isStoma))
                }
            }
    }

}
