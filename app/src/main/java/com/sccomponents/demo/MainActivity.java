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
import com.sccomponents.gauges.library.ScRepetitions;
import com.sccomponents.gauges.library.ScWriter;

public class MainActivity extends AppCompatActivity {

    private ScArcGauge mGauge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mGauge = this.findViewById(R.id.gauge);
        this.mGauge.setFillingArea(ScDrawer.FillingArea.NONE);
        this.mGauge.setAngleStart(-270);
        this.mGauge.setAngleSweep(270);

        ScCopier base = this.mGauge.getBase();
        base.setWidths(5);

        ScNotches notches = this.mGauge.getNotches();
        notches.setRepetitions(2);
        notches.setHeights(50);
        notches.setWidths(30);
        notches.setColors(Color.RED);
        notches.setPosition(ScFeature.Positions.OUTSIDE);
        notches.setEdges(ScFeature.Positions.INSIDE);

        SeekBar seek = this.findViewById(R.id.seekBar);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mGauge.setAngleStart(i - 180);
                //mGauge.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
