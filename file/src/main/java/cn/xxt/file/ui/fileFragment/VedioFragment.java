package cn.xxt.file.ui.fileFragment;

import java.util.ArrayList;
import java.util.List;

import cn.xxt.file.R;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.ui.base.FileBaseFragment;

/**
 * Created by zyj on 2017/8/16.
 */

public class VedioFragment extends FileBaseFragment {
    @Override
    public int getLayoutResource() {
        return R.layout.fragment_vedio_file;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //相当于onresume
//            if (isDeleteThisFragmentFile && expandableItemAdapter != null) {
//                loadData();
//
//                setDeleteThisFragmentFile(false);
//            }
        } else {
            //相当于onPause
        }
    }

    @Override
    public void initView() {

    }

    @Override
    public void onSuccessGetFileInfo(List<FileInfo> fileInfoList) {

    }

    @Override
    public void onFileLongPressToDelete(FileInfo fileInfo) {
        List<FileInfo> fileInfoList = new ArrayList<>();
        fileInfoList.add(fileInfo);
//        notifyToRefreshUi(fileInfoList);
    }
}
