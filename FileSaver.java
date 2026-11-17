package APP_ID_PLACEHOLDER;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FileSaver {
    private final Activity act;

    public FileSaver(Activity a) {
        act = a;
    }

    @JavascriptInterface
    public void save(String b64, String name, String mime) {
        try {
            saveBytes(Base64.decode(b64, Base64.DEFAULT), name, mime);
        } catch (Exception e) {
            toast("Gagal menyimpan file");
        }
    }

    @JavascriptInterface
    public void fail() {
        toast("Gagal mengunduh file");
    }

    public void toast(final String msg) {
        act.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(act, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static String clean(String name) {
        if (name == null || name.length() == 0) {
            name = "file";
        }
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (name.length() > 120) {
            name = name.substring(name.length() - 120);
        }
        return name;
    }

    public void saveBytes(byte[] data, String rawName, String mime) throws Exception {
        String name = clean(rawName);
        if (mime == null || mime.length() == 0) {
            mime = "application/octet-stream";
        }
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues v = new ContentValues();
            v.put(MediaStore.Downloads.DISPLAY_NAME, name);
            v.put(MediaStore.Downloads.MIME_TYPE, mime);
            v.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = act.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
            OutputStream os = act.getContentResolver().openOutputStream(uri);
            os.write(data);
            os.close();
            toast("Tersimpan di folder Download: " + name);
        } else {
            File dir = act.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            dir.mkdirs();
            FileOutputStream os = new FileOutputStream(new File(dir, name));
            os.write(data);
            os.close();
            toast("Tersimpan di Android/data: " + name);
        }
    }
}
