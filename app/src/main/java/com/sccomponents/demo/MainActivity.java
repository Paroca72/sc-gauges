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
import com.sccomponents.gauges.library.ScPointer;
import com.sccomponents.gauges.library.ScRepetitions;
import com.sccomponents.gauges.library.ScWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the components
        final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        ScCopier base = gauge.getBase();
        base.setColors(Color.parseColor("#ff0000"));
        base.setWidths(100);

        ScCopier progress = gauge.getProgress();
        progress.setWidths(100);
        progress.setColors(Color.parseColor("#00ff00"));

        ScNotches notches = gauge.getNotches();
        notches.setRepetitions(11);
        notches.setWidths(10);
        notches.setHeights(10);
        notches.setColors(Color.parseColor("#000000"));
        notches.setPosition(ScFeature.Positions.MIDDLE);
        gauge.bringOnTop(notches);

        ScLabeler labeler = gauge.getLabeler();
        labeler.setVisible(true);
        labeler.setBending(true);
        labeler.setLetterSpacing(0.3f);
        //labeler.setBackground(Color.parseColor("#ffff00"));
        labeler.getPainter().setTextSize(60);
        labeler.getPainter().setTextAlign(Paint.Align.RIGHT);
        labeler.setPosition(ScFeature.Positions.MIDDLE);
        labeler.setFormat("0");

        gauge.setAngleStart(0);
        gauge.setAngleSweep(270);
        gauge.setHighValue(10);
        //gauge.setPadding(30, 30, 30, 30);
    }

}
