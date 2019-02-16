package cn.xxt.file.internal.domain;

/**
 * Created by zyj on 2017/8/28.
 */

public enum ItemUiTypeEnum {
    ITEM_UI_TYPE_CONTENT(0),
    ITEM_UI_TYPE_CHECKBOX_CONTENT(1),
    ITEM_UI_TYPE_CONTENT_STATUS(2),

    ITEM_UI_TYPE_PHOTO_NORMAL(3),
    ITEM_UI_TYPE_PHOTO_EDIT(4);

    ItemUiTypeEnum(int itemUiType) {
        this.itemUiType = itemUiType;
    }


    public int getItemUiType() {
        return itemUiType;
    }

    public void setItemUiType(int itemUiType) {
        this.itemUiType = itemUiType;
    }

    private int itemUiType;
}
