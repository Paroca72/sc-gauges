package com.sccomponents.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.sccomponents.codes.demo.R;
import com.sccomponents.codes.gauges.ScArcGauge;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScArcGauge gauge = new ScArcGauge(this);

        FrameLayout frameLayout = this.findViewById(R.id.frameLayout);
        frameLayout.addView(gauge);
    }

}
