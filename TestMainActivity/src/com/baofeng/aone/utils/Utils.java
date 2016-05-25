package com.baofeng.aone.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

public class Utils {
    /**
     * 使用md5的算法进行加密
     */
    public static String md5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No algorithm！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);// 16进制数字
        // 如果生成数字未满32位，需要前面补0
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }
    
	public static String savedBitmapWithMD5(Context mContext,String url, Bitmap bitmap) {
		String bmpPath = null;
		try {
			String fileName = getMD5Str(url);
			
			File cacheDir = getDiskCacheDir(mContext , "MaxImages");
			cacheDir.mkdirs();
			bmpPath = cacheDir + "/" + fileName;
			File file = new File(bmpPath);
			
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, fos);
	        fos.flush();
	        fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return bmpPath;
	}
	
	public static String getBitmapWithMD5(Context mContext,String url ) {
		String bmpPath = null;
		try {
			String fileName = getMD5Str(url);
			
			bmpPath = getDiskCacheDir(mContext , "MaxImages") + "/" + fileName;
			File file = new File(bmpPath);
			if(!file.exists()){
				bmpPath = null;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return bmpPath;
	}
	
	/**
     * 该方法会判断当前sd卡是否存在，然后选择缓存地址
     * 
     * @param context
     * @param uniqueName
     * @return
     */
	   public static File getDiskCacheDir(Context context, String uniqueName) {
	        String cachePath;
	        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
	            cachePath = context.getExternalCacheDir().getPath();
	        } else {
	            cachePath = context.getCacheDir().getPath();
	        }
	        return new File(cachePath + File.separator + uniqueName);
	    }

    /**   
     * MD5
     */     
    public static String getMD5Str(String str) {     
        MessageDigest messageDigest = null;     
        try {     
            messageDigest = MessageDigest.getInstance("MD5");     
            messageDigest.reset();     
            messageDigest.update(str.getBytes("UTF-8"));     
        } catch (NoSuchAlgorithmException e) {     
            System.out.println("NoSuchAlgorithmException caught!");     
            return null;  
        } catch (UnsupportedEncodingException e) {     
            e.printStackTrace();  
            return null;  
        }     
     
        byte[] byteArray = messageDigest.digest();     
        StringBuffer md5StrBuff = new StringBuffer();     
        for (int i = 0; i < byteArray.length; i++) {                 
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)     
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));     
            else     
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));     
        }     
     
        return md5StrBuff.toString();     
    } 
    
    
	//for test save to sdcard
	private String saveToSdcard(Bitmap bmp) {
		String bmpPath = null;
		//cannot create file  "/sdcard/data/data/com.baofeng/files"?
	//	String dataPath = "/sdcard" + mContext.getFilesDir().getAbsolutePath();
	//	File file = new File(dataPath);
	//	Log.d(TAG, dataPath);
		 File file = new File("/sdcard/myFolder");
	        if (!file.exists())
	            file.mkdir();
	 
	        file = new File("/sdcard/temp.jpg".trim());
	        String fileName = file.getName();
	        String mName = fileName.substring(0, fileName.lastIndexOf("."));
	        String sName = fileName.substring(fileName.lastIndexOf("."));
	 
	        String newFilePath = "/sdcard/myFolder" + "/" + mName + "_cropped" + sName;
	        
	        file = new File(newFilePath);
	        bmpPath = file.getAbsolutePath();
	        try {
	            file.createNewFile();
	            FileOutputStream fos = new FileOutputStream(file);
	            bmp.compress(CompressFormat.JPEG, 50, fos);
	            fos.flush();
	            fos.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		return bmpPath;
	}
	
	public static void writeToFile(Context mContexts, byte[] buffer, String fileName) {
		String downloadFrom = mContexts.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+ File.separator +fileName;
		  File outputFile = new File(downloadFrom); 
		  
	        FileOutputStream outputFileStream = null; 
	        FileWriter fileWriter = null;
	          
	        // try to open file output.txt  
	        try {  
	            outputFileStream = new FileOutputStream(outputFile); 
	            fileWriter = new FileWriter(outputFile);
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	        //output to output.txt   
	        for(int i = 0;i < buffer.length;i++){  
	            try {  
	            //	Log.d(TAG,""+buffer[i]);
	            	if(i % 4 ==0) {
	            		fileWriter.write("\n");
	            	}
	            	fileWriter.write(buffer[i] + ",");
	            } catch (IOException e1) {  
	                e1.printStackTrace();  
	            }  
	        }  
	          
	        //close file stream  
	        try {  
	        	fileWriter.close();
	            outputFileStream.close();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  

	}
}
