package com.sccomponents.gauges.demo;

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
import android.widget.TextView;

import com.sccomponents.gauges.ScArcGauge;
import com.sccomponents.gauges.ScCopier;
import com.sccomponents.gauges.ScFeature;
import com.sccomponents.gauges.ScGauge;
import com.sccomponents.gauges.ScNotches;
import com.sccomponents.gauges.ScWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

// Find the components
        final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        // Set the values.
        gauge.setHighValue(55);

        // Set the filter of the base
        ScFeature base = gauge.findFeature(ScGauge.BASE_IDENTIFIER);
        BlurMaskFilter filter = new BlurMaskFilter(10, BlurMaskFilter.Blur.INNER);
        base.getPainter().setMaskFilter(filter);

        // Writer
        String[] tokens = new String[10];
        for (int index = 0; index < 10; index++) {
            tokens[index] = Integer.toString((index + 1) * 10);
        }

        ScWriter writer = gauge.getWriter();
        writer.setTokens(tokens);

    }

}
