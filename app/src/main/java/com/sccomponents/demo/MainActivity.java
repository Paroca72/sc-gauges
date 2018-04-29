package com.sccomponents.demo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sccomponents.codes.demo.R;
import com.sccomponents.gauges.library.ScArcGauge;
import com.sccomponents.gauges.library.ScCopier;
import com.sccomponents.gauges.library.ScFeature;
import com.sccomponents.gauges.library.ScNotches;

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

        ScNotches notches = this.mGauge.getNotches();
        notches.setRepetitions(4);
        notches.setHeights(50);
        notches.setWidths(2);
        notches.setLastRepetitionOnPathEnd(true);

        ScCopier base = this.mGauge.getBase();
        base.setWidthsMode(ScFeature.WidthsMode.ROUGH);
        base.setWidths(
                100,
                0,
                100
        );
        base.setColorsMode(ScFeature.ColorsMode.SOLID);
        base.setColors(
                Color.BLUE,
                Color.TRANSPARENT,
                Color.RED
        );
        base.setPosition(ScFeature.Positions.OUTSIDE);
    }

}
