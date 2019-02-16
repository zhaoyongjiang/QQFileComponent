package cn.xxt.commons.ui.base;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.Iterator;
import java.util.Map;

import cn.xxt.commons.R;
import cn.xxt.commons.util.RxBusWithTag;
import cn.xxt.commons.util.StatusBarUtil;

public class TranslucentActivity extends RxAppCompatActivity {

    private boolean customStatusBarFlag = true;

    private boolean customAccessLogFlag = false;

    /** 修改原生状态栏颜色，由绿色改为白色 */
    private int customStatusBarColorRes = R.color.white;

    private boolean isForeGround = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (customStatusBarFlag && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            //通知栏所需颜色
            tintManager.setStatusBarTintResource(customStatusBarColorRes);


        }
    }

    protected void setCustomStatusBar(boolean flag) {
        this.customStatusBarFlag = flag;
    }

    public void setCustomAccessLogFlag(boolean customAccessLogFlag) {
        this.customAccessLogFlag = customAccessLogFlag;
    }

    protected void setCustomStatusBarColor(int res) {
        this.customStatusBarColorRes = res;
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    protected void onStart() {
        super.onStart();
        StatusBarUtil.statusBarLightMode(this,
                StatusBarUtil.statusBarLightMode(this));

        isForeGround = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isForeGround = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBusWithTag.getInstance().cleanUnusedSubject();
        isForeGround = false;
    }

    /**
     * 获取界面名称，需在子类中覆写
     * @return 界面名称
     */
    protected String getActivityName() {
        return "尚未命名";
    }

    /**
     * 获取日志虚拟链接
     * @return
     */
    protected String getActivityUrl() {
        StringBuffer url = new StringBuffer(this.getClass().getSimpleName()
                .replace("Activity",".activity"));

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

    public boolean onFinish() {
        return false;
    }

    protected boolean getIsForeGround()
    {
        return isForeGround;
    }
}
