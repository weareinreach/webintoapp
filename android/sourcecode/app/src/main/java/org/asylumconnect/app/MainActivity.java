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
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
//import android.database.Cursor;
import android.graphics.Bitmap;
//import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
import android.content.Context;
import androidx.annotation.RequiresApi;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
//import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
//import android.text.InputType;
//import android.widget.EditText;
import android.webkit.ValueCallback;
import android.provider.MediaStore;
import java.io.IOException;
import java.net.URISyntaxException;
import android.app.Activity;
import android.Manifest;
import android.webkit.URLUtil;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.webkit.CookieManager;
import android.content.SharedPreferences;
import android.widget.FrameLayout;
//import android.widget.Toast;
//import com.android.volley.AuthFailureError;
//import com.android.volley.toolbox.Volley;
//import com.android.volley.RequestQueue;
//import com.android.volley.Request;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import java.io.DataOutputStream;
import java.io.File;
//import java.io.FileOutputStream;
//import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
import android.view.KeyEvent;
//import org.jetbrains.annotations.NotNull;
//import android.widget.FrameLayout;
import android.graphics.BitmapFactory;
public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private WebView splash_mWebView;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private static final String TAG = "MainActivity";
    SharedPreferences prefs = null;
    int width = 0, height = 0;
    boolean display_error = false;
    boolean no_internet = false;
    SwipeRefreshLayout swipeRefreshLayout;
    SwipeRefreshLayout NavigateProgressBar;
    WebView newWebView;
    ViewGroup container;
    GeolocationPermissions.Callback mGeoLocationCallback = null;
    String mGeoLocationRequestOrigin = null;
    static final int INPUT_FILE_REQUEST_CODE = 1;
    static final int PERMISSION_LOC = 100;
    static final int PERMISSION_VIDEO_CAPTURE1 = 1001;
    static final int PERMISSION_VIDEO_CAPTURE2 = 1002;
    static final int PERMISSION_AUDIO = 106;
    PermissionRequest permissionRequest;
    static boolean homeLoaded = false;
    static String currentUrl = "";
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"SetJavaScriptEnabled", "CutPasteId"})
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
            protected FrameLayout mFullscreenContainer;
            private int mOriginalOrientation;
            private int mOriginalSystemUiVisibility;
            public void MyWebClient() {}
            public Bitmap getDefaultVideoPoster()
            {
                if (MainActivity.this == null) {
                    return null;
                }
                return BitmapFactory.decodeResource(MainActivity.this.getApplicationContext().getResources(), 2130837573);
            }
            @Override
            public void onHideCustomView()
            {
                ((FrameLayout)MainActivity.this.getWindow().getDecorView()).removeView(this.mCustomView);
                this.mCustomView = null;
                MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
                MainActivity.this.setRequestedOrientation(this.mOriginalOrientation);
                this.mCustomViewCallback.onCustomViewHidden();
                this.mCustomViewCallback = null;
            }
            @Override
            public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
            {
                if (this.mCustomView != null)
                {
                    onHideCustomView();
                    return;
                }
                this.mCustomView = paramView;
                this.mOriginalSystemUiVisibility = MainActivity.this.getWindow().getDecorView().getSystemUiVisibility();
                this.mOriginalOrientation = MainActivity.this.getRequestedOrientation();
                this.mCustomViewCallback = paramCustomViewCallback;
                ((FrameLayout)MainActivity.this.getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
                MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(3846);
            }
            @Override
            public void onCloseWindow(WebView window) {
            }
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOC);
                    mGeoLocationRequestOrigin = origin;
                    mGeoLocationCallback = callback;
                }
                else{
                    callback.invoke(origin, true, true);
                }
            }
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                permissionRequest = request;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    for(String permission: request.getResources()){
                        if(permission.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)){
                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_VIDEO_CAPTURE2);
                                return;
                            }
                            else{
                                getVideoCapturePermission();
                            }
                        }
                    }
                }
            }
            @Override
            public boolean onShowFileChooser(
                WebView webView, ValueCallback<Uri[]> filePathCallback,
                WebChromeClient.FileChooserParams fileChooserParams) {
                if(mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        Log.e(TAG, "Unable to create Image File", ex);
                    }
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;
                if(takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Files Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
                return true;
            }
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg)
            {
                // External Link
                newWebView = new WebView(view.getContext());
                newWebView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                container.addView(newWebView);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
                newWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                        return true;
                    }
                });
                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url)
                    {
                        if (url.startsWith("mailto:"))
                        {
                            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                        }
                        else if (url.startsWith("tel:"))
                        {
                            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                        }
                        else if(url.startsWith("intent:"))
                        {
                            Uri parsedUri = Uri.parse(url);
                            PackageManager packageManager = MainActivity.this.getPackageManager();
                            Intent browseIntent = new Intent(Intent.ACTION_VIEW).setData(parsedUri);
                            if (browseIntent.resolveActivity(packageManager) != null) {
                                MainActivity.this.startActivity(browseIntent);
                                return true;
                            }
                            try {
                                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                if (intent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                                    MainActivity.this.startActivity(intent);
                                    return true;
                                }
                                Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(
                                    Uri.parse("market://details?id=" + intent.getPackage()));
                                if (marketIntent.resolveActivity(packageManager) != null) {
                                    MainActivity.this.startActivity(marketIntent);
                                    return true;
                                }
                                else
                                    IntentFallvack(view, intent);
                            } catch (URISyntaxException e) {
                            }
                        }
                        SetWebView(newWebView);
                        newWebView.getSettings().setJavaScriptEnabled(true);
                        newWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                        /*
                        newWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
                        newWebView.getSettings().setAllowFileAccess(true);
                        newWebView.getSettings().setAllowFileAccessFromFileURLs(true);
                        newWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                        newWebView.getSettings().setUseWideViewPort(true);
                        newWebView.getSettings().setSupportZoom(true);
                        newWebView.getSettings().setLoadWithOverviewMode(true);
                        newWebView.getSettings().setDomStorageEnabled(true);
                        newWebView.getSettings().setSupportMultipleWindows(true);
                        newWebView.getSettings().setLoadWithOverviewMode(true);
                        newWebView.getSettings().setUseWideViewPort(true);
                        newWebView.getSettings().setBuiltInZoomControls(false);
                        newWebView.getSettings().setUserAgentString(System.getProperty("http.agent"));
                        newWebView.getSettings().setTextZoom(100);
                        if (Build.VERSION.SDK_INT > 17)
                        {
                            newWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                        }
                        */
                        newWebView.loadUrl(url);
                        return true;
                    }
                });
                return true;
            }
        });
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        NavigateProgressBar = findViewById(R.id.swipeRefreshLayout);
        WebSettings settings = mWebView.getSettings();
        settings.setDomStorageEnabled(true);
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                        Log.v(TAG, "Permission is granted");
                        downloadDialog(url, userAgent, contentDisposition, mimetype);
                    } else {
                        Log.v(TAG, "Permission is revoked");
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                } else {
                    Log.v(TAG, "Permission is granted");
                    downloadDialog(url, userAgent, contentDisposition, mimetype);
                }
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            void IntentFallvack(WebView webView, Intent intent)
            {
                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                if (fallbackUrl != null) {
                    webView.loadUrl(fallbackUrl);
                }
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                currentUrl = url;
                if(homeLoaded) {
                    showProgress();
                }
                if (!checkInternetConnection(MainActivity.this)) {
                    hideProgress();
                }
                else {
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https") || url.startsWith("http")) return false;//open web links as usual
                if (url.startsWith("mailto:")) {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                } else if (url.startsWith("tel:")) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                } else if(url.startsWith("intent:")) {
                    Uri parsedUri = Uri.parse(url);
                    PackageManager packageManager = MainActivity.this.getPackageManager();
                    Intent browseIntent = new Intent(Intent.ACTION_VIEW).setData(parsedUri);
                    if (browseIntent.resolveActivity(packageManager) != null) {
                        MainActivity.this.startActivity(browseIntent);
                        return true;
                    }
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                            MainActivity.this.startActivity(intent);
                            return true;
                        }
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(
                            Uri.parse("market://details?id=" + intent.getPackage()));
                        if (marketIntent.resolveActivity(packageManager) != null) {
                            MainActivity.this.startActivity(marketIntent);
                            return true;
                        }
                        else
                            IntentFallvack(view, intent);
                    } catch (URISyntaxException e) {
                    }
                }
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
                if(!homeLoaded){
                    homeLoaded = true;
                }
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            }
            @Override
            public void onLoadResource(WebView  view, String  url){
                if (!checkInternetConnection(MainActivity.this)) {
                    if(!no_internet) {
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
        if(data != null) {
            String url = intent.getDataString();
            assert url != null;
            if(url.startsWith("https://app.inreach.org")) //Check the url inorder to avoid cross-app scripting
                mWebView.loadUrl(url);
            else
                mWebView.loadUrl("https://app.inreach.org");
        }
        else
        {
            mWebView.loadUrl("https://app.inreach.org");
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getVideoCapturePermission() {
        permissionRequest.grant(permissionRequest.getResources());
    }
    public static boolean checkAudioPermission(Activity activity){
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    public static void getAudioPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, MainActivity.PERMISSION_AUDIO);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSION_LOC) {
                if (mGeoLocationCallback != null)
                    mGeoLocationCallback.invoke(mGeoLocationRequestOrigin, true, true);
            }
            else if(requestCode == PERMISSION_VIDEO_CAPTURE1){
                if(!checkAudioPermission(MainActivity.this)){
                    getAudioPermission(MainActivity.this);
                }
                else{
                    getVideoCapturePermission();
                }
            }
            else if(requestCode == PERMISSION_VIDEO_CAPTURE2){
                getVideoCapturePermission();
            }
            mWebView.reload();
        }
    }
    public void downloadDialog(final String url, final String userAgent, final String contentDisposition, final String mimetype) {
        if(url.startsWith("blob")) {
            mWebView.loadUrl(JavaScriptInterface.getBase64StringFromBlobUrl(url, mimetype));
        }
        else {
            final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Download");
            builder.setMessage("Download File" + ' ' + filename);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        String cookie = CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("Cookie", cookie);
                        request.addRequestHeader("User-Agent", userAgent);
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                        downloadManager.enqueue(request);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
    }
    public void showProgress(){
        if(true)
            NavigateProgressBar.setRefreshing(true);
    }
    public void hideProgress(){
        NavigateProgressBar.setRefreshing(false);
    }
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void SetWebView(WebView wv) {
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setSupportZoom(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setUserAgentString(System.getProperty("http.agent"));
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        wv.addJavascriptInterface(new JavaScriptInterface(MainActivity.this), "Android");
        wv.getSettings().setPluginState(WebSettings.PluginState.ON);
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
                if(newWebView != null) {
                    if(newWebView.canGoBack()){
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
    void IntentFallvack(WebView webView, Intent intent)
    {
        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
        if (fallbackUrl != null) {
            webView.loadUrl(fallbackUrl);
        }
    }
    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager con_manager = (ConnectivityManager)
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (con_manager.getActiveNetworkInfo() != null
            && con_manager.getActiveNetworkInfo().isAvailable()
            && con_manager.getActiveNetworkInfo().isConnected());
    }
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        );
    }
    public static void openUrlInChrome(Activity activity, String url){
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
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri[] results = null;
        if(resultCode == Activity.RESULT_OK) {
            if(data == null) {
                if(mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }
        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
    }
}
