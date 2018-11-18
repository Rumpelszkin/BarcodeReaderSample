package com.varvet.barcodereadersample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class Main2Activity extends AppCompatActivity {


    public final static int WHITE = 0xFFFFFFFF;
    public final static int BLACK = 0xFF000000;
    public final static int WIDTH = 400;
    public final static int HEIGHT = 400;
    public final static String STR = "HEHEHEHE DUPA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);



// Don't permit screenshots since it contains the secret key
      //  getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        Intent i = getIntent();


        TimeBasedOneTimePasswordGenerator totp = null;
        try {
            totp = new TimeBasedOneTimePasswordGenerator();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        final SecretKey secretKey;
        {
            KeyGenerator keyGenerator = null;//timeBasedOneTimePasswordGenerator.getAlgorithm());
            try {
                keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
  /*          try {
                keyGenerator = KeyGenerator.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }*/
            keyGenerator.init(512);
            secretKey = keyGenerator.generateKey();
        }
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        byte[] encoded = secretKey.getEncoded();

        String s = new String(encoded);
       // System.out.println("Text Decryted : " + s);


        //String text = i.getStringExtra ("TextBox");




        ImageView imageView = (ImageView) findViewById(R.id.myImage);
        try{
            Bitmap bitmap = encodeAsBitmap(encodedKey);//ByteArrayToBitmap(encoded);
            imageView.setImageBitmap(bitmap);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public Bitmap ByteArrayToBitmap(byte[] byteArray)
    {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(byteArray);
        Bitmap bitmap = BitmapFactory.decodeStream(arrayInputStream);
        return bitmap;
    }


    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, HEIGHT, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }

        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}

