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
	 * ��ָ��URL��ȡͼƬ
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
			byte[] bt = getBytes(is); // ע�Ͳ��ֻ�������һ�ַ�ʽ����
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
	 * ��ȡָ��URL����Ӧ�ַ���
	 * 
	 * @param urlString
	 * @return
	 */
	public static String getURLResponse(String urlString) {
		HttpURLConnection conn = null; // ���Ӷ���
		InputStream is = null;
		String resultData = "";
		try {
			URL url = new URL(urlString); // URL����
			conn = (HttpURLConnection) url.openConnection(); // ʹ��URL��һ������
			conn.setDoInput(true); // ����������������������
			conn.setDoOutput(true); // ������������������ϴ�
			conn.setUseCaches(false); // ��ʹ�û���
			conn.setRequestMethod("GET"); // ʹ��get����
			conn.setRequestProperty("Connection", "close");
			is = conn.getInputStream(); // ��ȡ����������ʱ��������������
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
	
	/* �ϴ��ļ���Server��uploadUrl�������ļ��Ĵ���ҳ�� */
	public static String uploadFile(String uploadUrl, String imgSrc) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "******";
		try {
			URL url = new URL(uploadUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			// ����ÿ�δ��������С��������Ч��ֹ�ֻ���Ϊ�ڴ治�����
			// �˷���������Ԥ�Ȳ�֪�����ݳ���ʱ����û�н����ڲ������ HTTP �������ĵ�����
			httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
			// �������������
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			// ʹ��POST����
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
			// ��ȡ�ļ�
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
		// onPreExecute����������ִ�к�̨����ǰ��һЩUI����
		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute() called");
		}

		// doInBackground�����ڲ�ִ�к�̨����,�����ڴ˷������޸�UI
		@Override
		protected String doInBackground(String... params) {
			Log.i(TAG, "doInBackground(Params... params) called" + params[0]);
			return getURLResponse(params[0]);
		}

		// onProgressUpdate�������ڸ��½�����Ϣ
		@Override
		protected void onProgressUpdate(Integer... progresses) {
			Log.i(TAG, "onProgressUpdate(Progress... progresses) called");
		}

		// onPostExecute����������ִ�����̨��������UI,��ʾ���
		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "onPostExecute(Result result) called");
			Log.i(TAG, "result info:" + result);
		}

		// onCancelled����������ȡ��ִ���е�����ʱ����UI
		@Override
		protected void onCancelled() {
			Log.i(TAG, "onCancelled() called");
		}
	}
	
}
