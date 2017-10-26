package com.sccomponents.gauges.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.sccomponents.gauges.ScArcGauge;
import com.sccomponents.gauges.ScCopier;
import com.sccomponents.gauges.ScGauge;
import com.sccomponents.gauges.ScNotches;
import com.sccomponents.gauges.ScPointer;
import com.sccomponents.gauges.ScWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
        gauge.addFeature(ScWriter.class);

        // Set the features stroke cap style to rounded
        gauge.findFeature(ScArcGauge.BASE_IDENTIFIER)
                .getPainter().setStrokeCap(Paint.Cap.ROUND);
        gauge.findFeature(ScArcGauge.PROGRESS_IDENTIFIER)
                .getPainter().setStrokeCap(Paint.Cap.ROUND);
    }
}
