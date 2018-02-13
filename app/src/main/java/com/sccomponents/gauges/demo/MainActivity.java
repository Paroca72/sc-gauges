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
import com.sccomponents.gauges.ScPathMeasure;
import com.sccomponents.gauges.ScPointer;
import com.sccomponents.gauges.ScRepetitions;
import com.sccomponents.gauges.ScWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Dimensions
        int padding = 30;
        final Rect drawArea = new Rect(padding, padding, 500 - padding, 300 - padding);

        // Get the main layout
        ImageView imageContainer = (ImageView) this.findViewById(R.id.image);
        assert imageContainer != null;

        // Create a bitmap and link a canvas
        Bitmap bitmap = Bitmap.createBitmap(
                drawArea.width() + padding * 2, drawArea.height() + padding * 2,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#f5f5f5"));

        // Create the path building a bezier curve from the left-top to the right-bottom angles of
        // the drawing area.
        Path path = new Path();
        path.moveTo(drawArea.left, drawArea.top);
        path.quadTo(drawArea.centerX(), drawArea.top, drawArea.centerX(), drawArea.centerY());
        path.quadTo(drawArea.centerX(), drawArea.bottom, drawArea.right, drawArea.bottom);

        // Draw the path only for have a reference
        Paint temp = new Paint();
        temp.setStyle(Paint.Style.STROKE);
        temp.setStrokeWidth(2);
        canvas.drawPath(path, temp);

        // Feature
        ScWriter writer = new ScWriter (path);
        writer.setTokens("FIRST", "SECOND", "THIRD", "FOURTH");
        writer.getPainter().setTextAlign(Paint.Align.LEFT);
        writer.setPosition(ScFeature.Positions.MIDDLE);
        //writer.setBending(true);
        writer.setOnDrawRepetitionListener(new ScRepetitions.OnDrawRepetitionListener() {
            @Override
            public void onDrawRepetition(ScRepetitions.RepetitionInfo info) {
                info.angle = -45;
                info.offset[1] = -10;
            }
        });
        writer.draw(canvas);

        // Add the bitmap to the container
        imageContainer.setImageBitmap(bitmap);
    }

}
