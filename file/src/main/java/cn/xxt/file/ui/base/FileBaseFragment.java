package cn.xxt.file.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.ui.fileFragment.ExpandableItemAdapter;
import cn.xxt.file.ui.fileFragment.FragmentMvpView;
import cn.xxt.file.ui.fileFragment.FragmentPresenter;
import cn.xxt.file.ui.manager.FileLocalMainActivity;
import cn.xxt.file.ui.manager.FileRecentMainActivity;
import cn.xxt.file.ui.selector.FileSelectorMainActivity;
import cn.xxt.library.ui.base.BaseFragment;

/**
 * Created by zyj on 2017/8/15.
 */

public abstract class FileBaseFragment extends BaseFragment implements FragmentMvpView, ExpandableItemAdapter.OnExpandableItemAdapterListener{
    //activity -》 adapter 操作事件类型字段
    public static final int FLAG_OPER_TYPE_DELETE = 1;
    public static final int FLAG_OPER_TYPE_DOWNLOAD = 2;
    public static final int FLAG_OPER_TYPE_SHARE = 3;
    public static final int FLAG_OPER_TYPE_DEFAULT = 4;

    public boolean expanFirstFolder = false;

    @Inject
    public FragmentPresenter fragmentPresenter;

    protected View rootView;

    public static int FLAG_DATA_LOCAL = 1;

    public static int FLAG_DATA_RECENT = 2;

    /**
     * 数据来源标识：本机，最近
     * @param dataFlag
     */
    public void setDataFlag(int dataFlag) {
        this.dataFlag = dataFlag;
    }

    public int dataFlag;

    /**
     * 控件是否初始化完成
     */
    private boolean isViewCreated;
    /**
     * 数据是否已加载完毕
     */
    private boolean isLoadDataCompleted;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (fragmentPresenter != null) {
            fragmentPresenter.attachView(this);
        }

        try {
            if (null == rootView) {
                rootView = inflater.inflate(getLayoutResource(), container, false);
                ButterKnife.bind(this, rootView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isViewCreated = true;
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isViewCreated && !isLoadDataCompleted) {
            isLoadDataCompleted = true;
            loadData();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getUserVisibleHint()) {
            isLoadDataCompleted = true;
            loadData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (null != rootView) {
            ((ViewGroup)rootView.getParent()).removeView(rootView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSuccessGetFileInfo(List<FileInfo> fileInfoList) {

    }

    @Override
    public void onPhotoItemClickedToPreview(FileInfo fileInfo) {}

    @Override
    public void onFileItemClickedToOpen(FileInfo fileInfo) {}

    @Override
    public void onFileLongPressToDelete(FileInfo fileInfo) {}

    //获取布局文件id
    public abstract int getLayoutResource();

    //初始化界面
    public abstract void initView();

    private void loadData(){
        initView();
    }

    public List<FileInfo> getActivitySelectedFileInfoList() {
        List<FileInfo> selectedFileInfoList = new ArrayList<>();

        Activity activity = getActivity();
        if (activity instanceof FileSelectorMainActivity) {
            selectedFileInfoList =  ((FileSelectorMainActivity) activity).selectedFileInfoList;
        } else if (activity instanceof FileRecentMainActivity) {
            selectedFileInfoList = ((FileRecentMainActivity) activity).selectedFileInfoList;
        } else if (activity instanceof FileLocalMainActivity) {
            selectedFileInfoList = ((FileLocalMainActivity) activity).selectedFileInfoList;
        }

        return selectedFileInfoList;
    }

    public int getWebId() {
        int webId = 0;

        Activity activity = getActivity();
        if (activity instanceof FileSelectorMainActivity) {
            webId =  ((FileSelectorMainActivity) activity).webId;
        } else if (activity instanceof FileRecentMainActivity) {
            webId = ((FileRecentMainActivity) activity).webId;
        } else if (activity instanceof FileLocalMainActivity) {
            webId = ((FileLocalMainActivity) activity).webId;
        }

        return webId;
    }
}
