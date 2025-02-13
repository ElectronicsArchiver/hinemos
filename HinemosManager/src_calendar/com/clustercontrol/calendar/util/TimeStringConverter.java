/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.clustercontrol.bean.DateTimeFormatConstant;
import com.clustercontrol.util.HinemosTime;

/**
 * 
 * 時間の値(HH:mm:ss)をDate型及びString型に相互変換するクラス 0時未満および24時超も許容する(0時～24時以内へ補正しない)。
 *
 */
public class TimeStringConverter {
	/**
	 * Date型の時間をString型に変換する。
	 */
	public static String formatTime(Date timeValue) {
		String strTime = "";

		// 変換例
		// 48:00～：50:00を02:00と表示されないよう48加算する
		// 24:00～：26:00を02:00と表示されないよう24加算する
		// 00:00～：変換不要
		// 0時未満の場合
		// 前日の23:45は-00:15と表示する
		// 前々日の22:00は-26:00と表示する
		
		if (timeValue != null) {

			final TimeZone utc0 = TimeZone.getTimeZone("UTC");
			SimpleDateFormat sdfHH = new SimpleDateFormat(DateTimeFormatConstant.HR);
			sdfHH.setTimeZone(utc0);
			SimpleDateFormat sdfMmSs = new SimpleDateFormat(DateTimeFormatConstant.MIN_SEC);
			sdfHH.setTimeZone(utc0);

			long hour24 = 24 * 60 * 60 * 1000;
			long msecTime = timeValue.getTime() + HinemosTime.getTimeZoneOffset();

			if (hour24 <= msecTime) {
				String strHH = sdfHH.format(msecTime);
				Long hh = Long.parseLong(strHH);
				hh = hh + 24 * (msecTime / hour24);
				strTime = String.valueOf(hh) + ":" + sdfMmSs.format(msecTime);
			} else if (msecTime < 0) {
				long absTime = Math.abs(msecTime);
				String strHH = sdfHH.format(absTime);
				Long hh = Long.parseLong(strHH);
				hh = hh + 24 * (absTime / hour24);
				strTime = "-" + String.format("%02d", hh) + ":" + sdfMmSs.format(absTime);
			} else {
				// 補正不要
				SimpleDateFormat sdf = new SimpleDateFormat(DateTimeFormatConstant.COMMON_TIME);
				sdf.setTimeZone(utc0);
				strTime = sdf.format(msecTime);
			}
		}
		return strTime;
	}

	/**
	 * String型の時間をDate型に変換する。
	 */
	public static Date parseTime(String timeValue) throws ParseException {

		// "xx:xx:xx"形式でなければ許容しない
		String[] splitTime = timeValue.split(":", 0);
		if (splitTime.length != 3) {
			throw new ParseException("Unparseable date:" + timeValue, 0);
		}
		// 空白は許容しない
		if (splitTime[0].equals("") || splitTime[1].equals("") || splitTime[2].equals("")) {
			throw new ParseException("Unparseable date:" + timeValue, 0);
		}
		// "+"は許容しない(-は許容する)
		if (timeValue.indexOf("+") != -1) {
			throw new ParseException("Unparseable date:" + timeValue, 0);
		}

		long msecTime = 0;
		try {
			long hh = Long.parseLong(splitTime[0]);
			long mm = Long.parseLong(splitTime[1]);
			long ss = Long.parseLong(splitTime[2]);

			// "-00:30:00"の場合hhは0と変換されるため、0時未満かは先頭文字が"-"か否かで判断
			if (splitTime[0].charAt(0) == '-') {
				hh = Math.abs(hh);
				mm = Math.abs(mm);
				ss = Math.abs(ss);
				// "-1:-90:00"は"-02:30:00"相当の値とする
				msecTime = -1000 * (hh * 3600 + mm * 60 + ss);
			} else {
				// "02:-30:00"は"01:30:00"相当の値とする
				msecTime = 1000 * (hh * 3600 + mm * 60 + ss);
			}
		} catch (NumberFormatException e) {
			// Integer.parseInt()で変換出来ない、数字(及び-)以外の文字は許容しない
			throw new ParseException("Unparseable date:" + timeValue, 0);
		}

		SimpleDateFormat sdf = new SimpleDateFormat(DateTimeFormatConstant.COMMON_TIME);
		sdf.setTimeZone(HinemosTime.getTimeZone());
		Date rtnDate = new Date(msecTime - HinemosTime.getTimeZoneOffset());
		return rtnDate;
	}
}
