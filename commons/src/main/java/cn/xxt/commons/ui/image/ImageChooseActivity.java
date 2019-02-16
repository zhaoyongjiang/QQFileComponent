package cn.xxt.commons.ui.image;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.xxt.commons.R;
import cn.xxt.commons.ui.base.TranslucentActivity;
import cn.xxt.commons.util.ImageUtil;
import cn.xxt.commons.util.RxBusWithTag;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.commons.util.ToastUtil;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.commons.widget.XXTDialog;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static cn.xxt.commons.ui.image.ImageShowActivity.BUNDLE_IMAGE_LIST;

/**
 * 图片选择控件
 *
 * 使用说明：
 * 通过startForResult启动本Acitivty
 * 再自己继承onActivityResult接收选择图片列表，key为BUNDLE_SELECTED_IMAGE_LIST，值为ArrayList<String>
 *
 * Created by Luke on 16/6/21.
 */
public class ImageChooseActivity extends TranslucentActivity implements ImageChooseAdapter.OnImageChooseListener{

    public final static String BUNDLE_SELECTED_IMAGE_LIST = "BUNDLE_SELECTED_IMAGE_LIST";

    public final static String RXBUS_SELECTED_IMAGE_LIST = "RXBUS_SELECTED_IMAGE_LIST";

    private final static int REQUEST_CODE_SHOW_IMAGE = 1;

    private final static int REQUEST_CODE_TAKE_PHOTO = 2;

    /** 选取图片的最大张数 */
    public final static String BUNDLE_SELECT_PICTURE_MAX_NUM = "BUNDLE_SELECT_PICTURE_MAX_NUM";

    /** 可以选择图片的最大张数，默认值1（调用者不传时按照1张处理）*/
    private int selectPictureMaxNum = 1;

    /** 相册列表是否显示复选框。为了班级圈使用。默认显示，班级圈使用时候入参将标记置为不显示 */
    public boolean photoOptional = true;

    /** 图片list */
    public ArrayList<String> imageList = new ArrayList<>();

    /** 已选择图片list */
    private ArrayList<String> selectedImageList = new ArrayList<>();

    private ImageChooseAdapter adapter;

    private IconFontTextView iftvTopLeft;

    private IconFontTextView iftvTopRight;

    private TextView tvTopTitle;

    private RecyclerView rvImages;

    private Context context;

    private String imageTakePath;


    /** 拍照权限 */
    public static final int PERMISSION_CODE_TAKE_PHOTO = 100;

    /** 额外存储空间权限 */
    public static final int PERMISSION_CODE_EXTERNAL_STORAGE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_choose);

        context = this;

        initData();

        initViews();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null && bundle.containsKey(BUNDLE_SELECTED_IMAGE_LIST)
                && bundle.getStringArrayList(BUNDLE_SELECTED_IMAGE_LIST) != null) {
            selectedImageList.addAll(getIntent().getStringArrayListExtra(BUNDLE_SELECTED_IMAGE_LIST));
        }

        checkPermissionAndBuildImageList();
    }

    @Override
    public void onImageChooseImageClicked(int position) {
        Bundle bundle =  new Bundle();
        bundle.putStringArrayList(BUNDLE_IMAGE_LIST,imageList);
        bundle.putInt(ImageShowActivity.BUNDLE_SELECTED_INDEX,position);
        bundle.putStringArrayList(ImageShowActivity.BUNDLE_SELECTED_LIST,selectedImageList);
        bundle.putInt(BUNDLE_SELECT_PICTURE_MAX_NUM, selectPictureMaxNum);

        Intent intent = new Intent(ImageChooseActivity.this,ImageShowActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent,REQUEST_CODE_SHOW_IMAGE);
    }

    @Override
    public void onImageChooseTagClicked(int position) {
        if(imageList!=null && position>=0 && position<imageList.size()) {
            String url = imageList.get(position);
            if (selectedImageList != null && selectedImageList.contains(url)) {
                selectedImageList.remove(url);
            } else if (selectedImageList!=null) {
                //判断已经选择图片的张数是否达到最大张数，达到，则提示不能再选取，否则继续选
                if (selectedImageList.size() >= selectPictureMaxNum) {
                    ToastUtil.displayToastShort(context,
                            StringUtil.connectStrings("选择的图片已经达到上限",
                                    String.valueOf(selectPictureMaxNum), "张"));
                } else {
                    selectedImageList.add(url);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onImageTakeClicked() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            //检查摄像头权限
            requestCameraPermission();
        } else {
            ToastUtil.displayToastLong(context,getString(R.string.sdcard_disable));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SHOW_IMAGE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle.containsKey(ImageShowActivity.BUNDLE_SELECTED_LIST)) {
                selectedImageList.clear();
                selectedImageList.addAll(bundle.getStringArrayList(ImageShowActivity.BUNDLE_SELECTED_LIST));
            }

            adapter.notifyDataSetChanged();

            if (bundle.containsKey(ImageShowActivity.BUNDLE_RESULT_IS_CONFIRM)
                    && bundle.getBoolean(ImageShowActivity.BUNDLE_RESULT_IS_CONFIRM)) {
                prepareResultAndFinish();
            }
        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://"+ imageTakePath)));
            imageList.add(imageTakePath);
            selectedImageList.add(imageTakePath);
            adapter.notifyDataSetChanged();

            Bundle bundle =  new Bundle();
            bundle.putStringArrayList(BUNDLE_IMAGE_LIST,selectedImageList);
            bundle.putInt(ImageShowActivity.BUNDLE_SELECTED_INDEX,selectedImageList.size()-1);
            bundle.putStringArrayList(ImageShowActivity.BUNDLE_SELECTED_LIST,selectedImageList);

            Intent intent = new Intent(ImageChooseActivity.this,ImageShowActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent,REQUEST_CODE_SHOW_IMAGE);
        }
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (null != bundle && bundle.containsKey(BUNDLE_SELECT_PICTURE_MAX_NUM)) {
            selectPictureMaxNum = bundle.getInt(BUNDLE_SELECT_PICTURE_MAX_NUM, 1);
            if (selectPictureMaxNum <= 0) {
                selectPictureMaxNum = 1;
            }
        }
    }
    /**
     * 初始化界面
     */
    private void initViews() {
        iftvTopLeft = (IconFontTextView)findViewById(R.id.iftv_top_left);
        iftvTopRight = (IconFontTextView)findViewById(R.id.iftv_top_right);
        tvTopTitle = (TextView)findViewById(R.id.tv_top_title);

        iftvTopLeft.setText(getString(R.string.iconfont_back));
        iftvTopLeft.setVisibility(View.VISIBLE);

        tvTopTitle.setText(getString(R.string.title_image_choose));

        iftvTopRight.setText(getString(R.string.confirm));
        iftvTopRight.setTextSize(16);
        iftvTopRight.setVisibility(View.VISIBLE);

        rvImages = (RecyclerView)findViewById(R.id.rv_images);

        RxView.clicks(iftvTopLeft)
                .compose(this.<Void>bindToLifecycle())
                .throttleFirst(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        Intent intent = new Intent();
                        setResult(RESULT_CANCELED,intent);
                        ImageChooseActivity.this.finish();
                    }
                });

        RxView.clicks(iftvTopRight)
                .compose(this.<Void>bindToLifecycle())
                .throttleFirst(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        prepareResultAndFinish();
                    }
                });

        rvImages.setLayoutManager(new GridLayoutManager(context,3));
        adapter = new ImageChooseAdapter(context,imageList,selectedImageList);
        adapter.setPhotoOptional(photoOptional);
        rvImages.setAdapter(adapter);
        adapter.setOnImageChooseListener(this);

    }

    private void prepareResultAndFinish() {
        Intent intent = new Intent();

        Bundle bundle = new Bundle();
        bundle.putStringArrayList(BUNDLE_SELECTED_IMAGE_LIST,selectedImageList);
        intent.putExtras(bundle);

        /**
         * 添加人: Luke
         * 添加日期: 2018/12/29 4:32 PM
         * 添加描述：添加rxbus方便非activity调用
         *
         */
        RxBusWithTag.getInstance().send(RXBUS_SELECTED_IMAGE_LIST, selectedImageList);


        setResult(RESULT_OK,intent);
        ImageChooseActivity.this.finish();
    }

    private void checkPermissionAndBuildImageList() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 没有权限。则去申请权限
            ActivityCompat.requestPermissions(ImageChooseActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE_EXTERNAL_STORAGE);
        } else {
            buildImageList();
        }
    }


    /**
     * 获取图片列表
     */
    private void buildImageList() {
        Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                List<String> tempImageList = new ArrayList<>();

                ContentResolver cr = ImageChooseActivity.this.getContentResolver();

                try {

                    // 构造相册索引
                    String[] columns = new String[]{Media.DATA};
                    // 得到一个游标
                    Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null,
                            Media.DATE_TAKEN + " DESC");

                    try {
                        int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
                        while (cur.moveToNext()) {
                            // 获取指定列的索引
                            // 图片本地绝对路径
                            String path = cur.getString(photoPathIndex);
                            File file = new File(path);
                            if (file.length() > 0) {
                                tempImageList.add(path);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        cur.close();
                    }
                } catch (Exception e) {

                }

                subscriber.onNext(tempImageList);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<List<String>>bindToLifecycle())
                .subscribe(new Subscriber<List<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtil.displayToastLong(context,
                                getString(R.string.build_image_list_failed));
                    }

                    @Override
                    public void onNext(List<String> list) {
                        imageList.clear();
                        imageList.addAll(list);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * 检查摄像头权限
     */
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // 没有权限。
            ActivityCompat.requestPermissions(ImageChooseActivity.this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE_TAKE_PHOTO);
        } else {
            //拍照
            imageTakePath = ImageUtil.getCurrentDateImagePath();
            File imageFile = new File(imageTakePath);
            String authority = context.getApplicationInfo().packageName + ".provider";
            Uri imageFileUri = FileProvider.getUriForFile(context, authority, imageFile);
            Intent intent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                    imageFileUri);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE_TAKE_PHOTO) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被用户同意
                //拍照
                imageTakePath = ImageUtil.getCurrentDateImagePath();
                File imageFile = new File(imageTakePath);
                Uri imageFileUri = Uri.fromFile(imageFile);
                Intent intent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                        imageFileUri);
                startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    displayFrameworkBugMessageAndExit("相机");
                }
            }
        } else if (requestCode == PERMISSION_CODE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被用户同意
                buildImageList();
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    displayFrameworkBugMessageAndExit("读写手机");
                }
            }
        }
    }

    /**
     * 权限提示框
     * @param funcName, 功能名
     */
    private void displayFrameworkBugMessageAndExit(final String funcName) {
        // camera error
        XXTDialog dialog = new XXTDialog(context);
        dialog.setDialogTitle("提示");
        String tips = String.format("在设置中开启", funcName, "权限", "以正常使用", funcName, "功能");
        dialog.setTip(tips);
        dialog.setBtnConfirm("去设置", View.VISIBLE);
        dialog.setBtnCancel("取消", View.VISIBLE);
        dialog.setOnXxtDialogBtnClickListener(new XXTDialog.OnXxtDialogBtnClickListener() {
            @Override
            public void onBtnConfirmClick() {
                Intent intent =  new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }

            @Override
            public void onBtnCancelClick() {
            }
        });
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        dialog.show();
    }

    @Override
    protected String getActivityName() {
        return "图片列表选择页面";
    }
}
