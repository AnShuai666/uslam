package com.ubtrobot.uslam.thread;

import android.os.AsyncTask;

import java.util.concurrent.Executor;

public final class ThreadManager {

	/**
	 * AsyncTask的默认线程池Executor. 负责长时间的任务(网络访问) 默认5个线程
	 */
	public static final Executor NETWORK_EXECUTOR;

	static {
		NETWORK_EXECUTOR = initNetworkExecutor();
	}

	private static Executor initNetworkExecutor() {
		Executor result = null;
		result = AsyncTask.THREAD_POOL_EXECUTOR;
		return result;
	}

	public static void init() {

	}

	/**
	 * 在网络线程上执行异步操作. 该线程池负责网络请求等操作 长时间的执行(如网络请求使用此方法执行) 当然也可以执行其他 线程和AsyncTask公用
	 * 
	 * @param run
	 */
	public static void executeOnNetWorkThread(Runnable run) {
		NETWORK_EXECUTOR.execute(run);
	}

}
