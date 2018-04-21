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
import com.sccomponents.codes.gauges.ScCopier;
import com.sccomponents.codes.gauges.ScDrawer;
import com.sccomponents.codes.gauges.ScFeature;
import com.sccomponents.codes.gauges.ScGauge;
import com.sccomponents.codes.gauges.ScNotches;
import com.sccomponents.codes.gauges.ScRepetitions;
import com.sccomponents.codes.gauges.ScWriter;

public class MainActivity extends AppCompatActivity {

    private ScArcGauge mGauge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mGauge = this.findViewById(R.id.gauge);
        //this.mGauge.setFillingMode(ScDrawer.FillingMode.STRETCH);
        this.mGauge.setAngleStart(-180);
        this.mGauge.setAngleSweep(180);

        ScCopier base = this.mGauge.getBase();
        base.setWidths(20);
        base.setColorsMode(ScFeature.ColorsMode.GRADIENT);
        base.setColors(Color.GREEN, Color.TRANSPARENT, Color.RED);
    }

}
