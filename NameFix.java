package APP_ID_PLACEHOLDER;

import android.webkit.MimeTypeMap;

public class NameFix {
    public static String clean(String name) {
        if (name == null || name.length() == 0) {
            name = "file";
        }
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.length() > 120) {
            name = name.substring(name.length() - 120);
        }
        return name;
    }

    static String extOf(String n) {
        int dot = n.lastIndexOf('.');
        if (dot > 0 && dot < n.length() - 1) {
            return n.substring(dot + 1).toLowerCase();
        }
        return "";
    }

    static String withExt(String n, String e) {
        String old = extOf(n);
        if (old.equals("bin")) {
            n = n.substring(0, n.lastIndexOf('.'));
        }
        return n + "." + e;
    }

    public static String fixName(String name, String mime) {
        String n = clean(name);
        String ext = extOf(n);
        if ((ext.length() == 0 || ext.equals("bin")) && mime != null) {
            String m = mime.toLowerCase();
            int sc = m.indexOf(';');
            if (sc >= 0) {
                m = m.substring(0, sc).trim();
            }
            String e = null;
            if (m.equals("text/markdown") || m.equals("text/x-markdown")) {
                e = "md";
            } else if (!m.equals("application/octet-stream")) {
                e = MimeTypeMap.getSingleton().getExtensionFromMimeType(m);
            }
            if (e != null) {
                n = withExt(n, e);
            }
        }
        return n;
    }

    static boolean has(byte[] d, String s) {
        int len = Math.min(d.length, 6000);
        String t = new String(d, 0, len, java.nio.charset.Charset.forName("ISO-8859-1"));
        return t.contains(s);
    }

    static String sniff(byte[] d) {
        if (d.length < 4) {
            return null;
        }
        if (d[0] == '%' && d[1] == 'P' && d[2] == 'D' && d[3] == 'F') {
            return "pdf";
        }
        if ((d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return "png";
        }
        if ((d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8) {
            return "jpg";
        }
        if (d[0] == 'P' && d[1] == 'K') {
            if (has(d, "word/")) {
                return "docx";
            }
            if (has(d, "xl/")) {
                return "xlsx";
            }
            if (has(d, "ppt/")) {
                return "pptx";
            }
            return "zip";
        }
        return null;
    }
}
