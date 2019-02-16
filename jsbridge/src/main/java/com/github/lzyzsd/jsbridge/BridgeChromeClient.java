package com.github.lzyzsd.jsbridge;

import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

/**
 * Created by Luke on 2017/6/17.
 */

public class BridgeChromeClient extends WebChromeClient {

    private BridgeWebView webView;

    public BridgeChromeClient(BridgeWebView webView) {
        this.webView = webView;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);

        webView.updateProgress(newProgress);
    }
}
