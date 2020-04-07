package com.zack.xjht.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.InputStream;

/**
 * Created by Administrator on 2017-06-06.
 */

public class BitmapUtils {


    /**
     * BitMap转成灰色
     * @param bitmap
     * @return
     */
    public static Bitmap convert2Gray(Bitmap bitmap) {
        /**
         *  [ a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t ] When applied to a color [r, g, b, a],
         *  the resulting color is computed as (after clamping)
         *  R' = a*R + b*G + c*B + d*A + e; G' = f*R + g*G + h*B + i*A + j;
         *  B' = k*R + l*G + m*B + n*A + o; A' = p*R + q*G + r*B + s*A + t;
         */
        ColorMatrix colorMatrix = new ColorMatrix();
        /**
         * Set the matrix to affect the saturation(饱和度) of colors.
         * A value of 0 maps the color to gray-scale（灰阶）. 1 is identity
         */
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);

        Paint paint = new Paint();
        paint.setColorFilter(filter);

        Bitmap result = bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return result;
    }

    /**
     * 图片转灰度
     *
     * @param bmSrc
     * @return
     */
    public static Bitmap bitmap2Gray(Bitmap bmSrc) {
        int width, height;
        height = bmSrc.getHeight();
        width = bmSrc.getWidth();
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);

        return bmpGray;
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is,null,opt);
    }

    /**
     * 图片反转
     *
     * @param bmp
     * @param flag
     *            0为水平反转，1为垂直反转
     * @return
     */
    public static Bitmap reverseBitmap(Bitmap bmp, int flag) {
        float[] floats = null;
        switch (flag) {
            case 0: // 水平反转
                floats = new float[] { -1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f };
                break;
            case 1: // 垂直反转
                floats = new float[] { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
                break;
        }

        if (floats != null) {
            Matrix matrix = new Matrix();
            matrix.setValues(floats);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        }

        return null;
    }

    /**
     * 图片旋转
     *
     * @param bmp
     *            要旋转的图片
     * @param degree
     *            图片旋转的角度，负值为逆时针旋转，正值为顺时针旋转
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bmp, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    /**
     * 图片缩放
     *
     * @param bm
     * @param scale
     *            值小于则为缩小，否则为放大
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap bm, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }

    /**
     * 图片缩放
     *
     * @param bm
     * @param w
     *            缩小或放大成的宽
     * @param h
     *            缩小或放大成的高
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap bm, int w, int h) {
        Bitmap BitmapOrg = bm;

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();

        float scaleWidth = ((float) w) / width;
        float scaleHeight = ((float) h) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
    }

}
