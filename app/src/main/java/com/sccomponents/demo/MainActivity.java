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
import android.widget.TextView;

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

        // Find the components
        final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        ScWriter writer = gauge.getWriter();
        //writer.setConsiderFontMetrics(false);
        writer.setInterline(1.2f);
        writer.setTokens("Text\nText", "Text\nText", "Text\nText");

    }

}
