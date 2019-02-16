package cn.xxt.file.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import cn.xxt.file.R;
import cn.xxt.file.internal.domain.FileInfo;

/**
 * Created by zyj on 2017/8/18.
 */

public class UiUtil {
    //获取空白页
    public static View getEmptyView(Context context, ViewGroup rootView) {
        return ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.empty_view_file, rootView, false);
    }

    //获取加载页
    public static View getLoadingView(Context context, ViewGroup rootView) {
        return ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.loading_view_file, rootView, false);
    }

    public static interface PopMenuInterface{
        void onItemClicked();
    }

    public static void showPopMenu(final Context context, final BaseViewHolder helper, final FileInfo fileInfo, final PopMenuInterface popMenuInterface) {
        List<String> menuList = new ArrayList<>();
        menuList.add("删除");

        if (menuList.size() > 0){
            int[] location = new int[2];

            helper.itemView.getLocationOnScreen(location);

            float scale = context.getResources().getDisplayMetrics().density;
            int resId = (location[1] - 50 * scale - 40 * scale < 0) ? R.layout.file_longpress_menu_pop_up_file : R.layout.file_longpress_menu_pop_down_file;
            View view = LayoutInflater.from(context).inflate(resId, null);
            final PopupWindow popupWindow = new PopupWindow(view, RecyclerView.LayoutParams.WRAP_CONTENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT);

            popupWindow.setBackgroundDrawable(new BitmapDrawable());

            LinearLayout llContent = (LinearLayout) view.findViewById(R.id.ll_content);
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_file_longpress_menu_file, null);
            TextView tView = (TextView) itemView.findViewById(R.id.tv_item_text);
            tView.setText("删除");
            ImageView ivDivider = (ImageView) itemView.findViewById(R.id.iv_divider);
            ivDivider.setVisibility(View.GONE);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    popMenuInterface.onItemClicked();
                    popupWindow.dismiss();
                }
            });
            llContent.addView(itemView);

            // 使其聚集
            popupWindow.setFocusable(false);
            // 设置允许在外点击消失
            popupWindow.setOutsideTouchable(true);

            int vWidth, vHeight;

            vWidth = helper.itemView.getWidth();
            vHeight = helper.itemView.getHeight();
            int x = location[0] + (int) ((vWidth - ((menuList.size() * 61 + 8) * scale)) / 2);
            int y = (location[1] - 50 * scale - 40 * scale < 0) ? location[1] + vHeight : location[1] - (int) (40 * scale);
            popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y);
            popupWindow.update();
        }
    }
}
