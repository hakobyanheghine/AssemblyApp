package com.assembly.app.utils;

import android.util.Log;

public class L {
	public static String LOGTAG = "assembly";
	public static boolean debugLogEnabled = true;

	public static int i(String msg) {
		if (debugLogEnabled)
			return Log.i(LOGTAG, msg);
		return 0;
	}

	public static int i(String tag, String msg) {
		if (debugLogEnabled)
			return Log.i(tag, msg);
		return 0;
	}

	public static int i(Object... msgArgs) {
		if (!debugLogEnabled)
			return 0;
		StringBuilder sb = new StringBuilder();
		for (Object arg : msgArgs) {
			sb.append(arg);
		}
		return Log.i(LOGTAG, sb.toString());
	}

	public static int i(String tag, Object... msgArgs) {
		if (!debugLogEnabled)
			return 0;
		StringBuilder sb = new StringBuilder();
		for (Object arg : msgArgs) {
			sb.append(arg);
		}
		return Log.i(tag, sb.toString());
	}

	public static int d(String msg) {
		if (debugLogEnabled) {
			return Log.d(LOGTAG, msg);
		}
		return 0;
	}

	public static int d2(String tag, String msg) {
		if (debugLogEnabled) {
			return Log.d(tag, msg);
		}
		return 0;
	}

	public static int d(Object... msgArgs) {
		if (debugLogEnabled) {
			StringBuilder sb = new StringBuilder();
			for (Object arg : msgArgs) {
				sb.append(arg);
			}
			return Log.d(LOGTAG, sb.toString());
		}
		return 0;
	}

	public static int d(String tag, Object... msgArgs) {
		if (debugLogEnabled) {
			StringBuilder sb = new StringBuilder();
			for (Object arg : msgArgs) {
				sb.append(arg);
			}
			return Log.d(tag, sb.toString());
		}
		return 0;
	}

	public static int w(String msg) {
		if (debugLogEnabled)
			return Log.w(LOGTAG, msg);
		return 0;
	}

	public static int w(String tag, String msg) {
		if (debugLogEnabled)
			return Log.w(tag, msg);
		return 0;
	}

	public static int w(Object... msgArgs) {
		StringBuilder sb = new StringBuilder();
		for (Object arg : msgArgs) {
			sb.append(arg);
		}
		return Log.w(LOGTAG, sb.toString());
	}

	public static int w(String tag, String msg, Throwable tr) {
		return Log.w(tag, msg, tr);
	}

	public static int e(String msg) {
		return Log.e(LOGTAG, msg);
	}

	public static int e(Object... msgArgs) {
		StringBuilder sb = new StringBuilder();
		for (Object arg : msgArgs) {
			sb.append(arg);
		}
		return Log.e(LOGTAG, sb.toString());
	}

	public static int e(String msg, Throwable tr) {
		return Log.e(LOGTAG, msg, tr);
	}

	public static int e(String tag, String msg, Throwable tr) {
		return Log.e(tag, msg, tr);
	}
}
