package com.sccomponents.codes.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.sccomponents.codes.gauges.ScArcGauge;
import com.sccomponents.codes.gauges.ScGauge;
import com.sccomponents.codes.gauges.ScRepetitions;
import com.sccomponents.codes.gauges.ScWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the components
        final TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
        ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);

        // Set the features stroke cap style to rounded
        gauge.getBase()
                .getPainter().setStrokeCap(Paint.Cap.ROUND);
        gauge.getProgress()
                .getPainter().setStrokeCap(Paint.Cap.ROUND);

        ScWriter writer = gauge.getWriter();
        writer.setTokens("01", "02", "03", "04", "05");
        writer.setColors(Color.GREEN, Color.RED);
        writer.getPainter().setTextAlign(Paint.Align.CENTER);
        writer.getPainter().setTextSize(48);
        writer.setOnDrawRepetitionListener(new ScRepetitions.OnDrawRepetitionListener() {
            @Override
            public void onDrawRepetition(ScRepetitions.RepetitionInfo info) {
                info.offset[1] = 60;
                info.angle = (-info.tangent) / 2;
            }
        });

        // If you set the value from the xml that not produce an event so I will change the
        // value from code.
        gauge.setHighValue(50);

        // Each time I will change the value I must write it inside the counter text.
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float lowValue, float highValue) {
                counter.setText((int) highValue + "%");
            }
        });
    }

}
