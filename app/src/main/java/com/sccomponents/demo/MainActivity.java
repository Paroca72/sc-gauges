package com.sccomponents.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.sccomponents.codes.demo.R;
import com.sccomponents.codes.gauges.ScArcGauge;
import com.sccomponents.codes.gauges.ScCopier;
import com.sccomponents.codes.gauges.ScGauge;
import com.sccomponents.codes.gauges.ScNotches;
import com.sccomponents.codes.gauges.ScRepetitions;
import com.sccomponents.codes.gauges.ScWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the components
        ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);

        final ScWriter writer = gauge.getWriter();
        writer.setTokens("01", "02", "03", "04", "05");
        writer.getPainter().setTextAlign(Paint.Align.CENTER);
        writer.getPainter().setTextSize(48);
        writer.setOnDrawRepetitionListener(new ScRepetitions.OnDrawRepetitionListener() {
            @Override
            public void onDrawRepetition(ScRepetitions.RepetitionInfo info) {
                info.offset[1] = 60;
                info.angle = (-info.tangent) / 2;
            }
        });

        ScCopier progress = gauge.getProgress();
        progress.setColors(Color.GREEN, Color.YELLOW, Color.RED);

        ScNotches notches = gauge.getNotches();
        gauge.bringOnTop(notches);
        gauge.setDuration(1000);

        // If you set the value from the xml that not produce an event so I will change the
        // value from code.
        gauge.setHighValue(80);
    }

}
