package com.example.zyj.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.zyj.R;
import com.example.zyj.ui.base.AppBaseActivity;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.util.DateUtil;
import cn.xxt.commons.util.RxBusWithTag;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.file.R2;
import cn.xxt.file.api.FileComponent;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.file.ui.view.FileCornerImageView;
import cn.xxt.file.util.FileUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppBaseActivity {

    @Inject
    @ActivityContext
    Context context;

    @BindView(R2.id.ri_image)
    FileCornerImageView fileIconIv;

    @BindView(R2.id.tv_file_name)
    TextView tvFileName;

    @BindView(R2.id.tv_size)
    TextView tvFileSize;

    @BindView(R2.id.tv_time)
    TextView tvFileTime;

    private FileInfo currentFileInfo = new FileInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActivityComponent().inject(this);
        ButterKnife.bind(this);
    }

    public void toSelectFile(View view) {
        FileComponent.selectFile(context, 0, 100);

        registSelectFileRxBus();
    }

    public void toManageFile(View view) {
        FileComponent.manageFile(context, 0);
    }

    public void openFile(View view) {
        FileComponent.openFile(context, 0 ,currentFileInfo);
    }

    private void registSelectFileRxBus() {
        Observable rxBusObservable = RxBusWithTag.getInstance().registerOneshot("selectFileCompleted");

        rxBusObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List list) {
                        try {
                            if (null != list) {
                                FileInfo fileInfo = (FileInfo) list.get(0);
                                currentFileInfo = fileInfo;
                                setData(fileInfo);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    private void setData(FileInfo fileInfo) {
        if (fileInfo.fileType == FileTypeEnum.TYPE_IMAGE.getFileType()) {
            if (fileInfo.getFileServerPath() != null && fileInfo.getFileServerPath().length() > 0) {
                Picasso.get()
                        .load(fileInfo.getFileServerPath())
                        .placeholder(cn.xxt.file.R.drawable.bg_placeholder_loading_small)
                        .error(cn.xxt.file.R.drawable.bg_placeholder_load_fail_small)
                        .resize(60, 60)
                        .into(fileIconIv);
            } else {
                Picasso.get()
                        .load(StringUtil.connectStrings("file://",fileInfo.getFileLocalPath()))
                        .placeholder(cn.xxt.file.R.drawable.bg_placeholder_loading_small)
                        .error(cn.xxt.file.R.drawable.bg_placeholder_load_fail_small)
                        .resize(60, 60)
                        .into(fileIconIv);
            }
        } else {
            fileIconIv.setImageResource(FileUtil.getResourceIdByFileType(context, fileInfo.fileType));
        }

        tvFileName.setText(fileInfo.getFileName());

        tvFileSize.setText(FileUtil.FormetFileSize(fileInfo.getFileSize()));

        String formatDateStr1 = DateUtil.format(new Date(fileInfo.getUpdateDate()),
                DateUtil.DATE_FORMAT_STRING_YMDHMS);
        tvFileTime.setText(formatDateStr1);
    }
}
