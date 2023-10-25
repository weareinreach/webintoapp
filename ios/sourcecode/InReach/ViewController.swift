//
//  ViewController.swift
//
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
//
// InReach
//
// Created by InReach on 03/10/2023.
//
import UIKit
import WebKit
class FileDownloader {
	static func loadFileSync(url: URL, completion: @escaping (String?, Error?) -> Void) {
		let documentsUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
		let destinationUrl = documentsUrl.appendingPathComponent(url.lastPathComponent)
		if FileManager().fileExists(atPath: destinationUrl.path) {
			print("File already exists [\(destinationUrl.path)]")
			completion(destinationUrl.path, nil)
		}
		else if let dataFromURL = NSData(contentsOf: url) {
			if dataFromURL.write(to: destinationUrl, atomically: true) {
				print("file saved [\(destinationUrl.path)]")
				completion(destinationUrl.path, nil)
			}
			else {
				print("error saving file")
				let error = NSError(domain: "Error saving file", code: 1001, userInfo: nil)
				completion(destinationUrl.path, error)
			}
		}
		else {
			let error = NSError(domain: "Error downloading file", code: 1002, userInfo: nil)
			completion(destinationUrl.path, error)
		}
	}

	static func loadFileAsync(url: URL, completion: @escaping (String?, Error?) -> Void) {
		let documentsUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
		let destinationUrl = documentsUrl.appendingPathComponent(url.lastPathComponent)
		if FileManager().fileExists(atPath: destinationUrl.path) {
			print("File already exists [\(destinationUrl.path)]")
			completion(destinationUrl.path, nil)
		}
		else {
			let session = URLSession(configuration: URLSessionConfiguration.default, delegate: nil, delegateQueue: nil)
			var request = URLRequest(url: url)
			request.httpMethod = "GET"
			let task = session.dataTask(with: request, completionHandler: {
				data, response, error in
				if error == nil {
					if let response = response as? HTTPURLResponse {
						if response.statusCode == 200 {
							if let data = data {
								if let _ = try? data.write(to: destinationUrl, options: Data.WritingOptions.atomic) {
									completion(destinationUrl.path, error)
								}
								else {
									completion(destinationUrl.path, error)
								}
							}
							else {
								completion(destinationUrl.path, error)
							}
						}
					}
				}
				else {
					completion(destinationUrl.path, error)
				}
			})
			task.resume()
		}
	}
}

extension Dictionary {
	func percentEscaped() -> String {
		return map { key, value in
			let escapedKey = "\(key)".addingPercentEncoding(withAllowedCharacters: .urlQueryValueAllowed) ?? ""
			let escapedValue = "\(value)".addingPercentEncoding(withAllowedCharacters: .urlQueryValueAllowed) ?? ""
			return escapedKey + "=" + escapedValue
		}
		.joined(separator: "&")
	}
}

extension CharacterSet {
	static let urlQueryValueAllowed: CharacterSet = {
		let generalDelimitersToEncode = ":#[]@" // does not include "?" or "/" due to RFC 3986 - Section 3.4
		let subDelimitersToEncode = "!$&'()*+,;="
		var allowed = CharacterSet.urlQueryAllowed
		allowed.remove(charactersIn: "\(generalDelimitersToEncode)\(subDelimitersToEncode)")
		return allowed
	}()
}

extension Double {
	func removeZerosFromEnd() -> String {
		let formatter = NumberFormatter()
		let number = NSNumber(value: self)
		formatter.minimumFractionDigits = 0
		formatter.maximumFractionDigits = 16 // maximum digits in Double after dot (maximum precision)
		return String(formatter.string(from: number) ?? "")
	}
}

extension UIApplication {
	var statusBarView: UIView? {
		return value(forKey: "statusBar") as? UIView
	}
}

extension UINavigationBar {
	func customNavigationBar() {
		backgroundColor = UIColor(rgb: 0x00D56C)
		tintColor = UIColor(rgb: 0x000000)
		barTintColor = UIColor(rgb: 0x00D56C)
		isTranslucent = false
		titleTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor(rgb: 0xFFFFFF)]
//		self.setBackgroundImage(UIImage(), for: .default)
//		self.shadowImage = UIImage()
	}
}

extension UIColor {
	convenience init(red: Int, green: Int, blue: Int) {
		assert(red >= 0 && red <= 255, "Invalid red component")
		assert(green >= 0 && green <= 255, "Invalid green component")
		assert(blue >= 0 && blue <= 255, "Invalid blue component")
		self.init(red: CGFloat(red) / 255.0, green: CGFloat(green) / 255.0, blue: CGFloat(blue) / 255.0, alpha: 1.0)
	}

	convenience init(rgb: Int) {
		self.init(
			red: (rgb >> 16) & 0xFF,
			green: (rgb >> 8) & 0xFF,
			blue: rgb & 0xFF
		)
	}
}

class ViewController: UIViewController, WKNavigationDelegate, WKUIDelegate, UIDocumentInteractionControllerDelegate, UIGestureRecognizerDelegate {
	var window: UIApplication!
	var statusBar: UIView!
	override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
		print("-viewWillTransition")
		super.viewWillTransition(to: size, with: coordinator)
		if UIDevice.current.orientation.isLandscape {
			print("Landscape")
			statusBar.isHidden = true
		}
		else {
			print("Portrait")
			statusBar.isHidden = false
		}
	}

	var webView: WKWebView!
	var webViewSplashScreen: WKWebView!
	var useSplashScreen: Bool! = true
	var loadingError: Bool! = false
	let refreshControl = UIRefreshControl()
	var handledURLs: Set<URL> = []
	var isContainerOpened: Bool = false

	let allyGreen = 0x00D56C

	func isDonationURL(urlString: String) -> Bool {
		if #available(iOS 16.0, *) {
			if let url = URL(string: urlString), let host = url.host() {
				return host.hasSuffix("kindful.com")
			}
		}
		else {
			if let url = URL(string: urlString), let host = url.host {
				return host.hasSuffix("kindful.com")
			}
		}
		return false
	}

	@objc func reloadWebView(_ sender: UIRefreshControl) {
		print("-reloadWebView")
		if loadingError == true {
			print("reloading after error")
			loadingError = false
			LoadWebView()
		}
		else {
			print("normal reload")
			webView.reload()
		}
		refreshControl.endRefreshing()
	}

	let progressView = UIProgressView(progressViewStyle: .default)
	private var estimatedProgressObserver: NSKeyValueObservation?
	override var preferredStatusBarStyle: UIStatusBarStyle {
		return .lightContent
	}

	func application(_ application: UIApplication,
	                 continue userActivity: NSUserActivity,
	                 restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool
	{
		guard userActivity.activityType == NSUserActivityTypeBrowsingWeb,
		      let incomingURL = userActivity.webpageURL,
		      let components = NSURLComponents(url: incomingURL, resolvingAgainstBaseURL: true)
		else {
			return false
		}
		guard let path = components.path,
		      let params = components.queryItems
		else {
			return false
		}
		print("path = \(path)")
		if let albumName = params.first(where: { $0.name == "albumname" })?.value,
		   let photoIndex = params.first(where: { $0.name == "index" })?.value
		{
			print("album = \(albumName)")
			print("photoIndex = \(photoIndex)")
			return true
		}
		else {
			print("Either album name or photo index missing")
			return false
		}
	}

	override func loadView() {
		print("-loadView")
		super.loadView()
		navigationController?.navigationBar.barStyle = .black
		if #available(iOS 13.0, *) {
			let window = UIApplication.shared.windows.filter { $0.isKeyWindow }.first
			statusBar = UIView(frame: window?.windowScene?.statusBarManager?.statusBarFrame ?? CGRect.zero)
			statusBar.backgroundColor = UIColor(rgb: allyGreen)
			window?.addSubview(statusBar)
		}
		else {
			UIApplication.shared.statusBarView?.backgroundColor = UIColor(rgb: allyGreen)
			UIApplication.shared.statusBarStyle = .lightContent
		}
		let refresh = UIBarButtonItem(barButtonSystemItem: .refresh, target: webView, action: #selector(reloadWebView))
		let back = UIBarButtonItem(title: "Back", style: .plain, target: webView, action: #selector(self.webView!.goBack))
		self.navigationItem.rightBarButtonItem = refresh
		self.navigationItem.leftBarButtonItem = back
		self.navigationItem.title = "InReach"
		navigationController?.isNavigationBarHidden = true
		statusBar.isHidden = false

		let webView = WKWebView(frame: view.frame)
		view.addSubview(webView)
		webView.translatesAutoresizingMaskIntoConstraints = false
		NSLayoutConstraint.activate([
			webView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
			webView.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor),
			webView.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor),
			webView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor)
		])
		self.webView = webView
		webViewSplashScreen = WKWebView(frame: view.frame)

		if #available(iOS 16.4,*) {
			self.webView.isInspectable = true
		}
	}

	@objc func backNavigationFunction(_ sender: UIScreenEdgePanGestureRecognizer) {
		let dX = sender.translation(in: view).x
		if sender.state == .ended {
			let fraction = abs(dX / view.bounds.width)
			if fraction >= 0.35 {}
		}
	}

	override func viewDidLoad() {
		print("-viewDidLoad")
		super.viewDidLoad()
		let swipeGesture = UIScreenEdgePanGestureRecognizer(target: self, action: #selector(backNavigationFunction(_:)))
		swipeGesture.edges = .left
		swipeGesture.delegate = self
		view.addGestureRecognizer(swipeGesture)
		if useSplashScreen == true {
			print("Load with splash screen")
			view.addSubview(webViewSplashScreen)
			let localFilePath = Bundle.main.url(forResource: "loading", withExtension: "html", subdirectory: "htmlapp/helpers")
			let request = URLRequest(url: localFilePath!)
			webViewSplashScreen.navigationDelegate = self
			webViewSplashScreen.uiDelegate = self
			webViewSplashScreen.load(request as URLRequest)
		}
		else {
			print("Load without splash screen")
			LoadWebView()
		}
		NotificationCenter.default.addObserver(self, selector: #selector(applicationDidBecomeActive), name: UIApplication.didBecomeActiveNotification, object: nil)
	}

	@objc func applicationDidBecomeActive(notification: NSNotification) {
		print("-applicationDidBecomeActive")
		if let run_first_exists = UserDefaults.standard.object(forKey: "first_run") {
			print("first_run is: \(run_first_exists)")
		}
		else {}
	}

	private func setupProgressView() {
//		guard let navigationBar = navigationController?.navigationBar else { return }
		progressView.translatesAutoresizingMaskIntoConstraints = false
		progressView.trackTintColor = UIColor(rgb: allyGreen)
//		navigationBar.addSubview(progressView)
		statusBar.addSubview(progressView)
		progressView.isHidden = true
		NSLayoutConstraint.activate([
			progressView.leadingAnchor.constraint(equalTo: statusBar.leadingAnchor),
			progressView.trailingAnchor.constraint(equalTo: statusBar.trailingAnchor),
			progressView.bottomAnchor.constraint(equalTo: statusBar.bottomAnchor),
			progressView.heightAnchor.constraint(equalToConstant: 2.0)
		])
	}

	private func setupEstimatedProgressObserver() {
		estimatedProgressObserver = webView.observe(\.estimatedProgress, options: [.new]) { [weak self] webView, _ in
			self?.progressView.progress = Float(webView.estimatedProgress)
		}
	}

	private func setupWebview(url: URL) {
		print("-setupWebview")
		let request = URLRequest(url: url)
		webView.navigationDelegate = self
		webView.load(request)
	}

	@objc func LoadWebView() {
		print("-LoadWebView")
//		setupProgressView()
//		setupEstimatedProgressObserver()
		refreshControl.addTarget(self, action: #selector(reloadWebView(_:)), for: .valueChanged)
		webView.scrollView.addSubview(refreshControl)
		webView.navigationDelegate = self
		webView.uiDelegate = self

		let source = "var meta = document.createElement('meta');" +
			"meta.name = 'viewport';" +
			"meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';" +
			"var head = document.getElementsByTagName('head')[0];" +
			"head.appendChild(meta);"
		let script = WKUserScript(source: source, injectionTime: .atDocumentEnd, forMainFrameOnly: true)
		webView.configuration.userContentController.addUserScript(script)
		let url    = URL(string: "https://app.inreach.org?isMobileApp=true")!
//		let url = URL(string: "http://localhost:3000?isMobileApp=true")!
		let request = URLRequest(url: url)
		print("loading initial request ->", request)
		webView.load(request)
	}

	func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
		if progressView.isHidden {
			print("unhiding progressView")
			progressView.isHidden = false
		}
		UIView.animate(withDuration: 0.33,
		               animations: {
		               	self.progressView.alpha = 1.0
		               })

		print("didStartProvisionalNavigation - webView.url: \(String(describing: webView.url?.description)) x \(String(describing: webView.url?.host))")
	}

	func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
		let nserror = error as NSError
		if nserror.code != NSURLErrorCancelled {
			let localFilePath = Bundle.main.url(forResource: "error", withExtension: "html", subdirectory: "htmlapp/helpers")
			let request = NSURLRequest(url: localFilePath!)
			webView.load(request as URLRequest)
			loadingError = true
			print("webView error:", nserror)
		}
	}

	func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
		print("New URL: \(String(describing: navigationAction.request.url))")

		if let frame = navigationAction.targetFrame,
		   frame.isMainFrame
		{
			print("isMainFrame true")
			return nil
		}
		if let url = navigationAction.request.url {
			let shared = UIApplication.shared
			if shared.canOpenURL(url) {
				if isDonationURL(urlString: url.absoluteString) {
					print("Opening donation url in Safari")
					shared.open(url)
					return nil
				}

				print("Opening web container")

				let webViewController = WebViewController()
				webViewController.url = url

				let navigationController = UINavigationController(rootViewController: webViewController)
				navigationController.modalPresentationStyle = .pageSheet

				present(navigationController, animated: true, completion: nil)
				return nil
			}
		}
		print("Loading", navigationAction.request)
		webView.load(navigationAction.request)
		return nil
	}

	func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
		if webView == webViewSplashScreen {
			print("webViewSplashScreen didFinish")
			LoadWebView()
		}
		if webView == self.webView {
			print("webView didFinish")
//			view = webView
			view.addSubview(webView)
			UIView.animate(withDuration: 0.33,
			               animations: {
			               	self.progressView.alpha = 0.0
			               },
			               completion: { isFinished in
			               	self.progressView.isHidden = isFinished
			               })
		}
	}

	func documentInteractionControllerViewControllerForPreview(_ controller: UIDocumentInteractionController) -> UIViewController {
		UINavigationBar.appearance().barTintColor = UIColor(rgb: 0x79A5ED)
		UINavigationBar.appearance().tintColor = UIColor(rgb: 0x79A5ED)
		UINavigationBar.appearance().titleTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor(rgb: 0x79A5ED), NSAttributedString.Key.font: UIFont.systemFont(ofSize: 14, weight: UIFont.Weight.bold)]
		return self
	}

	func documentInteractionControllerViewForPreview(_ controller: UIDocumentInteractionController) -> UIView? {
		return view
	}

	func documentInteractionControllerRectForPreview(_ controller: UIDocumentInteractionController) -> CGRect {
		return view.frame
	}

	func showFileWithPath(path: String) {
		let isFileFound: Bool? = FileManager.default.fileExists(atPath: path)
		if isFileFound == true {
			let viewer = UIDocumentInteractionController(url: URL(fileURLWithPath: path))
			viewer.delegate = self
			viewer.presentPreview(animated: true)
		}
	}

	func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
		guard let url = navigationAction.request.url else {
			decisionHandler(.allow)
			return
		}
		if handledURLs.contains(url) {
			decisionHandler(.allow)
			return
		}
		let fileextension = url.pathExtension

		/*		if let host = url.host, !host.contains("app.inreach.org") {
		 	print("Opening web container")
		 	decisionHandler(.cancel)

		 	let webViewController = WebViewController()
		 	webViewController.url = url

		 	let navigationController = UINavigationController(rootViewController: webViewController)
		 	navigationController.modalPresentationStyle = .formSheet

		 	present(navigationController, animated: true, completion: nil)
		 	handledURLs.insert(url)
		 }
		 */
		if ["zip", "7g", "pdf"].contains(fileextension) {
			print("decisionHandler cancel")
			decisionHandler(.cancel)
			let url = URL(string: url.absoluteURL.absoluteString)
			FileDownloader.loadFileAsync(url: url!) { path, _ in
				print("File downloaded to : \(path!)")
				DispatchQueue.main.async { () in
					self.showFileWithPath(path: path!)
				}
			}
		}
		else if ["tel", "sms", "facetime", "mailto", "whatsapp", "twitter", "twitterauth", "fb", "fbapi", "fbauth2", "fbshareextension", "fb-messenger-api", "viber", "wechat", "line", "instagram", "instagram-stories", "googlephotos"].contains(url.scheme) {
			if UIApplication.shared.canOpenURL(url) {
				UIApplication.shared.open(url)
			}
			else {
				print("Can't open url on this device")
			}
			print("decisionHandler cancel")
			decisionHandler(.cancel)
		}
		else {
			decisionHandler(.allow)
		}
	}
	/*	func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
	 guard let url = navigationAction.request.url else {
	 decisionHandler(.allow)
	 return
	 }

	 // Check if the URL has already been handled
	 if isContainerOpened {
	 decisionHandler(.allow)
	 return
	 }

	 // Check if the URL is from outside the "app.inreach.org" domain
	 if let host = url.host, !host.contains("app.inreach.org") {
	 decisionHandler(.cancel)

	 let webViewController = WebViewController()
	 webViewController.url = url

	 let navigationController = UINavigationController(rootViewController: webViewController)
	 navigationController.modalPresentationStyle = .formSheet

	 present(navigationController, animated: true) {
	 self.isContainerOpened = true
	 }
	 } else {
	 decisionHandler(.allow)
	 }
	 }
	 */
}
