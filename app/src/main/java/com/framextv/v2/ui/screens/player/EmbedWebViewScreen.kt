package com.framextv.v2.ui.screens.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EmbedWebViewScreen(
    embedUrl: String,
    title: String,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val webViewRef = remember { arrayOfNulls<WebView>(1) }
    val guardHandler = remember { Handler(Looper.getMainLooper()) }

    DisposableEffect(Unit) {
        val reinject = object : Runnable {
            override fun run() {
                injectEmbedGuards(webViewRef[0])
                guardHandler.postDelayed(this, 500L)
            }
        }
        guardHandler.postDelayed(reinject, 300L)
        onDispose {
            guardHandler.removeCallbacksAndMessages(null)
            webViewRef[0]?.apply {
                stopLoading()
                loadUrl("about:blank")
                removeAllViews()
                destroy()
            }
            webViewRef[0] = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewRef[0] = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    setBackgroundColor(AndroidColor.BLACK)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(true)
                    settings.userAgentString =
                        settings.userAgentString + " frameX-TV-Embed/1.0"
                    webChromeClient = object : WebChromeClient() {
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?,
                        ): Boolean = false
                    }
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean {
                            val url = request?.url?.toString().orEmpty()
                            if (url.isBlank()) return true
                            // Only block top-level navigations off the embed onto known bad hosts.
                            if (request?.isForMainFrame == true && isBlockedAdHost(hostOf(url).orEmpty())) {
                                return true
                            }
                            if (request?.isForMainFrame == true && !isAllowedNavigation(url, embedUrl)) {
                                return true
                            }
                            return false
                        }

                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): WebResourceResponse? {
                            val host = request?.url?.host?.lowercase().orEmpty()
                            if (host.isNotEmpty() && isBlockedAdHost(host)) {
                                return emptyResponse()
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            injectEmbedGuards(view)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            injectEmbedGuards(view)
                        }
                    }
                    isFocusable = true
                    isFocusableInTouchMode = true
                    loadUrl(embedUrl)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (view.url != embedUrl && !view.url.isNullOrBlank() && view.url != "about:blank") {
                    if (!isAllowedNavigation(view.url.orEmpty(), embedUrl)) {
                        view.loadUrl(embedUrl)
                    }
                } else if (view.url.isNullOrBlank() || view.url == "about:blank") {
                    view.loadUrl(embedUrl)
                }
            },
        )
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 24.dp, top = 16.dp),
        )
    }
}

private fun injectEmbedGuards(view: WebView?) {
    view ?: return
    view.evaluateJavascript(EMBED_GUARD_JS, null)
}

private fun emptyResponse(): WebResourceResponse =
    WebResourceResponse(
        "text/plain",
        "utf-8",
        ByteArrayInputStream(ByteArray(0)),
    )

private fun isAllowedNavigation(url: String, embedUrl: String): Boolean {
    val host = hostOf(url) ?: return false
    if (isBlockedAdHost(host)) return false
    val embedHost = hostOf(embedUrl)
    if (embedHost != null && (host == embedHost || host.endsWith(".$embedHost"))) return true
    return ALLOWED_HOST_SUFFIXES.any { suffix -> host == suffix || host.endsWith(".$suffix") }
}

private fun isBlockedAdHost(host: String): Boolean {
    if (host.isEmpty()) return false
    return BLOCKED_AD_HOST_SUFFIXES.any { suffix -> host == suffix || host.endsWith(".$suffix") }
}

private fun hostOf(url: String): String? = runCatching {
    android.net.Uri.parse(url).host?.lowercase()
}.getOrNull()

private val ALLOWED_HOST_SUFFIXES = listOf(
    "atlantic.st",
    "atlantic.bz",
    "hls.lol",
    "themoviedb.org",
    "tmdb.org",
    "googleapis.com",
    "gstatic.com",
    "cloudflare.com",
    "cloudflareinsights.com",
)

private val BLOCKED_AD_HOST_SUFFIXES = listOf(
    "justice.hls.lol",
    "monetag.com",
    "profiton",
    "popads.net",
    "popcash.net",
    "propellerads.com",
    "adsterra.com",
    "juicyads.com",
    "exoclick.com",
    "doubleclick.net",
    "googlesyndication.com",
    "googleadservices.com",
    "adservice.google.com",
    "pagead2.googlesyndication.com",
    "adnxs.com",
    "outbrain.com",
    "taboola.com",
    "criteo.com",
    "moatads.com",
    "scorecardresearch.com",
    "hcaptcha.com",
    "recaptcha.net",
    "challenges.cloudflare.com",
    "funcaptcha.com",
    "arkoselabs.com",
    "geetest.com",
    "captcha-delivery.com",
)

/**
 * Keep playback working; aggressively auto-close "not a robot" QR modals (click X, then hide).
 * Only removes covering foreign iframes after video is already playing.
 */
private const val EMBED_GUARD_JS = """
(function() {
  try {
    window.open = function() { return null; };
    window.alert = function() {};
    window.confirm = function() { return false; };
    window.prompt = function() { return null; };
  } catch (e) {}

  if (!window.__framexClickGuard) {
    window.__framexClickGuard = true;
    document.addEventListener('click', function(e) {
      var a = e.target && e.target.closest ? e.target.closest('a[target="_blank"]') : null;
      if (a) { e.preventDefault(); e.stopPropagation(); }
    }, true);
  }

  var CAPTCHA_RE = /not a robot|human verification|verify you are human|scan.{0,24}qr|bot check|prove you are/i;

  function textOf(el) {
    try { return (el.innerText || el.textContent || '').replace(/\s+/g, ' ').slice(0, 900); }
    catch (e) { return ''; }
  }

  function kill(el) {
    if (!el) return;
    try {
      el.style.setProperty('display', 'none', 'important');
      el.style.setProperty('visibility', 'hidden', 'important');
      el.style.setProperty('pointer-events', 'none', 'important');
      el.style.setProperty('opacity', '0', 'important');
      el.setAttribute('aria-hidden', 'true');
      if (el.parentNode) el.parentNode.removeChild(el);
    } catch (e) { try { el.remove(); } catch (e2) {} }
  }

  function dispatchClick(el) {
    if (!el) return;
    try { el.click(); } catch (e) {}
    try {
      var opts = { bubbles: true, cancelable: true, view: window };
      el.dispatchEvent(new MouseEvent('mousedown', opts));
      el.dispatchEvent(new MouseEvent('mouseup', opts));
      el.dispatchEvent(new MouseEvent('click', opts));
    } catch (e2) {}
  }

  function clickClosers(root) {
    if (!root || !root.querySelectorAll) return;
    try {
      var nodes = root.querySelectorAll('button, a, div, span, i, svg, path, img');
      for (var i = 0; i < nodes.length; i++) {
        var n = nodes[i];
        var label = (
          ((n.getAttribute && (n.getAttribute('aria-label') || n.getAttribute('title') || n.getAttribute('alt'))) || '') +
          ' ' + (n.className || '') + ' ' + (n.id || '')
        );
        var t = (n.innerText || n.textContent || '').trim();
        if (/close|dismiss|fermer|cerrar/i.test(label) ||
            t === '×' || t === 'x' || t === 'X' || t === '✕' || t === '✖' || t === '⨯') {
          dispatchClick(n);
        }
      }
    } catch (e) {}
  }

  function clickTopRight(card) {
    try {
      var r = card.getBoundingClientRect();
      if (r.width < 80 || r.height < 80) return;
      var pts = [
        [r.right - 18, r.top + 18],
        [r.right - 30, r.top + 24],
        [r.right - 12, r.top + 12],
        [r.right - 44, r.top + 20],
        [r.right - 24, r.top + 36]
      ];
      for (var i = 0; i < pts.length; i++) {
        var hit = document.elementFromPoint(pts[i][0], pts[i][1]);
        if (hit) dispatchClick(hit);
      }
    } catch (e) {}
  }

  function findRobotCards() {
    var found = [];
    var nodes = document.querySelectorAll('div, section, aside, dialog, article, span, p, h1, h2, h3, h4');
    for (var i = 0; i < nodes.length; i++) {
      var el = nodes[i];
      var t = textOf(el);
      if (!CAPTCHA_RE.test(t)) continue;
      // Prefer a medium-sized card ancestor (the white modal), not the whole body.
      var card = el;
      for (var up = 0; up < 10 && card && card.parentElement && card.parentElement !== document.body; up++) {
        var r = card.getBoundingClientRect();
        if (r.width >= 220 && r.height >= 220 && r.width < (window.innerWidth || 9999) * 0.95) break;
        card = card.parentElement;
      }
      if (card && found.indexOf(card) < 0) found.push(card);
    }
    return found;
  }

  function videoIsPlaying() {
    try {
      var videos = document.querySelectorAll('video');
      for (var i = 0; i < videos.length; i++) {
        var v = videos[i];
        if (v && !v.paused && v.readyState >= 2) return true;
        if (v && v.currentTime > 0.2) return true;
      }
    } catch (e) {}
    return false;
  }

  function autoCloseCaptcha() {
    try {
      // Known captcha iframe srcs
      var iframes = document.querySelectorAll('iframe');
      for (var i = 0; i < iframes.length; i++) {
        var iframe = iframes[i];
        var src = (iframe.getAttribute('src') || iframe.src || '').toString();
        if (/captcha|hcaptcha|recaptcha|turnstile|challenge|robot|monetag|popads|propeller/i.test(src)) {
          kill(iframe);
        }
        // Same-origin iframe: run close inside it
        try {
          var idoc = iframe.contentDocument || (iframe.contentWindow && iframe.contentWindow.document);
          if (idoc) {
            autoCloseInDoc(idoc);
          }
        } catch (e) {}
      }

      var cards = findRobotCards();
      for (var c = 0; c < cards.length; c++) {
        var card = cards[c];
        clickClosers(card);
        clickTopRight(card);
        // Always hide the modal after attempting close — user asked to auto-close.
        kill(card);
      }

      // If video is already playing, any large foreign center iframe on top is almost certainly captcha.
      if (videoIsPlaying()) {
        var vw = window.innerWidth || 1;
        var vh = window.innerHeight || 1;
        for (var j = 0; j < iframes.length; j++) {
          var frame = iframes[j];
          if (!frame || !frame.isConnected) continue;
          var fsrc = (frame.getAttribute('src') || frame.src || '').toString();
          if (/atlantic\.(st|bz)|blob:|about:blank/i.test(fsrc) && !/captcha|robot|challenge/i.test(fsrc)) continue;
          var rect = frame.getBoundingClientRect();
          if (rect.width > vw * 0.28 && rect.height > vh * 0.28) {
            var st = window.getComputedStyle ? getComputedStyle(frame) : null;
            var z = st ? parseInt(st.zIndex, 10) : 0;
            if ((st && (st.position === 'fixed' || st.position === 'absolute')) || (!isNaN(z) && z >= 5)) {
              kill(frame);
            } else if (rect.width > vw * 0.4 && rect.height > vh * 0.4) {
              // Large centered frame above playing video — close it.
              var cx = rect.left + rect.width / 2;
              var cy = rect.top + rect.height / 2;
              if (cx > vw * 0.2 && cx < vw * 0.8 && cy > vh * 0.15 && cy < vh * 0.85) {
                kill(frame);
              }
            }
          }
        }
      }
    } catch (e) {}
  }

  function autoCloseInDoc(doc) {
    try {
      var nodes = doc.querySelectorAll('div, section, aside, dialog, article, span, p, h1, h2, h3');
      for (var i = 0; i < nodes.length; i++) {
        var el = nodes[i];
        var t = '';
        try { t = (el.innerText || el.textContent || ''); } catch (e) {}
        if (!CAPTCHA_RE.test(t)) continue;
        clickClosers(el);
        try {
          var r = el.getBoundingClientRect();
          var hit = doc.elementFromPoint(r.right - 18, r.top + 18);
          if (hit) dispatchClick(hit);
        } catch (e2) {}
        kill(el);
      }
    } catch (e) {}
  }

  function tryStartPlayback() {
    try {
      var videos = document.querySelectorAll('video');
      for (var i = 0; i < videos.length; i++) {
        var v = videos[i];
        try {
          var p = v.play();
          if (p && p.catch) p.catch(function(){});
        } catch (e) {}
      }
      var playSel = [
        'button.vjs-big-play-button', '.vjs-big-play-button',
        'button[aria-label*="Play"]', 'button[aria-label*="play"]',
        '.jw-icon-display', '.jw-display-icon-container',
        '.plyr__control--overlaid', '[class*="big-play"]'
      ];
      for (var s = 0; s < playSel.length; s++) {
        try {
          var nodes = document.querySelectorAll(playSel[s]);
          for (var n = 0; n < nodes.length; n++) dispatchClick(nodes[n]);
        } catch (e3) {}
      }
    } catch (e) {}
  }

  autoCloseCaptcha();
  tryStartPlayback();

  if (!window.__framexOverlayObs) {
    try {
      window.__framexOverlayObs = new MutationObserver(function() { autoCloseCaptcha(); });
      window.__framexOverlayObs.observe(document.documentElement || document.body, { childList: true, subtree: true });
    } catch (e) {}
  }
  if (!window.__framexCloseTimer) {
    window.__framexCloseTimer = setInterval(function() {
      autoCloseCaptcha();
      tryStartPlayback();
    }, 400);
  }
})();
"""
