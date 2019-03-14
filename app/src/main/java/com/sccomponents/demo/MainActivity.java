package com.sccomponents.demo;

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
import android.widget.SeekBar;
import android.widget.TextView;

import com.sccomponents.codes.demo.R;
import com.sccomponents.gauges.library.ScArcGauge;
import com.sccomponents.gauges.library.ScBase;
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

        final ImageView indicator = (ImageView) this.findViewById(R.id.indicator);
        assert indicator != null;

        // Set the center pivot for a right rotation
        indicator.setPivotX(39);
        indicator.setPivotY(88);

        // Set the filter of the base
        BlurMaskFilter filter = new BlurMaskFilter(10, BlurMaskFilter.Blur.INNER);
        ScCopier base = gauge.getBase();
        base.getPainter().setMaskFilter(filter);

        // Writer
        String[] tokens = new String[11];
        for (int index = 0; index < 11; index++) {
            tokens[index] = Integer.toString((index + 1) * 10);
        }

        ScWriter writer = gauge.getWriter();
        writer.setRepetitionOffset(50);
        writer.setTokens(tokens);

        // Notches
        ScNotches notches = gauge.getNotches();
        notches.setRepetitions(tokens.length);

        // Set the values.
        gauge.setHighValue(60);

        // Each time I will change the value I must write it inside the counter text.
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float lowValue, float highValue, boolean isRunning) {
                // Convert the percentage value in an angle
                float angle = gauge.percentageToAngle(highValue);
                indicator.setRotation(angle);
            }
        });

        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onDrawContour(ScFeature.ContourInfo info) {
                // NOP
            }

            @Override
            public void onDrawRepetition(ScRepetitions.RepetitionInfo info) {
                if (info instanceof ScNotches.NotchInfo) {
                    ScNotches notches = ((ScNotches.NotchInfo) info).source;
                    info.offset[1] = gauge.dipToPixel(6);
                    info.visible = info.repetition > 1 && info.repetition < notches.getRepetitions();
                }

                if (info instanceof ScWriter.TokenInfo) {
                    // Offset
                    info.offset[1] = - gauge.dipToPixel(8);

                    // Highlight
                    int sector = (int) (gauge.getHighValue() / 10);
                    info.color = sector == info.repetition ?
                            Color.BLACK : Color.parseColor("#cccccc");
                }
            }
        });

    }

}
