package com.sccomponents.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.sccomponents.codes.demo.R;
import com.sccomponents.codes.gauges.ScArcGauge;
import com.sccomponents.codes.gauges.ScNotches;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScArcGauge gauge = this.findViewById(R.id.gauge);
        ScNotches notches = gauge.getNotches();
        notches.setSpaceBetweenRepetitions(150);
        notches.setHeights(10);
        notches.setWidths(10);
        notches.setLastRepetitionOnPathEnd(false);
        notches.setRepetitionOffset(-100);
    }

}
