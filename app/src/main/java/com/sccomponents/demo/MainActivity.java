package com.sccomponents.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sccomponents.codes.demo.R;
import com.sccomponents.gauges.library.ScArcGauge;
import com.sccomponents.gauges.library.ScCopier;
import com.sccomponents.gauges.library.ScDrawer;
import com.sccomponents.gauges.library.ScFeature;
import com.sccomponents.gauges.library.ScGauge;
import com.sccomponents.gauges.library.ScNotches;
import com.sccomponents.gauges.library.ScRepetitions;
import com.sccomponents.gauges.library.ScWriter;

public class MainActivity extends AppCompatActivity {

    private ScArcGauge mGauge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mGauge = this.findViewById(R.id.gauge);
        this.mGauge.setFillingArea(ScDrawer.FillingArea.NONE);
        this.mGauge.setAngleStart(-270);
        this.mGauge.setAngleSweep(270);

        ScNotches notches = this.mGauge.getNotches();
        notches.setRepetitions(8);
        notches.setHeights(50);
        notches.setWidths(10);
        notches.setColors(Color.RED);

        ScWriter writer = this.mGauge.getWriter();
        writer.setTokens("1", "22", "333", "4444", "1", "22", "333", "4444");

        final Paint paint = writer.getPainter();
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);

        this.mGauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onDrawContour(ScFeature.ContourInfo info) {

            }

            @Override
            public void onDrawRepetition(ScRepetitions.RepetitionInfo info) {
                if (info.source instanceof  ScWriter) {
                    Rect bounds = new Rect();
                    String text = ((ScWriter.TokenInfo) info).text;
                    paint.getTextBounds(text, 0, text.length(), bounds);

                    double radiant = Math.toRadians(info.tangent) + Math.PI / 2;
                    double distance = 60;
                    double minWidth = 50;

                    info.offset[0] = (float) ((minWidth + bounds.width() / 2) * Math.cos(radiant));
                    info.offset[1] = (float) ((distance + bounds.height() / 2) * Math.sin(radiant));
                    info.tangent = 0;
                }
            }
        });
    }

}
