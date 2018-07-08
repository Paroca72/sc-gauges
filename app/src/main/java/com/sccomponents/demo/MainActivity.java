package com.sccomponents.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;

import com.sccomponents.codes.demo.R;
import com.sccomponents.gauges.library.ScArcGauge;
import com.sccomponents.gauges.library.ScCopier;
import com.sccomponents.gauges.library.ScDrawer;
import com.sccomponents.gauges.library.ScFeature;
import com.sccomponents.gauges.library.ScGauge;
import com.sccomponents.gauges.library.ScNotches;
import com.sccomponents.gauges.library.ScPointer;
import com.sccomponents.gauges.library.ScRepetitions;
import com.sccomponents.gauges.library.ScWriter;

public class MainActivity extends AppCompatActivity {

    private ScArcGauge mGauge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int size = 60;

        this.mGauge = this.findViewById(R.id.gauge);
        //this.mGauge.setAngleStart(-225);
        this.mGauge.setAngleSweep(360);
        this.mGauge.setRecognizePathTouch(true);
        this.mGauge.setPathTouchThreshold(size);

        ScCopier base = this.mGauge.getBase();
        base.setWidths(size);
        base.setColors(Color.WHITE, Color.WHITE);
        //base.setVisible(false);
        //base.getPainter().setStrokeCap(Paint.Cap.ROUND);
        base.setPosition(ScFeature.Positions.OUTSIDE);

        ScCopier progress = this.mGauge.getProgress();
        progress.setWidths(size);
        progress.setColors(Color.YELLOW, Color.MAGENTA);
        //progress.setColorsMode(ScFeature.ColorsMode.SOLID);
        progress.getPainter().setStrokeCap(Paint.Cap.ROUND);
        progress.setPosition(ScFeature.Positions.OUTSIDE);
    }

}
