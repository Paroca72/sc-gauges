package com.sccomponents.demo;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.sccomponents.gauges.library.ScPathMeasure;
import com.sccomponents.gauges.library.ScPointer;
import com.sccomponents.gauges.library.ScRepetitions;
import com.sccomponents.gauges.library.ScWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    private int mAngle = 1;
    private ScArcGauge mGauge = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Gauge
        this.mGauge = this.findViewById(R.id.gauge);

        // ------------------------------------------------------------------------
        Button increase = this.findViewById(R.id.btnIncrease);
        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Increase angle
                MainActivity.this.mGauge.setAngleStart(MainActivity.this.mAngle ++);

                // Update the text
                TextView text = MainActivity.this.findViewById(R.id.txtAngle);
                text.setText(MainActivity.this.mAngle + "");
            }
        });

    }

}
