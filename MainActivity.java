package APP_ID_PLACEHOLDER;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private boolean ready = false;
    private FileSaver saver;

    private void setupDownloads() {
        if (ready || getBridge() == null || getBridge().getWebView() == null) {
            return;
        }
        ready = true;
        saver = new FileSaver(this);
        WebView wv = getBridge().getWebView();
        wv.addJavascriptInterface(saver, "AndroidSaver");
        wv.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String ua, String cd, String mime, long len) {
                handleDownload(url, ua, cd, mime);
            }
        });
    }

    private void handleDownload(String url, String ua, String cd, String mime) {
        String name = FileSaver.clean(URLUtil.guessFileName(url, cd, mime));
        try {
            if (url.startsWith("blob:")) {
                String js = "(function(){var x=new XMLHttpRequest();x.open('GET','"
                    + url.replace("'", "%27") + "',true);x.responseType='blob';"
                    + "x.onload=function(){var r=new FileReader();r.onloadend=function(){"
                    + "var s=r.result;AndroidSaver.save(s.substring(s.indexOf(',')+1),'"
                    + name + "','" + (mime == null ? "" : mime.replace("'", "")) + "');};"
                    + "r.readAsDataURL(x.response);};x.onerror=function(){AndroidSaver.fail();};"
                    + "x.send();})();";
                getBridge().getWebView().evaluateJavascript(js, null);
            } else if (url.startsWith("data:")) {
                int i = url.indexOf(',');
                String body = url.substring(i + 1);
                byte[] data = url.substring(0, i).contains(";base64")
                    ? Base64.decode(body, Base64.DEFAULT)
                    : Uri.decode(body).getBytes("UTF-8");
                saver.saveBytes(data, name, mime);
            } else {
                DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                req.setMimeType(mime);
                String cookie = CookieManager.getInstance().getCookie(url);
                if (cookie != null) {
                    req.addRequestHeader("Cookie", cookie);
                }
                req.addRequestHeader("User-Agent", ua);
                req.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
                ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(req);
                saver.toast("Mengunduh: " + name);
            }
        } catch (Exception e) {
            saver.toast("Gagal mengunduh file");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        if (getBridge() != null && getBridge().getWebView() != null) {
            cm.setAcceptThirdPartyCookies(getBridge().getWebView(), true);
        }
        setupDownloads();
    }

    @Override
    public void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }

    @Override
    public void onStop() {
        super.onStop();
        CookieManager.getInstance().flush();
    }
}
