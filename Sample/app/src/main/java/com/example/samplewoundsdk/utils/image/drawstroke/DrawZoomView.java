package com.example.samplewoundsdk.utils.image.drawstroke;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.samplewoundsdk.R;

import java.util.ArrayList;


public class DrawZoomView extends SubsamplingScaleImageView {

    public int green;

    private Paint paint;
    private Paint vertexPaint;
    private PointF zoomPoint;
    private ArrayList<Point> vertices;
    private boolean isPathClosed = false;

    public DrawZoomView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public DrawZoomView(Context context) {
        super(context);
        init();
    }

    private void init() {
        green = ContextCompat.getColor(getContext(), R.color.sample_app_color_green);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        vertexPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        vertexPaint.setColor(Color.GREEN);
        vertexPaint.setStrokeWidth(5);

        setMaxScale(5);
        setMinScale(5);

    }

    public void setZoomPoint(PointF zoomPoint) {
        this.zoomPoint = zoomPoint;
//        this.zoomPoint.x=zoomPoint.x*4;
//        this.zoomPoint.y=zoomPoint.y*4;
//        this.zoomPoint = viewToSourceCoord(zoomPoint);
//        animateScale(5);
//        animateCenter(zoomPoint);
        setScaleAndCenter(2, this.zoomPoint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPolygon(canvas);


        if (zoomPoint != null) {
            PointF pointF = sourceToViewCoord(zoomPoint);
            if (pointF != null)
                canvas.drawCircle(pointF.x, pointF.y, 10, paint);
        }
    }

    private void drawPolygon(Canvas canvas) {
        try {

            if (vertices == null || vertices.isEmpty()) {
                return;
            }
            int size = vertices.size();
            if (size > 1) {
                float[] lines = new float[((size) * 4)];
                for (int i = 1; i < size; i++) {
                    Point start = sourceToViewCoordInt(vertices.get(i - 1));
                    Point end = sourceToViewCoordInt(vertices.get(i));

                    lines[(i) * 4 - 4] = start.x;
                    lines[(i) * 4 - 3] = start.y;
                    lines[(i) * 4 - 2] = end.x;
                    lines[(i) * 4 - 1] = end.y;
                }
                if (isPathClosed) {
                    Point start = sourceToViewCoordInt(vertices.get(0));
                    Point end = sourceToViewCoordInt(vertices.get(size - 1));

                    lines[(size) * 4 - 4] = start.x;
                    lines[(size) * 4 - 3] = start.y;
                    lines[(size) * 4 - 2] = end.x;
                    lines[(size) * 4 - 1] = end.y;
                }
                canvas.drawLines(lines, vertexPaint);
            }
            for (int i = 0; i < size; i++) {
                Point vertex = vertices.get(i);
                vertex = sourceToViewCoordInt(vertex);
                if (vertex == null) return;
                canvas.drawCircle(vertex.x, vertex.y, 10, vertexPaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Point sourceToViewCoordInt(Point vertex) {
        PointF pointF = sourceToViewCoord(vertex.x, vertex.y);
        if (pointF == null) {
            return null;
        }
        return new Point((int) pointF.x, (int) pointF.y);

    }


    public void setVertices(ArrayList<Point> vertices, boolean closed) {
        this.vertices = vertices;
        isPathClosed = closed;
    }
}