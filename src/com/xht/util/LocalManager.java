package com.xht.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class LocalManager {

	public static final String TAG = "LocalManager";
	public static final String LOCAL_CACHE = Environment
			.getExternalStorageDirectory() + "/VeriFinger/finger_cache/";
	public static final String LOCAL_AVATAR_CACHE = Environment
			.getExternalStorageDirectory() + "/VeriFinger/avatar_cache/";
	public static final String LOCAL_ORI_FINGER = "original.jpg";
	public static final String LOCAL_FEA_FINGER = "feature.jpg";
	
	// 创建本地文件路径
	public static void CreateLocalDir() {
		File local = new File(Environment.getExternalStorageDirectory()
				+ "/VeriFinger");
		if (!local.exists()) {
			local.mkdirs();
		}
		File cache = new File(Environment.getExternalStorageDirectory()
				+ "/VeriFinger/finger_cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}
		File Avatarcache = new File(Environment.getExternalStorageDirectory()
				+ "/VeriFinger/avatar_cache");
		if (!Avatarcache.exists()) {
			Avatarcache.mkdirs();
		}
	}

	// 将字符串保存到本地路径下txt
	public static void saveLocalStr(String result, String filename) {
		File file = new File(LOCAL_CACHE + filename);
		FileOutputStream outStream;
		try {
			outStream = new FileOutputStream(file);
			try {
				outStream.write(result.getBytes());
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// 从本地txt获得字符串
	public static String getLocalStr(String filename) {
		File file = new File(LOCAL_CACHE + filename);
		try {
			InputStreamReader isr = null;
			isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String str = "";
			String mimeTypeLine = null;
			while ((mimeTypeLine = br.readLine()) != null) {
				str = str + mimeTypeLine;
			}
			return str;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void saveBitmap(Bitmap bm, String filename) {
		File tempfile = new File(LOCAL_CACHE + filename);
		if (tempfile.exists())
			tempfile.delete();
		try {
			tempfile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(tempfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
		try {
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveAvatarBitmap(Bitmap bm, String filename) {
		File tempfile = new File(LOCAL_AVATAR_CACHE + filename);
		if (tempfile.exists())
			tempfile.delete();
		try {
			tempfile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(tempfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
		try {
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
