package com.savesmart;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseFile;

import java.lang.ref.WeakReference;

/**
 * Created by ZheXian on 19/7/2014.
 */
public class ImageLoaderTask extends AsyncTask<ParseFile, Void, Bitmap> {

    protected final WeakReference imageViewReference;
    private final WeakReference contextReference;
    private Bitmap bitmap;
    private byte[] bytes;

    public ImageLoaderTask(Context context, ImageView imageView) {
        imageViewReference = new WeakReference(imageView);
        contextReference = new WeakReference(context);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);


        final int color = 0xffffffff;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 4 * Resources.getSystem().getDisplayMetrics().density;


        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    @Override
    protected Bitmap doInBackground(ParseFile... parseFiles) {
        try {
            bytes = parseFiles[0].getData();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            bitmap = getRoundedCornerBitmap(bitmap);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null) {
            final ImageView imageView = (ImageView) imageViewReference.get();
            if (imageView != null) {

                Animation animFadeOut = AnimationUtils.loadAnimation((Context) contextReference.get(), R.anim.image_view_fade_out);
                final Animation animFadeIn = AnimationUtils.loadAnimation((Context) contextReference.get(), R.anim.image_view_fade_in);

                final Bitmap finalBitmap = bitmap;
                animFadeOut.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        if (finalBitmap != null) {
                            imageView.setImageBitmap(finalBitmap);
                            imageView.setAnimation(animFadeIn);

                        }
                    }
                });

                imageView.startAnimation(animFadeOut);


            }

        }
    }


}
