package com.example.samplewoundsdk.utils.image.drawstroke;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.samplewoundsdk.R;
import com.example.samplewoundsdk.databinding.SampleViewMeasureRulerBinding;

import java.text.DecimalFormat;


public class MeasureRulerLine extends LinearLayout {

    private final DecimalFormat decimalFormat1 = new DecimalFormat("0.0");
    private final DecimalFormat decimalFormat2 = new DecimalFormat("0.00");

    View line;
    View lineCenter;
    TextView text;
    private SampleViewMeasureRulerBinding binding;

    int greenColor;

    private float viewScale;
    private int textSize = 18;
    private int pxInSm = 100;

    private float minLine;
    private float maxLine;
    private float maxScale;

    private Units currentUnits = Units.Centimeter;
    private float currentLineScale;

    public MeasureRulerLine(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeasureRulerLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        binding = SampleViewMeasureRulerBinding.inflate(LayoutInflater.from(getContext()), this, true);
        line = binding.lineLayoutRL;
        lineCenter = binding.lineV;
        text = binding.valueACTV;
        greenColor = ContextCompat.getColor(getContext(), R.color.sample_app_color_green);
        minLine = dpToPx(60);
        maxLine = dpToPx(120);
        maxScale = 3;
    }

    public void setPxInSm(int pxInSm) {
        this.pxInSm = pxInSm;
    }

    public void setViewScale(float viewScale) {
        float lineScale = 1f;
        float liveWidth = pxInSm * viewScale * lineScale;
        if (liveWidth < minLine) {
            while (liveWidth < minLine) {
                lineScale *= 2;
                liveWidth = pxInSm * viewScale * lineScale;
            }
        } else if (liveWidth > maxLine) {
            while (liveWidth > maxLine) {
                lineScale /= 2;
                liveWidth = pxInSm * viewScale * lineScale;
            }
        }

        float scaleInPercent = (viewScale * 100) / maxScale;
        float factLineWidth = ((maxLine - minLine) * scaleInPercent) / 100;
        factLineWidth += minLine;

        ViewGroup.LayoutParams layoutParams = line.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = (int) (factLineWidth);
        }
        line.invalidate();
        lineCenter.invalidate();

        setText(lineScale);
    }

    private void setText(float value) {
        currentLineScale = value;
        if (value < 0.5) {
            this.text.setText(replaceComaByDot(decimalFormat2.format(value * currentUnits.getCountInCentimeter())) + " " + currentUnits.getAbbreviation());
        } else {
            this.text.setText(replaceComaByDot(decimalFormat1.format(value * currentUnits.getCountInCentimeter())) + " " + currentUnits.getAbbreviation());
        }
    }

    public void changeUnits(Units units) {
        currentUnits = units;
        setText(currentLineScale);
    }

    private String replaceComaByDot(String input) {
        return input.replaceAll(",", ".");
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    public float dpToPx(int dp) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }

    enum Units {
        Centimeter("cm", 1),
        Millimeter("mm", 10);

        private String abbreviation;
        private int countInCentimeter;

        Units(String abbreviation, int countInCentimeter) {
            this.abbreviation = abbreviation;
            this.countInCentimeter = countInCentimeter;
        }

        public int getCountInCentimeter() {
            return countInCentimeter;
        }

        public String getAbbreviation() {
            return abbreviation;
        }


        @Override
        public String toString() {
            return abbreviation;
        }
    }

}