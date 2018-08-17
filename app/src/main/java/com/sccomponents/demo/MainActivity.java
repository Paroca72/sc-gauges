package com.sccomponents.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.sccomponents.codes.demo.R;
import com.sccomponents.gauges.library.ScArcGauge;
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

        ScArcGauge gauge = this.findViewById(R.id.gauge);
        gauge.setHighValue(80);
        gauge.setRecognizePathTouch(true);

        ScCopier base = gauge.getBase();
        base.setWidths(50);

        ScCopier progress = gauge.getProgress();
        progress.setWidths(50);
        progress.setColors(Color.GREEN);

        ScLabeler labeler = gauge.getLabeler();
        labeler.setVisible(true);
        labeler.setColors(Color.BLUE);
        labeler.getPainter().setTextSize(80);
    }

}
