package com.xht.util;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

public class PointUtil {
	public static final String TAG = "PointUtil";
	public static final int POINT_TAIL = 15;
	public static final int POINT_WIDTH = 5;
	public static final double PI = 3.1415926;
	public static final String ORIGINAL_POINT = "original_point.txt";
	public static final String CURRENT_POINT = "current_point.txt";

	//往原始图上画特征点
	public static Bitmap AddPointToBitmap(List<String> Pointlist, Bitmap bitmap) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setColor(Color.RED);
		Bitmap PointBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(PointBitmap);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(bitmap, 0, 0, null);
		double x = 0, y = 0, a = 0;
		for (int i = 0; i < Pointlist.size(); i++) {
			if (i % 3 == 0)
				x = Double.parseDouble(Pointlist.get(i));
			if (i % 3 == 1)
				y = Double.parseDouble(Pointlist.get(i));
			if (i % 3 == 2) {
				a = Double.parseDouble(Pointlist.get(i));
				canvas.drawRect((int) x - POINT_WIDTH, (int) y - POINT_WIDTH,
						(int) x + POINT_WIDTH, (int) y + POINT_WIDTH, paint);
				canvas.drawLine((int) x, (int) y,
						(int) (x + POINT_TAIL * Math.cos(a * PI / 180)),
						(int) (y - POINT_TAIL * Math.sin(a * PI / 180)), paint);
			}
		}
		return PointBitmap;
	}
	
	//由服务器返回的特征点字符串解析成特征点数组
	public static List<String> getPointArray(String str) {
		List<String> Pointlist = new ArrayList<String>();
		Pattern pattern = Pattern.compile("\\d+\\.\\d+");
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			Pointlist.add(matcher.group());
		}
		return Pointlist;
	}
	
	public static String getPointStr(List<String> Pointlist)
	{
		String result = "";
		for (int i = 0; i < Pointlist.size(); i++)
		{
			result = result + Pointlist.get(i)+" ";
		}
		return result;
	}
	
	//从特征点数组中删除某个点
	public static void DeletePointFromArray(List<String> Pointlist,int index) {
		Pointlist.remove(index*3);
		Pointlist.remove(index*3);
		Pointlist.remove(index*3);
	}
	
	//从匹配结果字符串中提取原始点和匹配点 type = 0为原始 type = 1为匹配
	public static List<String> matchPointArray(String str, int type)
	{
		List<String> Pointlist = new ArrayList<String>();
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(str);
		int i = 0;
		while (matcher.find()) {
			if(i%2 == type)
			Pointlist.add(matcher.group());
			i++;
		}
		return Pointlist;
	}
	
	//往图上画特征点和匹配点
	public static Bitmap AddPointToMatchBitmap(List<String> Pointlist, List<String>Matchlist, Bitmap bitmap) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setColor(Color.RED);
		Bitmap PointBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(PointBitmap);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(bitmap, 0, 0, null);
		double x = 0, y = 0, a = 0;
		for (int i = 0; i < Pointlist.size(); i++) {
			if (i % 3 == 0)
				x = Double.parseDouble(Pointlist.get(i));
			if (i % 3 == 1)
				y = Double.parseDouble(Pointlist.get(i));
			if (i % 3 == 2) {
				a = Double.parseDouble(Pointlist.get(i));
				String index = Integer.toString(i/3);
				int drawindex = indexContainInList(Matchlist, index);
				paint.setColor(Color.RED);
				if(drawindex!=-1)
				{
					paint.setStrokeWidth(1);
					paint.setColor(Color.GREEN);
					canvas.drawText(Integer.toString(drawindex), (float)x+7, (float)y, paint);
					paint.setStrokeWidth(2);
				}
				canvas.drawRect((int) x - POINT_WIDTH, (int) y - POINT_WIDTH,
						(int) x + POINT_WIDTH, (int) y + POINT_WIDTH, paint);
				canvas.drawLine((int) x, (int) y,
						(int) (x + POINT_TAIL * Math.cos(a * PI / 180)),
						(int) (y - POINT_TAIL * Math.sin(a * PI / 180)), paint);
			}
		}
		return PointBitmap;
	}
	
	public static int indexContainInList(List<String>Matchlist,String str)
	{
		for(int i=0;i<Matchlist.size();i++)
		{
			String curstr = Matchlist.get(i);
			if(curstr.equals(str))
				return i;
		}
		return -1;
	}
	
	
}
