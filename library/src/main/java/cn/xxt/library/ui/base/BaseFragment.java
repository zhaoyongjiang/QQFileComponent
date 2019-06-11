package cn.xxt.library.ui.base;


import android.os.Bundle;
import android.view.KeyEvent;

import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.Iterator;
import java.util.Map;

import androidx.annotation.Nullable;


public class BaseFragment extends RxFragment {

    private String title;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

    }

    /**
     * 获取界面名称，需在子类中覆写
     * @return 界面名称
     */
    protected String getFragmentName() {
        return "尚未命名";
    }

    /**
     * 获取日志虚拟链接
     * @return
     */
    protected String getFragmentUrl() {
        StringBuffer url = new StringBuffer(this.getClass().getSimpleName().replace("Fragment",".fragment"));

        Map<String,String> paramMap = this.getAccessLogParam();
        if (paramMap!=null && !paramMap.isEmpty()) {
            url.append("?");
            Iterator<Map.Entry<String, String>> it = paramMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                url.append(entry.getKey());
                url.append("=");
                url.append(entry.getValue());
                url.append("&");
            }

            url.deleteCharAt(url.length()-1);
        }

        return url.toString();
    }

    protected Map<String,String> getAccessLogParam() {
        return null;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean onDispatchKeyEvent(KeyEvent event) {
        return false;
    }
}
