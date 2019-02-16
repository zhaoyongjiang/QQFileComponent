package cn.xxt.file.internal.domain;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.xxt.file.ui.manager.FileManagerMainAdapter;

/**
 * Created by zyj on 2017/8/25.
 */

public class FileManagerMainMultipleItem implements MultiItemEntity {
    public String title;

    public FileManagerMainMultipleItem(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int getItemType() {
        return FileManagerMainAdapter.ITEM;
    }
}
