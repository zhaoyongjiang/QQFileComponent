package cn.xxt.file.ui.manager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.xxt.file.R;
import cn.xxt.file.ui.base.FileBaseFragment;

/**
 * Created by zyj on 2017/11/2.
 */

public class FileOpenFragment extends FileBaseFragment {
    @Override
    public int getLayoutResource() {
        return R.layout.fragment_file_open_file;
    }

    @Override
    public void initView() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
