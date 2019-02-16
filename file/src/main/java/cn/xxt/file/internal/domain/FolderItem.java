package cn.xxt.file.internal.domain;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by zyj on 2017/8/17.
 */

public class FolderItem extends AbstractExpandableItem<FileInfo> implements MultiItemEntity {
    public String title;

    public FolderItem(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int getLevel() {
        return ItemViewTypeEnum.ITEM_VIEW_TYPE_HEAD.getItemViewType();
    }

    @Override
    public int getItemType() {
        return 0;
    }
}
