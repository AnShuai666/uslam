package com.ubtrobot.uslam.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

/**
 * 有些逻辑并不需要单独使用一个HandlerThread，但又有一些临时的任务需要开启线程进行处理，此时就可以将任务丢到这个线程来处理
 * 
 */
public class BaseThread {

	private BaseHandler handler;
	private HandlerThread thread;

// 	public BaseThread(String name) {
// 		this(name, android.os.Process.THREAD_PRIORITY_LOWEST);
// 	}

	public BaseThread(String name, int priority) {
		thread = new HandlerThread(name, priority);
		thread.start();
		handler = new BaseHandler(thread.getLooper());
	}

	// private final Printer mLooperPrinter = new Printer() {
	// @Override
	// public void println(String x) {
	// handleLooperReport(x);
	// }
	// };

	// private void handleLooperReport(String traceMsg) {
	// if (ThreadTracer.isLooperStartMsg(traceMsg)) {
	// taskRunID ++;
	// } else if (ThreadTracer.isLooperEndMsg(traceMsg)) {
	// taskDoneID ++;
	// if(mListener!=null){
	// mListener.statusChange(false);
	// }
	// }
	// }

	public Looper getLooper() {
		return thread.getLooper();
	}

	public Handler getHandler() {
		return handler;
	}

	// public static CommonTaskThread getInstance() {
	// if (threadInstance == null) {
	// synchronized (lock) {
	// if (threadInstance == null) {
	// threadInstance = new CommonTaskThread("CommonTaskThread");
	// }
	// }
	// }
	// return threadInstance;
	// }

	/**
	 * 将Runnable内部的代码丢到CommonTaskThread里执行
	 * 
	 * @param runnable
	 */
	public void post(Runnable runnable) {
		postDelayed(runnable, 0);
	}

	public void removeTask(Runnable task) {
		handler.removeCallbacks(task);
	}

	public void postDelayed(Runnable runnable, long delayMillis) {
		handler.postDelayed(runnable, delayMillis);
	}

	/**
	 * 还原线程优先级为默认优先级
	 */
	public void nicePriorityToDefault() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
			}
		});
	}

	public void setDaemon(boolean isDaemon) {
		thread.setDaemon(isDaemon);
	}

	public void start() {
		thread.start();
	}

	public boolean isAlive() {
		return thread.isAlive();
	}

}
