package cn.xxt.file.ui.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.ui.base.FileBaseFragment;
import cn.xxt.file.util.FileUtil;

/**
 * Created by zyj on 2017/10/31.
 */

public class FileOpenUnVailableFragment extends FileBaseFragment {

    @BindView(R2.id.iftv_file_icon)
    IconFontTextView iftvFileIcon;

    @BindView(R2.id.tv_file_name)
    TextView tvFileName;

    @Inject
    @ActivityContext
    Context context;

    private FileInfo fileInfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        injectThis();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_file_open_unvailable_file;
    }

    @Override
    public void initView() {
        initViews();
    }

    /**
     * 开发给openActivity调用。传进来：下载管理器，文件实体
     * @param fileInfo
     */
    public void initData(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    private void initViews() {
        //图标
        int iconCode = FileUtil.getFileIconCodeByFileType(context, fileInfo.fileType);
        int iconColor = FileUtil.getFileIconColorByFileType(context, fileInfo.fileType);
        iftvFileIcon.setText(iconCode);
        iftvFileIcon.setTextColor(ContextCompat.getColor(context, iconColor));

        //文件名称
        String fileName = fileInfo.fileName;
        tvFileName.setText(fileName);


    }

    private void injectThis(){
        Activity activity = getActivity();
        if (activity instanceof FileOpenActivity) {
            ((FileOpenActivity) activity).getActivityComponent().inject(this);
        }
    }
}
