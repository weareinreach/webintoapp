import UIKit
import WebKit

class WebViewController: UIViewController, WKNavigationDelegate,WKUIDelegate {
    var url: URL!
	var webView: WKWebView!
	var activityIndicator: UIActivityIndicatorView!
    override func viewDidLoad() {
        super.viewDidLoad()

        let webView = WKWebView(frame: view.bounds)
        webView.navigationDelegate = self
        view.addSubview(webView)

			activityIndicator = UIActivityIndicatorView()
			activityIndicator.center = self.view.center
			activityIndicator.hidesWhenStopped = true
			activityIndicator.style = UIActivityIndicatorView.Style.large
			view.addSubview(activityIndicator)
			
			
        let closeButton = UIBarButtonItem(barButtonSystemItem: .done, target: self, action: #selector(closeButtonTapped))
        let openInBrowser = UIBarButtonItem(barButtonSystemItem: .action, target: self, action: #selector(openInBrowser))
        navigationItem.leftBarButtonItem = closeButton
        navigationItem.rightBarButtonItem = openInBrowser

        let request = URLRequest(url: url)
        webView.load(request)
    }

    @objc private func closeButtonTapped() {
        dismiss(animated: true, completion: nil)
    }
    @objc private func openInBrowser() {
      if (url != nil) {
        UIApplication.shared.open(url, options: [:], completionHandler: nil)
      }
    }
	func showActivityIndicator(show: Bool) {
		if show {
			activityIndicator.startAnimating()
		} else {
			activityIndicator.stopAnimating()
		}
	}
	
	func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
		showActivityIndicator(show: false)
	}
	
	func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
		showActivityIndicator(show: true)
	}

	func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
		showActivityIndicator(show: false)
	}
}
