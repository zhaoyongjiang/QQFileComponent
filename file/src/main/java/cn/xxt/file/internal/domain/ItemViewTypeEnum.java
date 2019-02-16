package cn.xxt.file.internal.domain;

/**
 * Created by zyj on 2017/8/25.
 */

public enum ItemViewTypeEnum {
    ITEM_VIEW_TYPE_HEAD(0),
    ITEM_VIEW_TYPE_PHOTO(1),
    ITEM_VIEW_TYPE_FILE(2);

    ItemViewTypeEnum(int itemViewType) {
        this.itemViewType = itemViewType;
    }

    public int getItemViewType() {
        return itemViewType;
    }

    public void setItemViewType(int itemViewType) {
        this.itemViewType = itemViewType;
    }

    private int itemViewType;
}
