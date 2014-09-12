package com.xht.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class HttpUtil {
	public static final String TAG = "HttpUtil";
	public static final String SERVER_CACHE_URL = "http://finger.nat123.net/finger/cache/";
	public static final String SERVER_ENROLL_URL = "http://finger.nat123.net/finger/enroll/";
	public static final String SERVER_URL = "http://finger.nat123.net/finger/";
	public static final String SERVER_UPLOAD = "http://finger.nat123.net/finger/upload.php";
	public static final String SERVER_SAVEPOINT = "http://finger.nat123.net/finger/savePoints.php";
	/**
	 * 从指定URL获取图片
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getImageBitmap(String url) {
		URL imgUrl = null;
		Bitmap bitmap = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		try {
			imgUrl = new URL(url);
			conn = (HttpURLConnection) imgUrl.openConnection();
			conn.setDoInput(true);
			conn.connect();
			is = conn.getInputStream();
			byte[] bt = getBytes(is); // 注释部分换用另外一种方式解码
			bitmap = BitmapFactory.decodeByteArray(bt, 0, bt.length);
			is.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	private static byte[] getBytes(InputStream is) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int len = 0;

		while ((len = is.read(b, 0, 1024)) != -1) {
			baos.write(b, 0, len);
			baos.flush();
		}
		byte[] bytes = baos.toByteArray();
		return bytes;
	}
	
	/**
	 * 获取指定URL的响应字符串
	 * 
	 * @param urlString
	 * @return
	 */
	public static String getURLResponse(String urlString) {
		HttpURLConnection conn = null; // 连接对象
		InputStream is = null;
		String resultData = "";
		try {
			URL url = new URL(urlString); // URL对象
			conn = (HttpURLConnection) url.openConnection(); // 使用URL打开一个链接
			conn.setDoInput(true); // 允许输入流，即允许下载
			conn.setDoOutput(true); // 允许输出流，即允许上传
			conn.setUseCaches(false); // 不使用缓冲
			conn.setRequestMethod("GET"); // 使用get请求
			conn.setRequestProperty("Connection", "close");
			is = conn.getInputStream(); // 获取输入流，此时才真正建立链接
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader bufferReader = new BufferedReader(isr);
			String inputLine = "";
			while ((inputLine = bufferReader.readLine()) != null) {
				resultData += inputLine + "\n";
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}

		return resultData;
	}
	
	/* 上传文件至Server，uploadUrl：接收文件的处理页面 */
	public static String uploadFile(String uploadUrl, String imgSrc) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "******";
		try {
			URL url = new URL(uploadUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			// 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
			// 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
			httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
			// 允许输入输出流
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			// 使用POST方法
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			httpURLConnection.setRequestProperty("Connection", "close");
			DataOutputStream dos = new DataOutputStream(
					httpURLConnection.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + end);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
					+ imgSrc.substring(imgSrc.lastIndexOf("/") + 1)
					+ "\"" + end);
			dos.writeBytes(end);
			FileInputStream fis = new FileInputStream(imgSrc);
			byte[] buffer = new byte[8192]; // 8k
			int count = 0;
			// 读取文件
			while ((count = fis.read(buffer)) != -1) {
				dos.write(buffer, 0, count);
			}
			fis.close();
			dos.writeBytes(end);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
			dos.flush();
			InputStream is = httpURLConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			BufferedReader br = new BufferedReader(isr);
			String result = br.readLine();
			Log.i(TAG, "ReSponse:" + result);
			dos.close();
			is.close();
			httpURLConnection.disconnect();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public class HttpGetTask extends AsyncTask<String, Integer, String> {
		// onPreExecute方法用于在执行后台任务前做一些UI操作
		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute() called");
		}

		// doInBackground方法内部执行后台任务,不可在此方法内修改UI
		@Override
		protected String doInBackground(String... params) {
			Log.i(TAG, "doInBackground(Params... params) called" + params[0]);
			return getURLResponse(params[0]);
		}

		// onProgressUpdate方法用于更新进度信息
		@Override
		protected void onProgressUpdate(Integer... progresses) {
			Log.i(TAG, "onProgressUpdate(Progress... progresses) called");
		}

		// onPostExecute方法用于在执行完后台任务后更新UI,显示结果
		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "onPostExecute(Result result) called");
			Log.i(TAG, "result info:" + result);
		}

		// onCancelled方法用于在取消执行中的任务时更改UI
		@Override
		protected void onCancelled() {
			Log.i(TAG, "onCancelled() called");
		}
	}
	
}
