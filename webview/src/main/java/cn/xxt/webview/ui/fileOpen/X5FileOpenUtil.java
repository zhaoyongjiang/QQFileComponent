package cn.xxt.webview.ui.fileOpen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsVideo;
import com.tencent.smtt.sdk.ValueCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import cn.xxt.commons.util.FileUtil;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.commons.util.ToastUtil;

import static cn.xxt.webview.ui.fileOpen.DocPreviewActivity.BUNDLE_FILE_PATH;
import static cn.xxt.webview.ui.fileOpen.DocPreviewActivity.BUNDLE_TITLE;

/**
 * Created by zyj on 2018/8/2.
 */
public class X5FileOpenUtil {
    /**
     * 文件打开内部接口
     */
    public interface OnFileOpenListener {

        void openResult(boolean result);
    }

    public void setOnFileOpenListener(OnFileOpenListener onFileOpenListener) {
        this.onFileOpenListener = onFileOpenListener;
    }

    private OnFileOpenListener onFileOpenListener;

    public enum FileTypeEnum {
        //本地文件支持应用内打开，在线文档是浏览器打开
        FILE_TYPE_ENUM_OFFICE,
        //本地，在线视频都可播放
        FILE_TYPE_ENUM_VIDEO,
        //同视频
        FILE_TYPE_ENUM_AUDIO,
        //url，浏览器打开。。应用内链接一般使用应用内的webviewpage/videoactivity打开了。
        FILE_TYPE_ENUM_URL,
    }

    public static final String K_FILE_PATH = "K_FILE_PATH";
    public static final String K_FILE_TYPE = "K_FILE_TYPE";

    private Context context;

    private String filePath;

    private String fileName;

    private FileTypeEnum fileType;

    public X5FileOpenUtil(Context context, String filePath, String fileName, FileTypeEnum fileType) {
        this.context = context;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    /**
     *
     * x5内核打开文件。file组件里的文档，视频类打开使用的就是这个。唯一区别是：FileComponent类里的openFile中，
     * 音乐播放是参照qq的音乐文件播放界面。这里的音乐播放，是使用的视频的播放页面。所以，页面一般是没有东西的。黑屏
     *
     * @param onFileOpenListener
     */
    public void openFile(final OnFileOpenListener onFileOpenListener) {

        this.onFileOpenListener = onFileOpenListener;

        switch (fileType) {
            case FILE_TYPE_ENUM_OFFICE:
                startOffice();
                break;
            case FILE_TYPE_ENUM_AUDIO:
                startAudio();
                break;
            case FILE_TYPE_ENUM_VIDEO:
                startVideo();
                break;
            default:
                startUrl();
                break;
        }
    }

    private void startOffice() {
//        if (!StringUtil.isEmpty(filePath) && filePath.startsWith("/")) {
//            Intent intent = new Intent();
//
//            intent.setClass(context, DocPreviewActivity.class);
//
//            Bundle bundle = new Bundle();
//            bundle.putString(BUNDLE_TITLE, fileName);
//            bundle.putString(BUNDLE_FILE_PATH, filePath);
//
//            intent.putExtras(bundle);
//
//            context.startActivity(intent);
//
//            callBack(true);
//        } else {
//            startUrl();
//        }

        /**
         * 添加人: zyj
         * 添加日期: 2019/1/9 下午4:18
         * 添加描述：支撑h5打开文档
         *
         */
        Intent intent = new Intent();

        intent.setClass(context, DocPreviewActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TITLE, fileName);
        bundle.putString(BUNDLE_FILE_PATH, filePath);

        intent.putExtras(bundle);

        context.startActivity(intent);

        callBack(true);
    }

    private void startAudio() {
        if (!StringUtil.isEmpty(filePath) && filePath.startsWith("/")) {
            //本地
            if (FileUtil.fileIsExsit(filePath)) {
                TbsVideo.openVideo(context, filePath);
                callBack(true);
            } else {
                ToastUtil.displayToastShort(context, "文件不存在");

                callBack(false);
            }
        } else {
            TbsVideo.openVideo(context, filePath);
            callBack(true);
        }
    }

    private void startVideo() {
        if (!StringUtil.isEmpty(filePath) && filePath.startsWith("/")) {
            //本地
            if (FileUtil.fileIsExsit(filePath)) {
                TbsVideo.openVideo(context, filePath);
                callBack(true);
            } else {
                ToastUtil.displayToastShort(context, "文件不存在");
                callBack(false);
            }
        } else {
            TbsVideo.openVideo(context, filePath);
            callBack(true);
        }
    }

    private void startUrl() {
        QbSdk.startQbOrMiniQBToLoadUrl(context, filePath, getParams(), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
//                ToastUtil.displayToastShort(context, s);
                //FIXME 测试，返回内容。判断
                callBack(true);
            }
        });

//        if (!StringUtil.isEmpty(urlStr)) {
//            Bundle bundle = new Bundle();
//            bundle.putString("pathStr", urlStr);
//            Intent intent = new Intent();
//            intent.setClassName(context,"cn.xxt.webview.ui.webviewpage.WebViewPageActivity");
//            intent.putExtras(bundle);
//            context.startActivity(intent);
//        }
    }

    private String parsePath2Uri(String path) {
        String uri = "";

        File file = new File(path);
        uri = file.toURI() + "";

        return uri;
    }

    String jsondata = "{ pkgName:\"cn.xxt.file.util\", "
            + "className:\"cn.xxt.file.util.X5FileOpenUtil\","
            + "thirdCtx: {pp:123},"
            //            + "menuItems:"
            //            + "["
            //            + "{id:0,iconResId:"+ R.drawable.ic_back +",text:\"menu0\"}, "
            //            + "{id:1,iconResId:" + R.drawable.ic_close + ",text:\"menu1\"}, "
            //            + "{id:2,iconResId:"+ R.drawable.ic_delete +",text:\"菜单 2\"}"
            //            + "]"
            + " }";
    private String getJsondata() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("pkgName", "cn.xxt.file.util");
            jsonObject.put("className", "cn.xxt.file.util.X5FileOpenUtil");

            JSONObject thirdCtx = new JSONObject();
            thirdCtx.put(K_FILE_PATH, filePath);
            thirdCtx.put(K_FILE_TYPE, fileType);

            jsonObject.put("thirdCtx", thirdCtx);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    //参数
    //local:“true”表示是进入文件查看器,如果不设置或设置为“false”,则进入 miniqb 浏览器模式。不是必须设置项。
    //style:“0”表示文件查看器使用默认的 UI 样式。“1”表示文件查看器使用微信的 UI 样式。不设置此 key或设置错误值,则为默认 UI 样式
    //topBarBgColor:定制文件查看器的顶部栏背景色。格式为“#xxxxxx”,例“#2CFC47”;不设置此 key 或设置错误值,则为默认 UI 样式
    //ValueCallback:回调参数出现如下字符时,表示可以关闭当前进程,避免内存占用
    //                        openFileReader open in QB
    //                        filepath error
    //                        TbsReaderDialogClosed
    //                        default browser:
    //                        filepath error
    //                        fileReaderClosed

    private HashMap<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();

        params.put("style", "1");
        params.put("local", "false");
        params.put("style", "0");
        params.put("topBarBgColor", "#48BAF3");
        params.put("menuData", getJsondata());

        return params;
    }

    private void callBack(boolean result) {
        if (onFileOpenListener != null) {
            onFileOpenListener.openResult(result);
        }
    }
}
