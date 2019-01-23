package com.varvet.barcodereadersample.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.varvet.barcodereadersample.MainActivity;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CipherClass {
    File file;
    String filePath;
    String key;
    int tryb;
    Context context;

    public CipherClass(Uri uri,Context context, String key, int tryb) throws URISyntaxException {
        //file = new File(PathUtil.getPath(context, uri));
        //file = new File(uri.getPath());
/*
        Log.d("cipher1",uri.getPath());
        file = new File(uri.getPath());
        final String[] split = file.getPath().split(":");
        filePath = split[0] ;
*/      this.key = key;
        this.tryb = tryb;
        readFromUri(uri, context);
        this.context=context;


        Log.d("cipher1","elo");

    }



    public byte[] readFromUri(Uri uri,Context context) throws URISyntaxException {
        String filename;
        String mimeType =  context.getContentResolver().getType(uri);

        if(mimeType == null){
            String path = PathUtil.getPath(context,uri);
            if(path == null){
                filename = uri.toString();
            } else{
                File file = new File(path);
                filename = file.getName();
            }
        } else {
            Uri returnUri = uri;
            Cursor returnCursor = context.getContentResolver().query(returnUri,null,null,null,null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            filename = returnCursor.getString(nameIndex);
        }

         //String sourcePath = context.getExternalFilesDir(null).toString();
        String sourcePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        Log.d("testPathInfo", sourcePath);

        try{

            byte[] tekstDoObrobki = copyFileStream(new File(sourcePath+filename),uri,context);

            if(tryb == 1){
                encrypt(tekstDoObrobki,new File(sourcePath+"/zaszyfrowane"+filename));
                Toast.makeText(context,"Zaszyfrowano: "+ filename,Toast.LENGTH_LONG).show();
            }else{
                decrypt(tekstDoObrobki,new File(sourcePath+"/odszfrowane"+filename));
                Toast.makeText(context,"Odszyfrowano: "+ filename,Toast.LENGTH_LONG).show();
            }

            return tekstDoObrobki;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private byte[] copyFileStream(File dest, Uri uri, Context context) throws IOException {
        InputStream is = null;
      //  OutputStream os = null;
        byte [] bajtaraj = null;

        try{
            is = context.getContentResolver().openInputStream(uri);
         //   os = new FileOutputStream(dest);
            bajtaraj = new byte[is.available()];
            is.read(bajtaraj);
          // os.write(bajtaraj)z

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();

            return bajtaraj;
        }

    }


    public byte[] testBytes() throws IOException {
        Log.d("cipher1","elow");
        byte[] bytes = new byte[(int) file.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
        DataInputStream dos = new DataInputStream(bis);

        String s=null;


        Log.d("cipher1","eloe");
        return bytes;
    }

    public boolean encrypt(byte[] text,File file) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
        OutputStream outputStream = null;
        Cipher cipher = Cipher.getInstance("AES");
        TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
        byte[] decodedKey = Base64.getDecoder().decode(key);
// rebuild key using SecretKeySpec
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length,"AES");// totp.getAlgorithm());


       Log.e("testKey",new String(decodedKey));

        Calendar cal = Calendar.getInstance();//data to token
        Date act = cal.getTime();
        long transformedDate = act.getTime();//date to byte array
        byte[] timeStampBytes= longToBytes(transformedDate);

        int token = totp.generateOneTimePassword(originalKey,act);
        String xx =  Integer.toString(token);
        Log.e("length", Integer.toString(text.length));

        byte[] byteToken = xx.getBytes();

        for(int i = 0; i< byteToken.length;i++){
            decodedKey[i]=byteToken[i];
        }
        SecretKey tempKey = new SecretKeySpec(decodedKey,0,decodedKey.length,totp.getAlgorithm());

        cipher.init(Cipher.ENCRYPT_MODE,tempKey);
        byte[] cipheredText = cipher.doFinal(text);
           Log.e("key",new String(decodedKey));

        Log.e("keyle",Integer.toString(cipheredText.length));

        Log.e("keyletext",Integer.toString(text.length));
        byte[] finalCipheredText = new byte[cipheredText.length+timeStampBytes.length];

        for(int i = 0; i < timeStampBytes.length;i++  ){
            finalCipheredText[i] = timeStampBytes[i];
        }

        for(int i = timeStampBytes.length; i<finalCipheredText.length;i++){
            finalCipheredText[i]=cipheredText[i-timeStampBytes.length];
        }

  //      Cipher c2 = Cipher.getInstance("AES");
    //    c2.init(Cipher.DECRYPT_MODE,tempKey);
    //    byte[] dectypted = c2.doFinal(cipheredText);
       // outputStream.write(finalCipheredText);
            outputStream = new FileOutputStream(file);
            outputStream.write(finalCipheredText);


        Log.e("testEncryptedText",new String(finalCipheredText));


        //Toast.makeText(context,"Szyfrowanie zakończone powodzeniem.",Toast.LENGTH_LONG).show();
        outputStream.close();
        //ApplicationContext.showToast("Szyfrowanie zakończone powodzeniem!");


        return false;
    }

    public boolean decrypt(byte[] text,File file) throws Exception{
        OutputStream outputStream = null;
        if((text.length-8)%16!=0) {
            MainActivity.Companion.setTryb(-1);
            return false;
        }
        Cipher cipher = Cipher.getInstance("AES");
        TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
        byte[] decodedKey = Base64.getDecoder().decode(key);

// rebuild key using SecretKeySpec
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");//totp.getAlgorithm());

        byte[] cipherTime = new byte[8];

        for (int i= 0; i< 8;i++){
            cipherTime[i] = text[i];
        }
        long cipherDateLong = bytesToLong(cipherTime);
        //Date cipherDate = new Date(0);//
        Date cipherDate = new Date(cipherDateLong);

        int token = totp.generateOneTimePassword(originalKey,cipherDate);
        String xx =  Integer.toString(token);
        Log.e("token", xx);

        byte[] byteToken = xx.getBytes();

        for(int i = 0; i< byteToken.length;i++){
            decodedKey[i]=byteToken[i];
        }

        byte[] encodedText = new byte[text.length-cipherTime.length];


        Log.e("keyCipherTimeLength",Integer.toString(cipherTime.length));

        for(int i = 0; i< encodedText.length;i++){
            encodedText[i]= text[cipherTime.length+i];
        }

        SecretKey tempKey = new SecretKeySpec(decodedKey,0,decodedKey.length,totp.getAlgorithm());


        Cipher c2 = Cipher.getInstance("AES");

        Log.e("keyle",new String(decodedKey));
        c2.init(Cipher.DECRYPT_MODE,tempKey);
        Log.e("keyle",Integer.toString(encodedText.length));

        Log.e("keyletext",Integer.toString(text.length));

        byte[] dectypted = c2.doFinal(encodedText);

        outputStream = new FileOutputStream(file);

 outputStream.write(dectypted);

        Log.e("testCipheredText",new String(dectypted));



        outputStream.close();
        //ApplicationContext.showToast("Odszyfrowywanie zakończone powodzeniem!");
        return false;
    }



    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public long bytesToLong(byte[] bytes) {

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);


        return buffer.getLong();
    }
}
