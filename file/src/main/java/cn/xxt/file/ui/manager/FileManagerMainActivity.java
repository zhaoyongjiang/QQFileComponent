package cn.xxt.file.ui.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.jakewharton.rxbinding.view.RxView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.util.BundleUtil;
import cn.xxt.commons.widget.AlwaysMarqueeTextView;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.api.FileComponent;
import cn.xxt.file.internal.domain.FileManagerMainMultipleItem;
import cn.xxt.file.ui.base.FileBaseActivity;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

import static cn.xxt.file.api.FileComponentCommonValue.KEY_WEBID;

/**
 * Created by zyj on 2017/8/15.
 */

public class FileManagerMainActivity extends FileBaseActivity{
    private FileManagerMainAdapter fileManagerMainAdapter;
    private List<FileManagerMainMultipleItem> multipleItemArrayList = new ArrayList<>();

    @Inject
    @ActivityContext
    Context context;

    @BindView(R2.id.rlv_type)
    RecyclerView recyclerView;

    @BindView(R2.id.tv_top_title)
    AlwaysMarqueeTextView tvTopTitle;

    @BindView(R2.id.iftv_top_left)
    IconFontTextView iftvBack;

    private int maxNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager_main_file);
        getActivityComponent().inject(this);

        ButterKnife.bind(this);
        initData();

        initViews();

        registRxViewsClickEvent();
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        maxNum = FileComponent.builder(context).getMaxNum();
        webId = BundleUtil.getIntegerWithKey(bundle, KEY_WEBID);

        multipleItemArrayList.add(new FileManagerMainMultipleItem("最近文件"));
        multipleItemArrayList.add(new FileManagerMainMultipleItem("本机文件"));
    }

    private void initViews() {
        fileManagerMainAdapter = new FileManagerMainAdapter(multipleItemArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fileManagerMainAdapter);

        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (position == 0) {
                    //最近
                    Intent intent = new Intent();
                    intent.setClass(FileManagerMainActivity.this, FileRecentMainActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_WEBID, webId);
//                    bundle.putInt(KEY_FILE_SELECT_MAX_COUNT, maxNum);

                    intent.putExtras(bundle);

                    startActivity(intent);
                } else {
                    //本机
                    Intent intent = new Intent();
                    intent.setClass(FileManagerMainActivity.this, FileLocalMainActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_WEBID, webId);

                    intent.putExtras(bundle);

                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void registRxViewsClickEvent() {
        //取消
        RxView.clicks(iftvBack)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Void>bindToLifecycle())
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
                        finish();
                    }
                });
    }
}
