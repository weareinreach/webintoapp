package org.asylumconnect.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.ProgressBar


class MainActivity : AppCompatActivity() {
  private lateinit var webView: WebView
  lateinit var progressBar: ProgressBar
  private val mainUrl: Uri = Uri.parse("https://app.inreach.org")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_main)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }
    progressBar = findViewById(R.id.progressBar)
    webView = findViewById(R.id.webView)
    webView.webViewClient = MainWebviewClient(this)

    setWebView(webView)

    webView.loadUrl(mainUrl.toString())

  }

  inner class MainWebviewClient(val activity: MainActivity) : WebViewClient() {
    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    fun setWebView(wv: WebView) {
      val webSettings = wv.getSettings()
      webSettings.javaScriptEnabled = true
      webSettings.domStorageEnabled = true
      webSettings.setSupportMultipleWindows(false)
      webSettings.javaScriptCanOpenWindowsAutomatically = true
      webSettings.builtInZoomControls = false
      webSettings.displayZoomControls = false
      webSettings.loadWithOverviewMode = true
      webSettings.useWideViewPort = true
      webSettings.setSupportZoom(true)
      webSettings.userAgentString = System.getProperty("http.agent")
      webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
    }

    inner class WebDrawer(context: Context) : BottomSheetDialog(context) {
      override fun onStart() {
        super.onStart()
        val bottomSheet =
          findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
          val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
          val screenHeight = activity.resources.displayMetrics.heightPixels
          bottomSheetBehavior.isFitToContents = false
          bottomSheetBehavior.expandedOffset = (screenHeight * 0.05).toInt(
          )
          bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//                            view?.findViewById<WebView>(R.id.externalWebView)?.visibility =
//                                View.INVISIBLE
//                            view?.findViewById<TextView>(R.id.imageLoading1)?.visibility =
//                                View.VISIBLE
        }
      }
    }

    @SuppressLint("SetJavaScriptEnabled", "InflateParams")
    override fun shouldOverrideUrlLoading(
      view: WebView?,
      request: WebResourceRequest
    ): Boolean {
      val url = request.url.toString()
      if (!url.startsWith("app.inreach.org")) {

        val bottomSheetDialog = WebDrawer(activity)
        val bottomSheetView =
//                    bottomSheetDialog.setContentView(R.layout.bottom_sheet_webview)
          LayoutInflater.from(activity)
            .inflate(R.layout.bottom_sheet_webview, null)

        val externalWebView = bottomSheetView.findViewById<WebView>(R.id.externalWebView)
        externalWebView?.webViewClient = ExternalWebViewClient()
        setWebView(externalWebView)
        externalWebView.visibility = View.INVISIBLE
        externalWebView?.loadUrl(url)
        val closeButton =
          bottomSheetView.findViewById<ImageButton>(R.id.closeButton) // Find close button

        closeButton.setOnClickListener {
          bottomSheetDialog.dismiss()
        }
        val openInBrowserButton =
          bottomSheetView.findViewById<ImageButton>(R.id.openInBrowser)
        openInBrowserButton.setOnClickListener {
          val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
          startActivity(it.context, browserIntent, null)
        }
//                val screenHeight = activity.resources.displayMetrics.heightPixels

//                bottomSheetDialog.behavior.peekHeight = (screenHeight * 0.95).toInt()
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
      } else {
        view?.loadUrl(url)
      }
      return true
    }

    override fun onPageFinished(view: WebView, url: String) {
      super.onPageFinished(view, url)

      progressBar.visibility = View.GONE

    }
  }

  inner class ExternalWebViewClient : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
      view.loadUrl(request.url.toString())
      return false
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
      super.onPageCommitVisible(view, url)
      view?.visibility = View.VISIBLE
      progressBar.visibility = View.INVISIBLE
    }

  }


  @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
  fun setWebView(wv: WebView) {
    val webSettings = wv.getSettings()
    webSettings.javaScriptEnabled = true
    webSettings.domStorageEnabled = true
    webSettings.setSupportMultipleWindows(false)
    webSettings.javaScriptCanOpenWindowsAutomatically = true
    //        webSettings.setAllowFileAccess(true);
    webSettings.builtInZoomControls = false
    webSettings.displayZoomControls = false
    webSettings.loadWithOverviewMode = true
    webSettings.useWideViewPort = true
    //        webSettings.setAllowFileAccessFromFileURLs(true);
//        webSettings.setAllowUniversalAccessFromFileURLs(true);
    webSettings.setSupportZoom(true)
    //        webSettings.setDatabaseEnabled(true);
    webSettings.userAgentString = System.getProperty("http.agent")
    webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
    //wv.addJavascriptInterface(new JavaScriptInterface(MainActivity.this), "Android");
    //wv.getSettings().setPluginState(WebSettings.PluginState.ON);
  }
}
