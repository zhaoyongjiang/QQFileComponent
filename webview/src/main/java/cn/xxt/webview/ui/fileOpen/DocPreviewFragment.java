package cn.xxt.webview.ui.fileOpen;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

import cn.xxt.commons.util.FileUtil;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.commons.util.ToastUtil;
import cn.xxt.library.ui.base.BaseFragment;
import cn.xxt.webview.R;

/**
 * Created by zyj on 2018/8/4.
 */
public class DocPreviewFragment extends BaseFragment implements TbsReaderView.ReaderCallback{

    private TbsReaderView tbsReaderView;

    private String filePath = "";

    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doc_reader, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            loadFile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tbsReaderView.onStop();
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }

    public void initData(String filePath) {
        this.filePath = filePath;
    }

    public void refreshFileDisplay(String filePath) {
        this.filePath = filePath;

        loadFile();
    }

    public static boolean isNetWorkFile(String filePath) {
        boolean flag = false;

        if (!StringUtil.isEmpty(filePath)
                && (filePath.startsWith("http")
                || filePath.startsWith("//"))) {
            flag = true;
        }

        return flag;
    }

    private void initViews(View view) {
        tbsReaderView = new TbsReaderView(context, this);
        RelativeLayout rlRoot = (RelativeLayout) view.findViewById(R.id.rl_tbs);
        rlRoot.addView(tbsReaderView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void loadFile() {
        if (DocPreviewFragment.isNetWorkFile(filePath)) {
            displayFileWithUrl();
        } else {
            displayFile();
        }
    }

    private void displayFile() {
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());
        String fileName = FileUtil.getFileNameFromUrl(filePath);
        boolean result = tbsReaderView.preOpen(parseFormat(fileName), false);
        if (result) {
            tbsReaderView.openFile(bundle);
        }
    }

    private void displayFileWithUrl() {
//        QbSdk.startQbOrMiniQBToLoadUrl(context, filePath, null, new ValueCallback<String>() {
//            @Override
//            public void onReceiveValue(String s) {
//                ToastUtil.displayToastShort(context, s);
//            }
//        });

        ToastUtil.displayToastLong(context, "暂不支持打开网络文件");
    }

    private String parsePath2Uri(String path) {
        String uri = "";

        File file = new File(path);
        uri = file.toURI() + "";

        return uri;
    }

    private String parseFormat(String fileName) {
        String str = fileName.substring(fileName.lastIndexOf(".") + 1);
        return str;
    }
}
