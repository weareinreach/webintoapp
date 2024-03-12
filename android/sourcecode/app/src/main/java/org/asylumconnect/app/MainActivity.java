// Copyright 2023 (c) WebIntoApp.com
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of
// this software and associated documentation files (the "Software"), to deal in the
// Software without restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
// Software, and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//  InReach
//
//  Created by InReach on 03/10/2023.
//
package org.asylumconnect.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.net.ConnectivityManager;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.FrameLayout;
import android.view.KeyEvent;


public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    SharedPreferences prefs = null;
//    int width = 0, height = 0;
    boolean display_error = false;
    boolean no_internet = false;
    SwipeRefreshLayout swipeRefreshLayout;
    SwipeRefreshLayout NavigateProgressBar;
    WebView newWebView;
    ViewGroup container;
    static boolean homeLoaded = false;
    static String currentUrl = "";

//    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({ "SetJavaScriptEnabled", "CutPasteId" })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        WebView splash_mWebView = findViewById(R.id.activity_splash_webview);
        splash_mWebView.setWebChromeClient(new WebChromeClient());
        splash_mWebView.setWebViewClient(new WebViewClient());
        WebSettings webSettings_splash = splash_mWebView.getSettings();
        webSettings_splash.setJavaScriptEnabled(true);
        splash_mWebView.loadUrl("file:///android_asset/htmlapp/helpers/loading.html");
        prefs = getSharedPreferences("org.asylumconnect.app", MODE_PRIVATE);
        mWebView = findViewById(R.id.activity_main_webview);
        container = findViewById(R.id.container);
        mWebView.setWebChromeClient(new WebChromeClient() {
            private View mCustomView;
            private WebChromeClient.CustomViewCallback mCustomViewCallback;
//            protected FrameLayout mFullscreenContainer;
            private int mOriginalOrientation;
            private int mOriginalSystemUiVisibility;

            @Override
            public void onHideCustomView() {
                ((FrameLayout) MainActivity.this.getWindow().getDecorView()).removeView(this.mCustomView);
                this.mCustomView = null;
                MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
                MainActivity.this.setRequestedOrientation(this.mOriginalOrientation);
                this.mCustomViewCallback.onCustomViewHidden();
                this.mCustomViewCallback = null;
            }

            @Override
            public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
                if (this.mCustomView != null) {
                    onHideCustomView();
                    return;
                }
                this.mCustomView = paramView;
                this.mOriginalSystemUiVisibility = MainActivity.this.getWindow().getDecorView().getSystemUiVisibility();
                this.mOriginalOrientation = MainActivity.this.getRequestedOrientation();
                this.mCustomViewCallback = paramCustomViewCallback;
                ((FrameLayout) MainActivity.this.getWindow().getDecorView()).addView(this.mCustomView,
                        new FrameLayout.LayoutParams(-1, -1));
                MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(3846);
            }



            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
                    android.os.Message resultMsg) {
                // External Link
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("External Link");


                newWebView = new WebView(view.getContext());
                newWebView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
//                container.addView(newWebView);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
                newWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
                            android.os.Message resultMsg) {
                        return true;
                    }
                });
                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        String url = request.getUrl().toString();
                        if (url.startsWith("mailto:")) {
                            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                        } else if (url.startsWith("tel:")) {
                            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                        } //else if (url.startsWith("intent:")) {
//                            Uri parsedUri = Uri.parse(url);
//                            PackageManager packageManager = MainActivity.this.getPackageManager();
//                            Intent browseIntent = new Intent(Intent.ACTION_VIEW).setData(parsedUri);
//                            if (browseIntent.resolveActivity(packageManager) != null) {
//                                MainActivity.this.startActivity(browseIntent);
//                                return true;
//                            }
//                            try {
//                                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
//                                if (intent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
//                                    MainActivity.this.startActivity(intent);
//                                    return true;
//                                }
//                                Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(
//                                        Uri.parse("market://details?id=" + intent.getPackage()));
//                                if (marketIntent.resolveActivity(packageManager) != null) {
//                                    MainActivity.this.startActivity(marketIntent);
//                                    return true;
//                                } else
//                                    IntentFallvack(view, intent);
//                            } catch (Exception e) {
//                            }
                       // }

                        // do we need this?
//                        SetWebView(newWebView);
                        newWebView.getSettings().setJavaScriptEnabled(true);
                        newWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//                        newWebView.getSettings().setSupportMultipleWindows(true);
                        newWebView.getSettings().setDomStorageEnabled(true);
                        newWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

                        /*
                         * newWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
                         * newWebView.getSettings().setAllowFileAccess(true);
                         * newWebView.getSettings().setAllowFileAccessFromFileURLs(true);
                         * newWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                         * newWebView.getSettings().setUseWideViewPort(true);
                         * newWebView.getSettings().setSupportZoom(true);
                         * newWebView.getSettings().setLoadWithOverviewMode(true);
                         * newWebView.getSettings().setLoadWithOverviewMode(true);
                         * newWebView.getSettings().setUseWideViewPort(true);
                         * newWebView.getSettings().setBuiltInZoomControls(false);
                         * newWebView.getSettings().setUserAgentString(System.getProperty("http.agent"))
                         * ;
                         * newWebView.getSettings().setTextZoom(100);
                         * if (Build.VERSION.SDK_INT > 17)
                         * {
                         * newWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                         * }
                         */
                        newWebView.loadUrl(url);
                        return true;
                    }
                });
                alert.setView(newWebView);
                alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                return true;
            }
        });
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        NavigateProgressBar = findViewById(R.id.swipeRefreshLayout);
        WebSettings settings = mWebView.getSettings();
        settings.setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
//            void IntentFallvack(WebView webView, Intent intent) {
//                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
//                if (fallbackUrl != null) {
//                    webView.loadUrl(fallbackUrl);
//                }
//            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                currentUrl = url;
                if (homeLoaded) {
                    showProgress();
                }
                if (!checkInternetConnection(MainActivity.this)) {
                    hideProgress();
                } //else {
                //}
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if ((url.startsWith("https") || url.startsWith("http")) && !url
                        .matches("^https?:\\/\\/app\\.inreach\\.org.*"))

                    return false;// open web links as usual
                if (url.startsWith("mailto:")) {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                } else if (url.startsWith("tel:")) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                } else if (url.startsWith("sms:")) {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                }
//                else if (url.startsWith("intent:")) {
//                    Uri parsedUri = Uri.parse(url);
//                    PackageManager packageManager = MainActivity.this.getPackageManager();
//                    Intent browseIntent = new Intent(Intent.ACTION_VIEW).setData(parsedUri);
//                    if (browseIntent.resolveActivity(packageManager) != null) {
//                        MainActivity.this.startActivity(browseIntent);
//                        return true;
//                    }
//                    try {
//                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
//                        if (intent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
//                            MainActivity.this.startActivity(intent);
//                            return true;
//                        }
//                        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(
//                                Uri.parse("market://details?id=" + intent.getPackage()));
//                        if (marketIntent.resolveActivity(packageManager) != null) {
//                            MainActivity.this.startActivity(marketIntent);
//                            return true;
//                        } else
//                            IntentFallvack(view, intent);
//                    } catch (URISyntaxException e) {
//                    }
//                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(mWebView, url);
                hideProgress();
                swipeRefreshLayout.setRefreshing(false);
                findViewById(R.id.activity_splash_webview).setVisibility(View.GONE);
                findViewById(R.id.activity_main_webview).setVisibility(View.VISIBLE);
                display_error = true;
                if (!homeLoaded) {
                    homeLoaded = true;
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (!checkInternetConnection(MainActivity.this)) {
                    if (!no_internet) {
                    }
                    no_internet = true;
                }
            }
        });
        SetWebView(mWebView);
        if (!checkInternetConnection(MainActivity.this)) {
            mWebView.loadUrl("file:///android_asset/htmlapp/helpers/error.html");
            no_internet = true;
            return;
        }
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            String url = intent.getDataString();
            assert url != null;
            if (url.startsWith("https://app.inreach.org")) // Check the url inorder to avoid cross-app scripting
                mWebView.loadUrl(url);
            else
                mWebView.loadUrl("https://app.inreach.org");
        } else {
            mWebView.loadUrl("https://app.inreach.org");
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
    }

    public void showProgress() {
            NavigateProgressBar.setRefreshing(true);
    }

    public void hideProgress() {
        NavigateProgressBar.setRefreshing(false);
    }

    @SuppressLint({ "SetJavaScriptEnabled", "AddJavascriptInterface" })
    private void SetWebView(WebView wv) {
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
//        webSettings.setAllowFileAccessFromFileURLs(true);
//        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setSupportZoom(true);
//        webSettings.setDatabaseEnabled(true);
        webSettings.setUserAgentString(System.getProperty("http.agent"));
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //wv.addJavascriptInterface(new JavaScriptInterface(MainActivity.this), "Android");
        //wv.getSettings().setPluginState(WebSettings.PluginState.ON);
    }

    // External Link
    private void closeWebviewPopup() {
        if (newWebView != null) {
            newWebView.setVisibility(View.GONE);
            container.removeView(newWebView);
            newWebView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit().putBoolean("firstrun", false).apply();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // External Link
                if (newWebView != null) {
                    if (newWebView.canGoBack()) {
                        newWebView.goBack();
                        return true;
                    }
                    closeWebviewPopup();
                    return true;
                }
                if (no_internet) {
                    finish();
                }
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

//    void IntentFallvack(WebView webView, Intent intent) {
//        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
//        if (fallbackUrl != null) {
//            webView.loadUrl(fallbackUrl);
//        }
//    }

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager con_manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected());
    }

    public static void openUrlInChrome(Activity activity, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            intent.setPackage(null);
            activity.startActivity(intent);
        }
    }
}
