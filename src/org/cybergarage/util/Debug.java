package org.cybergarage.util;

import android.util.Log;
import java.io.PrintStream;

import com.letv.upnpControl.tools.LetvLog;

public final class Debug {
	public static Debug debug = new Debug();
	public static boolean enabled = false;
	private PrintStream out = System.out;

	public static int d(String paramString1, String paramString2) {
		if (LetvLog.B_DEBUG) {
			Log.d(paramString1, paramString2);
		}
		return 0;
	}
	public static int v(String paramString1, String paramString2) {
		if (LetvLog.B_DEBUG) {
			Log.d(paramString1, paramString2);
		}
		return 0;
	}
	public static int e(String paramString1, String paramString2) {
		if (LetvLog.B_DEBUG) {
			Log.e(paramString1, paramString2);
		}
		return 0;
	}

	public static int e(String paramString1, String paramString2,
			Throwable paramThrowable) {
		if (LetvLog.B_DEBUG) {
			Log.e(paramString1, paramString2, paramThrowable);
		}
		return 0;
	}

	public static boolean isOn() {
		return enabled;
	}

	public static final void message(String paramString) {
		if (enabled != true)
			return;
		debug.getOut().println("CyberGarage message : " + paramString);
	}

	public static final void warning(Exception paramException) {
		warning(paramException.getMessage());
		paramException.printStackTrace(debug.getOut());
	}

	public static final void warning(String paramString) {
		debug.getOut().println("CyberGarage warning : " + paramString);
	}

	public static final void warning(String paramString,
			Exception paramException) {
		if (paramException.getMessage() == null) {
			debug.getOut().println(
					"CyberGarage warning : " + paramString + " START");
			paramException.printStackTrace(debug.getOut());
			debug.getOut().println(
					"CyberGarage warning : " + paramString + " END");
			return;
		}
		debug.getOut().println(
				"CyberGarage warning : " + paramString + " ("
						+ paramException.getMessage() + ")");
		paramException.printStackTrace(debug.getOut());
	}

	public synchronized PrintStream getOut() {
		return out;
	}

	public synchronized void setOut(PrintStream out) {
		this.out = out;
	}
}
