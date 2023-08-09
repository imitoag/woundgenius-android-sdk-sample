package com.example.samplewoundsdk.ui.screen.measurementfullscreen

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.PointF
import android.os.Bundle
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.measurement.MeasurementMetadata
import com.example.samplewoundsdk.databinding.SampleAppActivitySampleMeasurementFullScreenBinding
import com.example.samplewoundsdk.ui.screen.base.AbsActivity
import com.example.samplewoundsdk.ui.screen.measurementfullscreen.adapter.BoundaryLabelRecyclerAdapter
import com.example.samplewoundsdk.utils.image.drawstroke.StrokeScalableImageView
import java.io.Serializable

class MeasurementFullScreenActivity : AbsActivity<MeasurementFullScreenViewModel>() {

    private val args by lazy { intent.getSerializableExtra(KEY_ARGS) as? Args }

    override fun provideViewModelClass() = MeasurementFullScreenViewModel::class
    override fun provideLayoutId() = R.layout.sample_app_activity_sample_measurement_full_screen

    lateinit var binding: SampleAppActivitySampleMeasurementFullScreenBinding

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
                boundaryLabelListRV.adapter = BoundaryLabelRecyclerAdapter(
                    labelList = metadataList.mapIndexed { index, _ -> index + 1 },
                    onSelectedLabel = { selectedIndexes ->
                        imageSSIV.setVisibilityVerticesIndexes(selectedIndexes)
                    }
                )
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
                        vertices: ArrayList<ArrayList<Point>>?,
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
        val file = args?.photoPath
        val bitmap = BitmapFactory.decodeFile(file)
        binding.imageSSIV.setImage(ImageSource.bitmap(bitmap))
    }

    private fun setUpMetadataUi() {
        args?.apply {

            val allVertexesList = ArrayList<List<Point>>()
            val widthIndexes = ArrayList<Pair<Int?, Int?>>()
            val lengthIndexes = ArrayList<Pair<Int?, Int?>>()
            val areaList = ArrayList<Double>()

            metadataList.forEachIndexed { index, boundaryMetadata ->
                boundaryMetadata.apply {
                    boundaryMetadata.vertices?.let {
                        allVertexesList.add(it.map {
                            Point(
                                it.x,
                                it.y
                            )
                        })
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
            val metadataList: List<MeasurementMetadata>
        ) : Serializable

        fun open(
            context: Context,
            photoPath: String,
            metadataList: List<MeasurementMetadata>
        ) = context.startActivity(
            Intent(context, MeasurementFullScreenActivity::class.java).apply {
                putExtra(KEY_ARGS, Args(photoPath, metadataList))
            }
        )

    }

}