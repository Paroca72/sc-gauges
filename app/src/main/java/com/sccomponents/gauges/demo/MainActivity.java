package com.sccomponents.gauges.demo;

import android.graphics.Color;
import android.graphics.Paint;
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

        // Get the gauge
        final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.alarmVolumeGauge);
        assert gauge != null;

        // Remove all the feature
        gauge.removeAllFeatures();

        // Create the base notches.
        ScNotches base = (ScNotches) gauge.addFeature(ScNotches.class);
        base.setTag(ScGauge.BASE_IDENTIFIER);
        base.setCount(30);
        //base.setPosition(ScNotches.NotchPositions.INSIDE);

        // Create the progress notches.
        ScNotches progress = (ScNotches) gauge.addFeature(ScNotches.class);
        progress.setTag(ScGauge.PROGRESS_IDENTIFIER);
        progress.setCount(30);
        //progress.setPosition(ScNotches.NotchPositions.INSIDE);

        // Set value
        gauge.setPathTouchThreshold(20);
        gauge.setHighValue(75, 0, 100);

        // Each time I will change the value I must write it inside the counter text.
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float lowValue, float highValue) {
                // Write the value
                int value = (int) ScGauge.percentageToValue(highValue, 0, 100);
            }
        });

        // Before draw
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
                // Do nothing
            }

            @Override
            public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
                // Set the length of the notch
                info.length = gauge.dipToPixel(info.index + 5);
            }

            @Override
            public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
                // Do nothing
            }

            @Override
            public void onBeforeDrawToken(ScWriter.TokenInfo info) {
                // Do nothing
            }
        });
    }
}
