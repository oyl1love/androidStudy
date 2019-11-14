package com.style.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class BitmapUtil {
    public final static String TAG = "BitmapCache";
    private static HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
    private static LruCache<String, Bitmap> mMemoryCache;
    public static int cacheSize;

    static void initConfig() {
        // 获取虚拟机可用内存（内存占用超过该值的时候，将报OOM异常导致程序崩溃）。最后除以1024是为了以kb为单位
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.e(TAG, "maxMemory-----" + String.valueOf(maxMemory / 1024) + "Mib");
        // 使用可用内存的1/8来作为Memory Cache
        cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 重写sizeOf()方法，使用Bitmap占用内存的kb数作为LruCache的size
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static void getMemoryCacheSize(String path, Bitmap bmp) {
        if (!TextUtils.isEmpty(path) && bmp != null) {
            imageCache.put(path, new SoftReference<Bitmap>(bmp));
        }
    }

    public static void put(String path, Bitmap bmp) {
        if (!TextUtils.isEmpty(path) && bmp != null) {
            imageCache.put(path, new SoftReference<Bitmap>(bmp));
        }
    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public static void displayBmp(final Activity act, final ImageView imageView, final String path, final int reqWidth, final int reqHeight) {
        if (null != imageView) {
            if (imageCache.containsKey(path)) {
                SoftReference<Bitmap> reference = imageCache.get(path);
                Bitmap bmp = reference.get();
                if (bmp != null) {
                    imageView.setImageBitmap(bmp);
                    return;
                }
            }
            Bitmap cachebitmap = getBitmapFromMemCache(path);
            if (cachebitmap != null) {
                imageView.setImageBitmap(cachebitmap);
                return;
            }
            new Thread() {
                Bitmap thumb;

                public void run() {
                    try {
                        thumb = BitmapUtil.getThumbnail(path, reqHeight, reqHeight);
                    } catch (Exception e) {
                    }
                    if (null != thumb) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(thumb);
                                put(path, thumb);
                                addBitmapToMemoryCache(path, thumb);
                            }
                        });
                    }
                }
            }.start();
        }
    }

    public static Bitmap getThumbnail(String path, int vWidth, int vHeight) {
        try {
            FileInputStream in = new FileInputStream(new File(path));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            Bitmap bitmap = null;
            in = new FileInputStream(new File(path));
            // 计算 inSampleSize 的值，解析器使用的inSampleSize都是2的指数倍，如果inSampleSize是其他值，则找一个离这个值最近的2的指数值。
            options.inSampleSize = calculateInSampleSize(options, vWidth, vHeight);
            /*
             * ALPHA_8：只有一个alpha通道每个像素占用1byte内存
             * ARGB_4444：每个像素占用2byte内存，已经被官方嫌弃
             * ARGB_8888：每个像素ARGB四个通道，每个通道8bit，占用4byte内存
             * （默认） RGB_565：每个像素占2Byte，其中红色占5bit，绿色占6bit，蓝色占5bit
             */
            options.inPreferredConfig = null;    // 让解码器基于屏幕以最佳方式解码
            options.inJustDecodeBounds = false;  // 是否只读取边界
            options.inDither = false;            // 不进行图片抖动处理
            /* 下面两个字段需要组合使用 */
            options.inPurgeable = true;
            options.inInputShareable = true;
            bitmap = BitmapFactory.decodeStream(in, null, options);
            Bitmap newBmp = scaleCrop(bitmap, vWidth, vHeight);
            return newBmp;
        } catch (IOException e) {
            return null;
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 原始图片的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // 在保证解析出的bitmap宽高分别大于目标尺寸宽高的前提下，取可能的inSampleSize的最大值
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 根据显示图片比例裁剪源bitmap
     *
     * @param source
     * @param outWidth
     * @param outHeight
     * @return
     */
    public static Bitmap scaleCrop(Bitmap source, int outWidth, int outHeight) {
        if (source == null)
            return null;
        //裁剪bitmap使其宽高比与imageview保持一致
        float scale;
        int x = 0, y = 0;
        int dx = source.getWidth(), dy = source.getHeight();
        //源bitmap宽高比大于imageview的宽高比,以imageview的宽高比裁剪bitmap宽度中心dx区域，反之裁剪高度中心dy区域。
        if ((float) source.getWidth() / source.getHeight() > (float) outWidth / outHeight) {
            scale = (float) outWidth / (float) outHeight;
            dx = (int) (source.getHeight() * scale);
            x = (source.getWidth() - dx) / 2;
        } else if ((float) source.getWidth() / source.getHeight() < (float) outWidth / outHeight) {
            scale = (float) outHeight / (float) outWidth;
            dy = (int) (source.getWidth() * scale);
            y = (source.getHeight() - dy) / 2;
        }
        Bitmap sourceNew = Bitmap.createBitmap(source, x, y, dx, dy);
        return sourceNew;
    }

    /**
     * 旋转图片一定角度
     *
     * @return Bitmap
     * @throws
     */
    public static Bitmap rotateImageView(Bitmap bitmap, int angle) {
        if (bitmap != null && angle != 0) {
            // 旋转图片 动作
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            // 创建新的图片
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    /**
     * @param path    --文件路径
     * @param bitmap
     * @param quality 图片质量：30 表示压缩70%; 100表示压缩率为0
     * @return void
     * @throws
     */
    public static void saveBitmap(String path, Bitmap bitmap, int quality) throws IOException {
        FileOutputStream out;
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        out = new FileOutputStream(f);//JPEG:以什么格式压缩
        if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
            out.flush();
        }
        out.close();
        recycle(bitmap);
    }

    public static void recycle(Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public static byte[] bitmap2byte(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        return baos.toByteArray();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static InputStream bitmapToInputStream(Bitmap bitmap) {
        int size = bitmap.getHeight() * bitmap.getRowBytes();
        ByteBuffer buffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(buffer);
        return new ByteArrayInputStream(buffer.array());
    }

    /**
     * 因为png图片是无损的，不能进行压缩。
     * @param b
     * @param maxSize 单位kb
     * @return
     */
    public static Bitmap compressImage(Bitmap b, int maxSize) {
        if (b != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            Log.e("compressImage", "压缩前大小  " + baos.toByteArray().length / 1024 + " kb");
            Bitmap mSrcBitmap = b;
            if (baos.toByteArray().length > 1024 * 100) {
                mSrcBitmap = Bitmap.createScaledBitmap(b, b.getWidth() / 2, b.getHeight() / 2, true);
            }
            int quality = 95;//必须大于0
            while (baos.toByteArray().length > 1024 * maxSize && quality > 15) {  //循环判断如果压缩后图片是否大于2048kb,大于继续压缩
                baos.reset();//重置baos即清空baos
                mSrcBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);//这里压缩options%，把压缩后的数据存放到baos中
                Log.e("compressImage", "压缩后大小  ByteArray  " + baos.toByteArray().length / 1024 + " kb" + " size " + baos.size());
                quality -= 10;
            }
            //二次压缩：当quality=10时依然超过maxsize
            if (baos.toByteArray().length > 1024 * maxSize) {
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
                Bitmap imageNew = BitmapFactory.decodeStream(bais, null, null);
                while (baos.toByteArray().length > 1024 * maxSize && quality > 0) {
                    baos.reset();
                    imageNew.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                    Log.e("compressImage", "二次压缩后大小  ByteArray  " + baos.toByteArray().length / 1024 + " kb" + " size " + baos.size());
                    quality -= 1;
                }
            }

            ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
            Bitmap r = BitmapFactory.decodeStream(isBm, null, null);
            return r;
        }
        return null;
    }

    /**
     * 将图片变为圆角
     *
     * @param bitmap 原Bitmap图片
     * @param corner 图片圆角的弧度(单位:像素(px))
     * @return 带有圆角的图片(Bitmap 类型)
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, int corner) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect dst = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(src);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, corner, corner, paint);
        //SRC_IN:后画的
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    /**
     * 将图片转化为圆形头像
     *
     * @throws
     * @Title: toRoundBitmap
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;

            left = 0;
            top = 0;
            right = width;
            bottom = width;

            height = width;

            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;

            float clip = (width - height) / 2;

            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;

            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);// 设置画笔无锯齿

        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas

        // 以下有两种方法画圆,drawRounRect和drawCircle
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
        // canvas.drawCircle(roundPx, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
        canvas.drawBitmap(bitmap, src, dst, paint); // 以Mode.SRC_IN模式合并bitmap和已经draw了的Circle
        return output;
    }
}
