package cn.xxt.file.ui.fileFragment;

import java.util.List;

import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.library.ui.base.MvpView;

/**
 * Created by zyj on 2017/8/25.
 */

public interface FragmentMvpView extends MvpView {
    void onSuccessGetFileInfo(List<FileInfo> fileInfoList);
}
