package cn.xxt.commons.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.xxt.commons.R;
import cn.xxt.commons.util.download.MultiThreadDownload;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;


/**
 * 图片处理工具类
 *
 * @author wang yuliang
 *
 */
public class ImageUtil {
    /** 拍照结果码 */
    public final static int CAMERA_REQUEST_CODE = 0;
    /** 选择图片结果码 */
    public final static int GALLERY_REQUEST_CODE = 1;

    private static final int FULL_QUALITY_FOR_COMPRESSING = 100;

    public static Observable<Bitmap> getBitmapWithImageUrl(final Context context, final String imgUrl) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap bitmap = null;
                try {
                    byte[] data = getImage(context, imgUrl);
                    int length = data.length;
                    bitmap = BitmapFactory.decodeByteArray(data, 0, length);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onNext(bitmap);
            }
        });
    }

    public static byte[] getImage(Context context, String imgUrl) throws Exception {
        OkHttpClient client = RemoteServiceUtil.getOkHttpClientNoInterceptor(context,null,null,null);
        Request request=new Request.Builder()
                .url(imgUrl)
                .build();

        Response response = client.newCall(request).execute();
        if(response!=null&&response.isSuccessful()) {
            byte[] data = response.body().bytes();
            response.body().close();
            return data;
        }
        return null;
    }

    public static byte[] readStream(InputStream in) throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while((len = in.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.close();
        in.close();
        return outputStream.toByteArray();
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    /**
     * 根据图片uri获取图片存储路径
     *
     * @param context
     *            上下文环境
     * @param uri
     *            图片uri
     * @return 图片存储路径
     */
    public static String getPathByUri(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        try {
            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                } else if (isDownloadsDocument(uri)) {  // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                } else if (isMediaDocument(uri)) {   // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] {
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {   // MediaStore (and general)
                // Return the remote address
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();

                return getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {  // File
                return uri.getPath();
            }
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 获取将图片进行质量压缩时的压缩率
     *
     * @param bitmap
     *            图片位图
     * @param k
     *            压缩后图片所占存储空间最大值，以K为单位
     * @return 压缩后的位图
     */
    public static int getQualityByCompressingImage(Bitmap bitmap, int k) {
        int option = 70;
        if (bitmap != null) {
            try {
                int optionTry = FULL_QUALITY_FOR_COMPRESSING;

                do {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    bitmap.compress(CompressFormat.JPEG, optionTry, os);

                    if (os.toByteArray().length / 1024 <= k) {
                        break;
                    }
                    os.flush();
                    os.close();
                    optionTry -= 10;
                } while (optionTry > 20);

                option = optionTry;
            } catch (Exception e) {

            }
        }
        return option;
    }

    /**
     * 获取将图片按照高宽等比例压缩后的位图
     *
     * @param path
     *            图片在手机存储上的实际路径
     * @param width
     *            压缩后的宽
     * @param height
     *            压缩后的高
     * @return 压缩后的位图
     */
    public static Bitmap getBitmapByResizingImage(String path, float width, float height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        int wr = (int)Math.ceil(options.outWidth/width);
        int hr = (int)Math.ceil(options.outHeight/height);

        if (wr > 1 || hr > 1) {
            if (wr > hr) {
                options.inSampleSize = wr;
            } else {
                options.inSampleSize = hr;
            }
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static synchronized Bitmap getBitmapByResizingImageSyn(String path, float width, float height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        int wr = (int)Math.ceil(options.outWidth/width);
        int hr = (int)Math.ceil(options.outHeight/height);

        if (wr > 1 || hr > 1) {
            if (wr > hr) {
                options.inSampleSize = wr;
            } else {
                options.inSampleSize = hr;
            }
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     *  根据resId获取指定宽高的drawable
     * @param context
     * @param resId
     * @param width
     * @param height
     * @return
     */
    public static Drawable getResizeDrawable(Context context,int resId, int width, int height) {
        return getResizeDrawable(context,ContextCompat.getDrawable(context,resId),width,height);
    }

    /**
     * 根据drawable获取指定宽高的drawable
     * @param context
     * @param drawable
     * @param width
     * @param height
     * @return
     */
    public static Drawable getResizeDrawable(Context context,Drawable drawable, int width, int height) {
        Bitmap bitmap = drawable2Bitmap(drawable);
        bitmap = resizeBitmap(bitmap, width, height);
        return bitmap2Drawable(context,bitmap);
    }

    /**
     * bitmap指定宽高
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    /**
     * Drawable转Bitmap
     * @param drawable
     * @return
     */
    public static Bitmap drawable2Bitmap(Drawable drawable){
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ?Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Bitmap转Drawable
     * @param context
     * @param bitmap
     * @return
     */
    public static Drawable bitmap2Drawable(Context context, Bitmap bitmap){
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    /**
     * 将图片位图保存为jpg文件
     * @param fileOriginalPath 源文件路径
     * @return
     */
    public static  File getFileFromBitmap(String fileOriginalPath) {
        File imageFile = null;

        try {
            imageFile = getFileFromBitmap(fileOriginalPath, null);
        } catch (Exception e) {
            imageFile = null;
        }

        return imageFile;
    }

    public static  File getFileFromBitmap(String fileOriginalPath, String prefix) {
        File imageFile = null;

        try {
            imageFile = getFileFromBitmap(fileOriginalPath, null, prefix);
        } catch (Exception e) {
            imageFile = null;
        }

        return imageFile;
    }

    public static  File getFileFromBitmap(String fileOriginalPath, String saveDir, String prefix) {
        File imageFile = null;

        if (!StringUtil.isEmpty(fileOriginalPath)) {
            try {
                Bitmap bitmap = ImageUtil.getBitmapByResizingImage(fileOriginalPath, 1024, 1024);

                if (bitmap != null) {
                    String filePath = StringUtil.isEmpty(saveDir) ? (Environment.getExternalStorageDirectory() + "/QQFileComponent/tmp/") : saveDir;
                    File file = new File(filePath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }

                    String filePreStr = StringUtil.isEmpty(prefix) ? "zar" : prefix;
                    imageFile = new File(filePath, filePreStr + String.valueOf(System.currentTimeMillis())
                            + ".jpg");

                    // 取得图片旋转角度
                    int angle = PhotoUtil.readPictureDegree(fileOriginalPath);

                    // 修复图片被旋转的角度
                    Bitmap bm = PhotoUtil.rotaingImageView(angle, bitmap);

                    int quality = ImageUtil.getQualityByCompressingImage(bm, 100);

                    FileOutputStream os = new FileOutputStream(imageFile);
                    if (bm.compress(CompressFormat.JPEG, quality, os)) {
                        os.flush();
                        os.close();
                    }
                }
            } catch (Exception e) {
                imageFile = null;
            }
        }

        return imageFile;
    }

    public static String getHeaderFileDirPath()
    {
        String directoryPath = Environment.getExternalStorageDirectory() + "/.QQFileComponent/avatar/";
        File directoryFile = new File(directoryPath);
        if (!directoryFile.exists()) {
            directoryFile.mkdirs();
        }

        return directoryPath;
    }

    public static String getHeaderFilePath(String webId) {
        return getHeaderFileDirPath() + String.valueOf(webId);
    }

    public static void downLoadImage(final Context context,
                                     final String url,
                                     final String dir,
                                     final String fileName) {
        if (StringUtil.isEmpty(url)) {
            return;
        }

        try {
            Observable.just(1)
                    .observeOn(Schedulers.io())
                    .subscribe(new Subscriber<Integer>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Integer integer) {
                            new MultiThreadDownload(context, url,dir,fileName,null).start();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean setHeaderImageWithWebId(Context context,ImageView iv,String webId) {
        boolean hasFileflag = false;

        try {
            String pathString = getHeaderFilePath(webId);
            File file = new File(pathString);

            if (file.exists() && file.length() > 0) {
                Picasso.get().load(file)
                        .fit()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person).into(iv);
                hasFileflag = true;
            } else {
                iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_person));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasFileflag;
    }

    /**
     * 弹窗：通过拍照或者图片获取照片
     * @param activity              弹窗所在的Activity
     * @param imageFileDirectory    拍照后图片存储的路径
     * @param imageFileName         拍照后图片存储的名字
     */
    public static void getPhotoPopup(final Activity activity, final String imageFileDirectory,
                                     final String imageFileName) {
        /************ 弹出选择框 *************/
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("上传方式");
            builder.setSingleChoiceItems(new String[]{"拍照", "从相册中选择"}, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        File directoryFile = new File(imageFileDirectory);
                        if (!directoryFile.exists()) {
                            directoryFile.mkdirs();
                        }
                        File imageFile = new File(imageFileDirectory + File.separator + imageFileName);
                        Uri imageFileUri = Uri.fromFile(imageFile);
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
                        activity.startActivityForResult(intent, ImageUtil.CAMERA_REQUEST_CODE);

                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        activity.startActivityForResult(intent, ImageUtil.GALLERY_REQUEST_CODE);
                    }
                    dialog.dismiss();
                }
            });
            builder.setCancelable(true);
            builder.create().show();
        } catch (Exception e) {}
    }

    /**
     * 保存图片到校讯通相册
     * @param context
     * @param bitmap
     * @param
     * @return
     */
    public static boolean saveImage(Context context,Bitmap bitmap) {
        boolean successFlag = false;
        try {
            FileOutputStream b = null;
            String pathStr = getCurrentDateImagePath();
            try {
                b = new FileOutputStream(pathStr);
                if(bitmap!=null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+ pathStr)));
                    successFlag = true;
                    bitmap.recycle();
                }
            } catch (Exception e) {

            } finally {
                try {
                    if (b!=null) {
                        b.flush();
                        b.close();
                    }
                } catch (IOException e) {
                }
            }
        } catch (Exception e) {
        }
        return successFlag;
    }

    /**
     * 获取校讯通相册路径
     * @return
     */
    public static String getXxtAlbumPath() {
        String path = Environment.getExternalStorageDirectory()
                + "/QQFileComponent/photo/";

        File file = new File(path);
        // 判断文件目录是否存在
        if (!file.exists()) {
            file.mkdirs();
        }

        return path;
    }

    /**
     * 获取以当前时间命名的图片名称
     *
     * @return
     */
    public static String getCurrentDateImageName() {
        return DateUtil.getDayString("yyyyMMddHHmmss", 0) + ".jpg";
    }

    /**
     * 获取以当前时间命名的图片路径
     * @return
     */
    public static String getCurrentDateImagePath() {
        return getXxtAlbumPath() + getCurrentDateImageName();
    }
}
