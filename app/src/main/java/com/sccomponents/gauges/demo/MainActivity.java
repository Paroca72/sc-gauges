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
import com.sccomponents.gauges.ScWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Dimensions
        int padding = 24;
        Rect drawArea = new Rect(padding, padding, 900 - padding, 900 - padding);

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
        path.moveTo(drawArea.left + 100, drawArea.bottom - 100);
        path.quadTo(drawArea.centerX(), drawArea.top, drawArea.right - 100, drawArea.bottom - 100);

        // Feature
        ScCopier copier = new ScCopier(path);
        copier.setWidths(100);
        copier.setColors(Color.RED, Color.GREEN, Color.BLUE);
        //copier.setColorsMode(ScCopier.ColorsMode.SOLID);
        copier.setPosition(ScFeature.Positions.MIDDLE);
        copier.getPainter().setStrokeCap(Paint.Cap.ROUND);
        copier.setEdges(ScFeature.Positions.OUTSIDE);
        copier.draw(canvas);

        ScPathMeasure measure = new ScPathMeasure(path, false);
        Paint paint = new Paint();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.RED);
        canvas.drawPath(path, paint);


        // Add the bitmap to the container
        imageContainer.setImageBitmap(bitmap);
    }

}
