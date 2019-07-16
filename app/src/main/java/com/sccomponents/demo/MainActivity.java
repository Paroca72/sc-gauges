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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the components
        final ScArcGauge gauge = this.findViewById(R.id.gauge);
        assert gauge != null;

        gauge.setRecognizePathTouch(true);
        gauge.setDoubleBuffering(true);
        //gauge.setDuration(1000);
        //gauge.getBase().getPainter().setStrokeCap(Paint.Cap.ROUND);
        //gauge.getProgress().getPainter().setStrokeCap(Paint.Cap.ROUND);
        //gauge.removeFeature(gauge.getProgress());
        gauge.setHighValue(100);

        //ScCopier progress = gauge.getProgress();
        //progress.setColors(Color.BLUE);

        ScNotches notches = gauge.getNotches();
        notches.setWidths(10);
        notches.setHeights(50);
        notches.setSpaceBetweenRepetitions(10);
        //notches.setLastRepetitionOnPathEnd(false);
        //notches.setRepetitions(10);
        notches.setColors(Color.RED);

        ScPointer pointer = gauge.getHighPointer();
        //pointer.setVisible(true);
        pointer.setWidths(50);
        pointer.setHeights(50);
        pointer.setColors(Color.RED);
        pointer.setHaloWidth(25);
        pointer.setHaloAlpha(50);

        SeekBar bar = this.findViewById(R.id.seekBar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
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
