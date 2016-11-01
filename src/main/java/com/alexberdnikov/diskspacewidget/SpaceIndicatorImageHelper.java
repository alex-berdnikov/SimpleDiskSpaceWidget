package com.alexberdnikov.diskspacewidget;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class SpaceIndicatorImageHelper {
    public static Bitmap drawIndicator(int usedSpacePercents, int color) {
        final int INDICATOR_WIDTH = 100;
        final int INDICATOR_HEIGHT = 1;

        Bitmap bitmap = Bitmap.createBitmap(
                INDICATOR_WIDTH, INDICATOR_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0f, 0f, usedSpacePercents, INDICATOR_HEIGHT, paint);

        return bitmap;
    }
}
