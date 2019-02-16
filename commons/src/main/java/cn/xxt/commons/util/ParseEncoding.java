package cn.xxt.commons.util;

/**
 * Created by Luke on 16/11/30.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ParseEncoding extends AbstractEncode {

    public ParseEncoding() {
    }

    @Override
    public boolean isUtf(String path) {
        return this.checkUtf(path);
    }

    @Override
    public boolean isUtf(InputStream in) {
        return this.checkUtf(in);
    }

    @Override
    public boolean isUtf(byte[] buffer) {
        return this.checkUtf(buffer);
    }

    @Override
    public boolean isUtf(URL url) {
        return this.checkUtf(url);
    }

    private boolean checkUtf(String path) {
        boolean express = true;
        if(path.startsWith("http://") || path.startsWith("https://")) {
            try {
                express = this.checkUtf(new URL(path));
            } catch (MalformedURLException var4) {
                express = true;
            }
        } else {
            express = this.checkUtf(new File(path));
        }

        return express;
    }

    public boolean checkUtf(InputStream in) {
        byte[] buffer;
        try {
            buffer = this.read(in);
        } catch (Exception var4) {
            buffer = (byte[])null;
        }

        return this.checkUtf(buffer);
    }

    public boolean checkUtf(URL url) {
        InputStream stream;
        try {
            stream = url.openStream();
        } catch (IOException var4) {
            stream = null;
        }

        return this.checkUtf(stream);
    }

    public boolean checkUtf(File file) {
        byte[] buffer;
        try {
            buffer = this.read(new FileInputStream(file));
        } catch (FileNotFoundException var4) {
            buffer = (byte[])null;
        }

        return this.checkUtf(buffer);
    }

    private final byte[] read(InputStream inputStream) {
        byte[] arrayByte = (byte[])null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1100];

        try {
//            bytes = new byte[inputStream.available()];

            int e;
            while((e = inputStream.read(bytes,0,1024)) >= 0) {
                byteArrayOutputStream.write(bytes, 0, e);
            }

            arrayByte = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return arrayByte;
        } catch (IOException var6) {
            return null;
        }
    }

    public boolean checkUtf(byte[] content) {

        boolean flag = true;
        if(content != null) {
            int utf8Score = this.utf8probability(content);
            int utf16Score = this.utf16probability(content);
            if(utf8Score <= 80 && utf16Score <= 80) {
                flag = false;
            }
        }

        return flag;
    }

    private int utf8probability(byte[] content) {
        boolean score = false;
        boolean rawtextlen = false;
        int goodbytes = 0;
        int asciibytes = 0;
        int var8 = content.length;

        for(int i = 0; i < var8; ++i) {
            if((content[i] & 127) == content[i]) {
                ++asciibytes;
            } else if(-64 <= content[i] && content[i] <= -33 && i + 1 < var8 && -128 <= content[i + 1] && content[i + 1] <= -65) {
                goodbytes += 2;
                ++i;
            } else if(-32 <= content[i] && content[i] <= -17 && i + 2 < var8 && -128 <= content[i + 1] && content[i + 1] <= -65 && -128 <= content[i + 2] && content[i + 2] <= -65) {
                goodbytes += 3;
                i += 2;
            }
        }

        if(asciibytes == var8) {
            return 0;
        } else {
            int var7 = (int)(100.0F * ((float)goodbytes / (float)(var8 - asciibytes)));
            if(var7 > 98) {
                return var7;
            } else if(var7 > 95 && goodbytes > 30) {
                return var7;
            } else {
                return 0;
            }
        }
    }

    private int utf16probability(byte[] content) {
        return (content.length <= 1 || -2 != content[0] || -1 != content[1]) && (-1 != content[0] || -2 != content[1])?0:100;
    }
}
