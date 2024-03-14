package com.example.samplewoundsdk.ui.screen.measurementfullscreen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.measurement.MeasurementMetadata
import com.example.samplewoundsdk.databinding.SampleAppActivitySampleMeasurementFullScreenBinding
import com.example.samplewoundsdk.ui.screen.base.AbsActivity
import com.example.samplewoundsdk.ui.screen.measurementfullscreen.adapter.BoundaryLabelRecyclerAdapter
import com.example.samplewoundsdk.utils.image.drawstroke.StrokeScalableImageView
import com.example.samplewoundsdk.data.pojo.measurement.ImageResolution
import com.example.samplewoundsdk.data.pojo.measurement.Vertices
import java.io.File
import java.io.Serializable
import kotlin.math.max

class MeasurementFullScreenActivity : AbsActivity<MeasurementFullScreenViewModel>() {

    private val args by lazy { intent.getSerializableExtra(KEY_ARGS) as? Args }

    override fun provideViewModelClass() = MeasurementFullScreenViewModel::class
    override fun provideLayoutId() = R.layout.sample_app_activity_sample_measurement_full_screen

    lateinit var binding: SampleAppActivitySampleMeasurementFullScreenBinding

    private var currentPictureSize: ImageResolution? = null

    override fun initListeners() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SampleAppActivitySampleMeasurementFullScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initStrokeScalableImageView()
        loadImage()
        setUpMetadataUi()
        args?.apply {
            binding.apply {
                viewModel?.apply {
                    viewModel?.getBoundaryLabelList(metadataList)

                    boundaryListLD.observe(this@MeasurementFullScreenActivity) {
                        binding.boundaryLabelListRV.adapter =
                            BoundaryLabelRecyclerAdapter(
                                labelList = it,
                                verticesSelectedIndexes = metadataList.mapIndexed { index, s -> index },
                                onSelectedLabel = { selectedIndexes ->
                                    binding.imageSSIV.setVisibilityVerticesIndexes(selectedIndexes)
                                },
                                onHideOutlineClick = { isOutlineHidden ->
                                    if (!isOutlineHidden) {
                                        binding.imageSSIV.setVisibilityVerticesIndexes(emptyList())
                                    } else {
                                        binding.imageSSIV.setVisibilityVerticesIndexes(metadataList.mapIndexed { index, s -> index })
                                    }
                                }
                            )
                    }
                }
            }
        }

        viewModel?.apply {

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
                    override fun onZoomChanged(zoom: Float) {}
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

    private fun loadImage() {
        args?.photoPath?.let { photoPath ->
            binding.apply {
                Glide.with(this@MeasurementFullScreenActivity)
                    .asBitmap()
                    .load(File(photoPath))
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
                            resource?.let {
                                currentPictureSize = ImageResolution(it.width, it.height)
                                imageSSIV.setImage(ImageSource.bitmap(it))
                                setUpMetadataUi()
                            }
                            return true
                        }
                    }).into(hidePhotoACIV)
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
        args?.apply {
            var scale = 0.0
            currentPictureSize?.let { currentSize ->
                scale = getScale(
                    currentWidth = currentSize.width,
                    currentHeight = currentSize.height,
                    originalWidth = pictureSize.width,
                    originalHeight = pictureSize.height
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
                Unit
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

    override fun onKeyboardOpen() {}

    override fun onKeyboardClose() {}

    companion object {

        private const val KEY_ARGS = "KEY_ARGS"

        private data class Args(
            val photoPath: String,
            val pictureSize: ImageResolution,
            val metadataList: List<MeasurementMetadata>
        ) : Serializable

        fun open(
            context: Context,
            photoPath: String,
            pictureSize: ImageResolution,
            metadataList: List<MeasurementMetadata>
        ) = context.startActivity(
            Intent(context, MeasurementFullScreenActivity::class.java).apply {
                putExtra(KEY_ARGS, Args(photoPath, pictureSize, metadataList))
            }
        )

    }

}