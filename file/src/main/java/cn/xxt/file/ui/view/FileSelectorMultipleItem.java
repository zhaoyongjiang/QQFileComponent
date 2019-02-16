package cn.xxt.file.ui.view;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.xxt.file.internal.domain.FileInfo;

/**
 * Created by zyj on 2017/8/15.
 */

public class FileSelectorMultipleItem implements MultiItemEntity {
    public static final int FOLD = 1;
    public static final int FILE = 2;
    private int itemType;
    private FileInfo data ;

    public FileSelectorMultipleItem(int itemType, FileInfo data) {
        this.data = data;
        this.itemType = itemType;
    }

    public FileInfo getData() {
        return data;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
