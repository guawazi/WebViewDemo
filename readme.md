---
title: WebView
tags: webview
grammar_cjkRuby: true
---

## 简单使用

[启舰的文章](http://blog.csdn.net/harvic880925/article/details/51464687)
- 访问网络

```java
    mWebView.setWebViewClient(new WebViewClient());   // 加上这句才能在自己的页面打开网页
    mWebView.loadUrl("http://www.baidu.com");
```

- 访问本地

```java
mWebView.loadUrl("file:///android_asset/web.html");  // 本地可以不用加client 路径名基本固定
```

- 设置webview的控制用setting(更多属性看博客)

```java
		webView.requestFocusFromTouch(); // 如果用户要输入密码,请求获取焦点
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // 开启JavaScript
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 开启缓存
        // 自适应屏幕
        settings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        settings.setLoadWithOverviewMode(true);

        settings.setSupportZoom(true);// 设置可以支持缩放
        settings.setBuiltInZoomControls(true);// 设置出现缩放工具
```

- JavaScript调用java代码

```java
mWebView.addJavascriptInterface(this, "android");  // 将本类注入,并给其去别名"android"

  @JavascriptInterface  // API17 以后JavaScript 只能访问带注解的方法
    public void toastMessage(String message) {  // 与JavaScript代码调用的方法名相对应
        Toast.makeText(this, "通过Natvie传递的Toast:"+message, Toast.LENGTH_LONG).show();
    }
```

```html
function ok() {
 android.toastMessage("哈哈,i m webview msg"); // 调用java的代码
}
```

> tips 上面的方法有漏洞,一般注入的对象是单独写一个类,并且加上注释

- java调用JS代码
  - 无返回值的情况
```java
String url = "javascript:methodName(params……);"  // 使用JavaScript伪协议来访问JS代码
webView.loadUrl(url);  
```

```html
// JavaScript中的代码
function sum(i,m)   
{
 document.getElementById("h").innerHTML= (i+m);
}
```

  - 获取JS中的返回值
    - 4.4以前的做法: java调用JS代码得到结果后,JS再调用java代码,将数据返回
    
```java
  WebSettings webSettings = mWebView.getSettings();  
        webSettings.setJavaScriptEnabled(true);  
        mWebView.addJavascriptInterface(this, "android");  
        mWebView.loadUrl("file:///android_asset/web.html");  
  
        mBtn.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                mWebView.loadUrl("javascript:sum(3,8)");  
            }  
        });  
    }  
  
    public void onSumResult(int result) {  
        Toast.makeText(this,"received result:"+result,Toast.LENGTH_SHORT).show();  
    }  
```

```html
<!DOCTYPE html>  
<html lang="en">  
<head>  
    <meta charset="UTF-8">  
    <title>Title</title>  
    <h1 id="h">欢迎光临启舰的blog</h1>  
    <input type="button" value="js调native" onclick="ok()">  
</head>  
<body>  
<script type="text/javascript">  
function sum(i,m){  
 var result = i+m;  
 document.getElementById("h").innerHTML= result;  
 android.onSumResult(result);  
}  
</script>  
</body>  
</html>  
```

 - 4.4之后获取返回值

```java
// 直接调用这个计算方法,在回调中拿到值就行了,必须在主线程 
//回调中的值只能是String 复杂数据返回JSON
webView.evaluateJavascript("getGreetings()", new ValueCallback() {  
       @Override  
       public void onReceiveValue(String value) {   
           Log.i(LOGTAG, "onReceiveValue value=" + value);  
       }  
   });  
```

## WebViewClient
- 如果想在加载网页前加上进度条等操作
```java
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
        });
```
- 拦截超链接

```java
public boolean shouldOverrideUrlLoading(WebView view, String url)  ; 	//返回true拦截,false不拦截
```

```java
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
```

> tips : 在利用shouldOverrideUrlLoading来拦截URL时，如果return true，则会屏蔽系统默认的显示URL结果的行为，不需要处理的URL也需要调用loadUrl()来加载进WebVIew，不然就会出现白屏；如果return false，则系统默认的加载URL行为是不会被屏蔽的，所以一般建议大家return false，我们只关心我们关心的拦截内容，对于不拦截的内容，让系统自己来处理即可。

- onReceivedError 加载错误时调用

```java
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
```

- WebView view:当前的WebView实例
- SslErrorHandler handler：当前处理错误的Handler，它只有两个函数SslErrorHandler.proceed()和SslErrorHandler.cancel()，SslErrorHandler.proceed()表示忽略错误继续加载，SslErrorHandler.cancel()表示取消加载。在onReceivedSslError的默认实现中是使用的SslErrorHandler.cancel()来取消加载，所以一旦出来SSL错误，HTTPS网站就会被取消加载了，如果想忽略错误继续加载就只有重写onReceivedSslError，并在其中调用SslErrorHandler.proceed()
- SslError error：当前的的错误对象，SslError包含了当前SSL错误的基本所有信息，大家自己去看下它的方法吧，这里就不再展开了。


> tips : 当出现SSL错误时，WebView默认是取消加载当前页面，只有去掉onReceivedSslError的默认操作，然后添加SslErrorHandler.proceed()才能继续加载出错页面
当HTTPS传输出现SSL错误时，错误会只通过onReceivedSslError回调传过来,不会走上面那两个错误回调了

- shouldInterceptRequest 当没网的情况下,可以直接打断请求,返回一个本地网页,注意,这是在非UI线程

```java
 @Override  
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {  
                try {  
                    if (url.equals("http://localhost/qijian.png")) {   // 如果访问的是这个网站,直接打断显示本地图片
                        AssetFileDescriptor fileDescriptor =  getAssets().openFd("s07.jpg");  
                        InputStream stream = fileDescriptor.createInputStream();  
                        WebResourceResponse response = new WebResourceResponse("image/png", "UTF-8", stream);  
                        return response;  
                    }  
                }catch (Exception e){  
                    Log.e(TAG,e.getMessage());  
                }  
                return super.shouldInterceptRequest(view, url);  // 反之则还是调用父类的方法
            }  
```

- webview中按返回键,返回上一级页面

```java
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
```

- 滚动事件监听,只有重写webview

## WebChromeClient

- WebClient不能处理弹窗,但是WebChromeClient可以

```java
webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Toast.makeText(MainActivity.this,"xxx",Toast.LENGTH_SHORT).show(); // 拦截后自己处理
                result.confirm(); // 拦截后设置点击了确认按钮,如果不写,则JS会认为alert还在那,再次点击不会有效果
                return true; // 表示拦截了.自己处理,如果false,表示没处理,则既弹吐司,又弹对话框
            }
        }); // 加上这句话就能处理JS中的弹窗
```

> tips: 如果需要使网页中的confrim()、alert()、prompt()函数生效，需要设置WebChromeClient！
在使用onJsAlert来拦截alert对话框时，如果不需要再弹出alert对话框，一定要return true;在return false以后，会依然调用系统的默认处理来弹出对话框的
如果我们return true,则需要在处理完成以后调用JsResult.confirm()或者JsResult.cancel()来告诉WebView我们点中哪个按钮来取消程序对话框。否则再次点击按钮将会失败

- onProgressChanged 界面加载的进度



## LoadData()与loadDataWithBaseURL()