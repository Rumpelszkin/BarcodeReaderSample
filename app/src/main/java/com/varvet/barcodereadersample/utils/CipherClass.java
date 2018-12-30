package com.varvet.barcodereadersample.utils;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CipherClass {
    File file;


    public CipherClass(Uri uri){
        file = new File(uri.getPath());
        Log.d("cipher1","elo");

    }

    public String testBytes() throws IOException {
        Log.d("cipher1","elow");
        byte[] bytes = new byte[(int) file.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        DataInputStream dos = new DataInputStream(bis);

        String s=null;


        Log.d("cipher1","eloe");
        return s;
    }

    public boolean encrypt(){


        return false;
    }

    public boolean decrypt(){


        return false;
    }
}
