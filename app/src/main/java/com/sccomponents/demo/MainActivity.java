package com.sccomponents.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.sccomponents.codes.demo.R;
import com.sccomponents.codes.gauges.ScArcGauge;
import com.sccomponents.codes.gauges.ScDrawer;
import com.sccomponents.codes.gauges.ScNotches;

public class MainActivity extends AppCompatActivity {

    private ScArcGauge mGauge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mGauge = this.findViewById(R.id.gauge);
        //this.mGauge.setFillingMode(ScDrawer.FillingMode.STRETCH);
        this.mGauge.setFillingArea(ScDrawer.FillingArea.VERTICAL);
        ScNotches notches = this.mGauge.getNotches();
        notches.setHeights(10);
        notches.setWidths(10);
        notches.setLastRepetitionOnPathEnd(false);

        this.mGauge.post(new Runnable() {
            @Override
            public void run() {
                int ticks = 10;
                float space = mGauge.getPathMeasure().getLength() / ticks;
                mGauge.getNotches().setSpaceBetweenRepetitions(space - 0.01f);
            }
        });
    }

}
