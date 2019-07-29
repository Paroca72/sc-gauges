package com.sccomponents.demo;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sccomponents.codes.demo.R;
import com.sccomponents.gauges.library.ScArcGauge;
import com.sccomponents.gauges.library.ScBase;
import com.sccomponents.gauges.library.ScCopier;
import com.sccomponents.gauges.library.ScDrawer;
import com.sccomponents.gauges.library.ScFeature;
import com.sccomponents.gauges.library.ScGauge;
import com.sccomponents.gauges.library.ScLabeler;
import com.sccomponents.gauges.library.ScNotches;
import com.sccomponents.gauges.library.ScPathMeasure;
import com.sccomponents.gauges.library.ScPointer;
import com.sccomponents.gauges.library.ScRepetitions;
import com.sccomponents.gauges.library.ScWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the components
        final ScArcGauge gauge = this.findViewById(R.id.gauge);
        assert gauge != null;

        final float min = 0.0f;
        final float max = 100.0f;
        int repetitions = 17;

        gauge.setRecognizePathTouch(true);
        gauge.setDoubleBuffering(true);
        gauge.setDuration(1000);
        //gauge.getBase().getPainter().setStrokeCap(Paint.Cap.ROUND);
        //gauge.getProgress().getPainter().setStrokeCap(Paint.Cap.ROUND);
        //gauge.removeFeature(gauge.getProgress());
        gauge.setFillingArea(ScDrawer.FillingArea.NONE);
        gauge.setHighValue(0.00001f);
        gauge.setAngleStart(-0);
        gauge.setAngleSweep(360);

        //ScCopier progress = gauge.getProgress();
        //progress.setColors(Color.BLUE);

        ScNotches notches = gauge.getNotches();
        notches.setWidths(10);
        notches.setHeights(100);
        //notches.setSpaceBetweenRepetitions(16.667f);
        notches.setLastRepetitionOnPathEnd(true);
        notches.setRepetitions(repetitions);
        notches.setColors(Color.RED);
        notches.setPosition(ScFeature.Positions.INSIDE);

        ScNotches another = (ScNotches) gauge.addFeature(ScNotches.class);
        another.setTag("ANOTHER");
        another.setWidths(5);
        another.setHeights(50);
        notches.setLastRepetitionOnPathEnd(true);
        another.setRepetitions((repetitions - 1) * 5 + 1);
        another.setColors(Color.BLUE);
        another.setPosition(ScFeature.Positions.INSIDE);

        ScPointer pointer = gauge.getHighPointer();
        //pointer.setVisible(true);
        pointer.setWidths(50);
        pointer.setHeights(50);
        pointer.setColors(Color.RED);
        pointer.setHaloWidth(25);
        pointer.setHaloAlpha(50);

        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onDrawContour(ScGauge gauge, ScFeature.ContourInfo info) {
                // NOP
            }

            @Override
            public void onDrawRepetition(ScGauge gauge, ScRepetitions.RepetitionInfo info) {
            }
        });

        // ------------------------------------------------------------------------
        final TextView textView = this.findViewById(R.id.text);
        final TextView angleView = this.findViewById(R.id.angle);
        final ImageView indicator = this.findViewById(R.id.indicator);

        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(ScGauge gauge, float lowValue, float highValue, boolean isRunning) {
                float angle = ((ScArcGauge) gauge).percentageToAngle(highValue);
                textView.setText(String.format("%s", angle));

                float value = ScGauge.percentageToValue(highValue, min, max);
                angleView.setText(String.format("%s", value));

                float fixed = angle - 126.0f;
                //fixed = new BigDecimal(fixed)
                //        .setScale(2, RoundingMode.HALF_DOWN)
                //        .floatValue();
                indicator.setRotation(fixed);
            }
        });

        // ------------------------------------------------------------------------
        Button increase = this.findViewById(R.id.btnIncrease);
        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float currentValue = gauge.getHighValue(min, max) + 6.25f;
                if (currentValue > max)
                    currentValue = 0.0f;

                gauge.setHighValue(currentValue, min, max);
            }
        });

        // ------------------------------------------------------------------------
        SeekBar bar = this.findViewById(R.id.seekBar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                i *= 2;
                gauge.setPadding(i, i, i, i);
                gauge.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
    }

}
