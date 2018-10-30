# android byte[]与图片的转换

今天，简单讲讲android如何将byte数组的数据转换成图片显示。

之前，在做一个功能时，从服务器获得了图片的byte数组的数据，需要将数据转成图片显示在手机上，或者保存在文件里。当时居然不知道怎么转换，所以在网上查找了资料，最终是解决了问题。这里记录一下。

直接上代码：

package com.bingo.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class ImageDispose {
​	
​	
​	
	/**
	 * @param 将图片内容解析成字节数组
	 * @param inStream
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] readStream(InputStream inStream) throws Exception {
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inStream.close();
		return data;
	
	}
	/**
	 * @param 将字节数组转换为ImageView可调用的Bitmap对象
	 * @param bytes
	 * @param opts
	 * @return Bitmap
	 */
	public static Bitmap getPicFromBytes(byte[] bytes,
			BitmapFactory.Options opts) {
		if (bytes != null)
			if (opts != null)
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
						opts);
			else
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return null;
	}
	/**
	 * @param 图片缩放
	 * @param bitmap 对象
	 * @param w 要缩放的宽度
	 * @param h 要缩放的高度
	 * @return newBmp 新 Bitmap对象
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		return newBmp;
	}
	
	/**
	 * 把Bitmap转Byte
	 * @Author HEH
	 * @EditTime 2010-07-19 上午11:45:56
	 */
	public static byte[] Bitmap2Bytes(Bitmap bm){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	/**
	 * 把字节数组保存为一个文件
	 * @Author HEH
	 * @EditTime 2010-07-19 上午11:45:56
	 */
	public static File getFileFromBytes(byte[] b, String outputFile) {
	    BufferedOutputStream stream = null;
	    File file = null;
	    try {
	        file = new File(outputFile);
	        FileOutputStream fstream = new FileOutputStream(file);
	        stream = new BufferedOutputStream(fstream);
	        stream.write(b);
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (stream != null) {
	            try {
	                stream.close();
	            } catch (IOException e1) {
	                e1.printStackTrace();
	            }
	        }
	    }
	    return file;
	}

}


简单讲讲，这里面其实已经有byte[]和bitmap的转换，转换成bitmap后，就可以直接显示到界面上。但是如何需要将图片的byte[]存储进入手机的文件里，那应该怎么办呢？其实也很简单。

private void bytesToImageFile(byte[] bytes) {
​        try {
​            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaa.jpeg");
​            FileOutputStream fos = new FileOutputStream(file);
​            fos.write(bytes, 0, bytes.length);
​            fos.flush();
​            fos.close();
​        } catch (Exception e) {
​            e.printStackTrace();
​        }


简单讲讲，其实就是定义一个后缀名为jpg的文件名，然后使用输出流将byte[]写入文件就可以了。


android byte[]与图片的转换就讲完了。


就这么简单。
--------------------- 