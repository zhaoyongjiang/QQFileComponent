package cn.xxt.file.ui.manager;


import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.xxt.file.R;
import cn.xxt.file.internal.domain.FileManagerMainMultipleItem;


/**
 * Created by zyj on 2017/8/15.
 */

public class FileManagerMainAdapter extends BaseMultiItemQuickAdapter<FileManagerMainMultipleItem, BaseViewHolder> {

    public static final int ITEM = 0;

    public FileManagerMainAdapter(List<FileManagerMainMultipleItem> data) {
        super(data);
        addItemType(ITEM, R.layout.item_manager_main_file);
    }

    @Override
    protected void convert(BaseViewHolder helper, FileManagerMainMultipleItem item) {

        if (helper.getItemViewType() == ITEM) {
            helper.setText(R.id.tv_title, item.getTitle());
        }
    }
}
