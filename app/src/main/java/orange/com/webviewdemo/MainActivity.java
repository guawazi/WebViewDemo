package orange.com.webviewdemo;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressDialog = new ProgressDialog(this);  // 这样才能创建出ProgressDialog
        progressDialog.setMessage("正在加载中");
        progressDialog.setCancelable(false); // 按返回键不可取消
        progressDialog.setCanceledOnTouchOutside(false);  // 点击其他地方不可取消
//        progressDialog =  new ProgressDialog.Builder(this)
//                .setTitle("这是标题")
//                .setMessage("正在加载中")
//                .create();// 这里拿到的是一个AlertDialog 不是progressDialog
        webView.addJavascriptInterface(this, "android");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) { // 加载页面前调用,用于显示进度条
                super.onPageStarted(view, url, favicon);
//                progressBar.setVisibility(View.VISIBLE);
                MainActivity.this.progressDialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) { // 加载页面后调用,GONE掉进度条
                super.onPageFinished(view, url);
//                progressBar.setVisibility(View.GONE);
                MainActivity.this.progressDialog.hide();
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { // 21 以上走这个方法
                String url = request.getUrl().toString();
                if (url.contains("baidu")){  // URL中包含baidu 转成另外一个网站
                    view.loadUrl("http://blog.csdn.net/harvic880925/article/details/51523983");
                }else {
                    view.loadUrl(url);
                }
                return true;  // 拦截返回true
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {  // 21 以下会走这个方法
                if (url.contains("baidu")){  //
                    view.loadUrl("http://blog.csdn.net/harvic880925/article/details/51523983");
                }else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                view.loadUrl("file:///android_asset/error.html"); // 出错后加载的页面
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("file:///android_asset/error.html");
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) { // https 请求失败后的回调

//                super.onReceivedSslError(view, handler, error); // 想要继续加载的话必须注释掉,因为默认调用 cancle();
                handler.proceed(); // 继续加载
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Toast.makeText(MainActivity.this,"xxx",Toast.LENGTH_SHORT).show(); // 拦截后自己处理
                result.confirm(); // 拦截后设置点击了确认按钮,如果不写,则JS会认为alert还在那,再次点击不会有效果
                return true; // 表示拦截了.自己处理,如果false,表示没处理,则既弹吐司,又弹对话框
            }
        }); // 加上这句话就能处理JS中的弹窗
        webView.requestFocusFromTouch(); // 如果用户要输入密码,请求获取焦点
        WebSettings settings = webView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 开启缓存
        settings.setJavaScriptEnabled(true); // 开启JavaScript
        // 自适应屏幕
        settings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);// 设置可以支持缩放
        settings.setBuiltInZoomControls(true);// 设置出现缩放工具
        //https://www.12306.cn/
        webView.loadUrl("file:///android_asset/alert.html");
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_1:
                Toast.makeText(this, "系好安全带", Toast.LENGTH_SHORT).show();
                webView.loadUrl("javascript:sum(3,8)");
                break;
        }
    }

    @JavascriptInterface
    public void toastMessage(String message) {
        Toast.makeText(this, "通过Natvie传递的Toast:" + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //改写物理返回键的逻辑
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            if(webView.canGoBack()) {
                webView.goBack();//返回上一页面
                return true;
            } else {
                System.exit(0);//退出程序
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
