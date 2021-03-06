package com.packtpub.rxjava_essentials;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import rx.schedulers.Schedulers;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 2016/9/27 16:27.
 * @desc: Utils
 */
public class Utils {
    private static final String TAG = "Utils";

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void storeBitmap(Context context, Bitmap bitmap, String filename) {
        Schedulers.io().createWorker().schedule(() -> {
            blockingStoreBitmap(context, bitmap, filename);
        });
    }

    private static void blockingStoreBitmap(Context context, Bitmap bitmap, String filename) {
        FileOutputStream fOut = null;
        try {
            fOut = context.openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
