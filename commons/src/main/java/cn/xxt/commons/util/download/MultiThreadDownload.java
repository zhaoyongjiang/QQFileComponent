package cn.xxt.commons.util.download;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.RandomAccessFile;

import cn.xxt.commons.util.FileUtil;
import cn.xxt.commons.util.RemoteServiceUtil;
import cn.xxt.commons.util.StringUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *多线程下载，UI更新类
 *@author young
 * */
public class MultiThreadDownload extends Thread{
	private static final String TAG = "luxy";

	private static final int MAX_TRY_TIMES = 5;

	/**每一个线程需要下载的大小 */
	private long blockSize;
	/*** 线程数量<br> 默认为3个线程下载*/
	private int threadNum = 5;
	/*** 文件大小 */
	private long fileSize;
	/** * 已经下载多少 */
	private long downloadSize;
	/**文件的url,线程编号，文件名称*/
	private String urlStr,ThreadNo;
	/**下载的百分比*/
	private int downloadPercent = 0;
	/**下载的 平均速度*/
	private int downloadSpeed = 0;
	/**下载用的时间*/
	private long usedTime = 0;
	/**当前时间*/
	private long curTime;
	/**是否已经下载完成*/
	private boolean completed = false;
	private Handler handler ;

	private String dir = Environment.getExternalStorageDirectory().getPath() + "/QQFileComponent/";
	private String fileName = String.valueOf(System.currentTimeMillis());

	private Context context;

	/**
	 * 下载的构造函数，默认下载文件夹路径，默认文件名称
	 * @param url  请求下载的URL
	 * @param handler 下载结果处理
	 */
	public MultiThreadDownload(Context context, String url, Handler handler)
	{
		this.context = context;
		this.handler = handler;
		this.urlStr = url;
		this.fileName = DownloadFileUtil.getFileName(url);

		createDownloadEnv();
	}

	/**
	 * 下载的构造函数，自定义下载文件夹路径，默认文件名称
	 * @param url 请求下载的URL
	 * @param dir 自定义下载文件夹路径
	 * @param handler 下载结果处理
	 */
	public MultiThreadDownload(Context context, String url,String dir,Handler handler)
	{
		this.context = context;
		this.handler = handler;
		this.urlStr = url;
		if (dir.startsWith(Environment.getExternalStorageDirectory().getPath())){
			this.dir = dir;
		} else {
			this.dir = Environment.getExternalStorageDirectory().getPath() + dir;
		}

		if (!dir.endsWith("/"))
		{
			this.dir += "/";
		}

		this.dir = FileUtil.getDirPathAfterFormat(dir);

		this.fileName = DownloadFileUtil.getFileName(url);

		createDownloadEnv();
	}

	/**
	 * 下载的构造函数，自定义下载文件夹路径，自定义文件名称
	 * @param url 请求下载的URL
	 * @param dir 自定义下载文件夹路径
	 * @param fileName 自定义文件名称
	 * @param handler 下载结果处理
	 */
	public MultiThreadDownload(Context context, String url,String dir,String fileName,Handler handler)
	{
		this.context = context;
		this.handler = handler;
		this.urlStr = url;

		this.dir = FileUtil.getDirPathAfterFormat(dir);

		this.fileName = fileName;

		createDownloadEnv();
	}

	/**
	 * 获取默认下载目录路径
	 * @return 默认下载目录路径
	 */
	public static String getDownloadDir()
	{
		return StringUtil.connectStrings(Environment.getExternalStorageDirectory().getPath(),
				"/QQFileComponent/");
	}

	/**
	 * 获取默认的下载文件路径
	 * @param url 请求下载的URL
	 * @return 默认的下载文件路径
	 */
	public static String getSavePathByUrl(String url) {
		return StringUtil.connectStrings(getDownloadDir(),
				DownloadFileUtil.getFileName(url));
	}

	/**
	 * 获取自定义下载文件夹+默认文件名称的文件路径
	 * @param url 请求下载的URL
	 * @param dir 自定义下载文件夹
	 * @return 自定义下载文件夹+默认文件名称的文件路径
	 */
	public static String getSavePathByUrl(String url, String dir) {
		return StringUtil.connectStrings(FileUtil.getDirPathAfterFormat(dir),
				DownloadFileUtil.getFileName(url));

	}

	/**
	 * 获取自定义下载文件夹+自定义文件名称的文件路径
	 * @param url 请求下载的URL
	 * @param dir 自定义下载文件夹
	 * @param fileName 自定义文件名称
	 * @return 自定义下载文件夹+自定义文件名称的文件路径
	 */
	public static String getSavePathByUrl(String url, String dir, String fileName) {
		return StringUtil.connectStrings(FileUtil.getDirPathAfterFormat(dir),
				fileName);
	}

	private void createDownloadDir() {
		File file = new File(dir);
		// 判断文件目录是否存在
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	private void createDownloadEnv() {
		createDownloadDir();

		File file = new File(dir+fileName);
		// 判断文件目录是否存在
		if (file.exists()) {
			file.delete();
		}
	}

	private String getSavePath()
	{
		return dir + fileName;
	}

	@Override
	public void run() {

		//设置线程数量
		FileDownloadThread[] fds = new FileDownloadThread[threadNum];
		try {
			//Handler更新UI，发送消息
			sendMsg(DownloadFileUtil.START_DOWNLOAD_MSG);

			OkHttpClient client = RemoteServiceUtil.getOkHttpClientNoInterceptor(context,null,null,null);
			Request request=new Request.Builder()
					.url(urlStr)
					.build();

			try {
				Response response = client.newCall(request).execute();
				if(response!=null&&response.isSuccessful()) {
					fileSize = response.body().contentLength();
					response.body().close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (fileSize>0) {
				//只创建一个文件，saveFile下载内容
				File saveFile = new File(getSavePath());

				RandomAccessFile accessFile = new RandomAccessFile(saveFile, "rwd");
				//设置本地文件的长度和下载文件相同
				accessFile.setLength(fileSize);
				accessFile.close();

				//每块线程下载数据
				blockSize = ((fileSize % threadNum) == 0) ? (fileSize / threadNum) : (fileSize / threadNum + 1);
				Log.e(TAG, "每个线程分别下载 ：" + blockSize);

				for (int i = 0; i < threadNum; i++) {
					long curThreadEndPosition = (i + 1) != threadNum ? ((i + 1) * blockSize - 1) : fileSize-1;
					FileDownloadThread fdt = new FileDownloadThread(context, urlStr, saveFile, i * blockSize, curThreadEndPosition);
					fdt.setName("thread" + i);
					fdt.start();
					fds[i] = fdt;
				}
				/**
				 * 获取数据，更新UI，直到所有下载线程都下载完成。
				 */
				boolean finished = false;
				boolean isfailed = false;
				//开始时间，放在循环外，求解的usedTime就是总时间
				long startTime = System.currentTimeMillis();
				while (!finished && !isfailed) {
					downloadSize = 0;
					finished = true;
					for (int i = 0; i < fds.length; i++) {
						downloadSize += fds[i].getDownloadSize();
						if (!fds[i].isFinished()) {
							finished = false;
						}
						if (fds[i].isFailed()) {
							if (fds[i].getTryNum() > MAX_TRY_TIMES) {
								isfailed = true;
								break;
							} else {
								fds[i].downloadFile();
							}
						}
//						Log.e(TAG,fds[i].toString());
					}
					if (isfailed) {
						break;
					}

					downloadPercent = (int)((downloadSize * 100) / fileSize);

					curTime = System.currentTimeMillis();

					usedTime = curTime - startTime;


					if (usedTime == 0) {
                        usedTime = 100;
                    }
					downloadSpeed = (int) ((downloadSize / usedTime) * 1000 / 1024);


//					Log.e(TAG,"curTime = " + curTime
//                            + " downloadSize = " + downloadSize
//                            + " usedTime= " + (curTime - startTime)
//                            + " downloadSpeed= " + String.valueOf(downloadSpeed));

					/*0.1秒钟刷新一次界面*/
					sleep(100);
					if (downloadPercent%5==0) {
						sendMsg(DownloadFileUtil.UPDATE_DOWNLOAD_MSG, downloadPercent);
					}
				}
				if (isfailed) {
					revertData();
					sendMsg(DownloadFileUtil.DOWNLOAD_FAIL_MSG);
				} else {
					Log.e(TAG, "下载完成");
					completed = true;
					sendCompleteMsg(downloadSpeed);
				}
			} else {
				revertData();
				sendMsg(DownloadFileUtil.DOWNLOAD_FAIL_MSG);
			}
		} catch (Exception e) {
			revertData();
			Log.e(TAG, "multi file error  Exception  "+e.getMessage());
			sendMsg(DownloadFileUtil.DOWNLOAD_FAIL_MSG);
		}
		super.run();
	}

	/**
	 * 下载失败的时候把下一部分的文件干掉
	 */
	private void revertData() {
		try {
			File saveFile = new File(getSavePath());
			if (saveFile.exists()) {
				saveFile.delete();
			}
		} catch (Exception e) {

		}
	}
	/**
	 * 得到文件的大小
	 * @return
	 */
	public long getFileSize()
	{
		return this.fileSize;
	}
	/**
	 * 得到已经下载的数量
	 * @return
	 */
	public long getDownloadSize()
	{
		return this.downloadSize;
	}
	/**
	 * 获取下载百分比
	 * @return
	 */
	public int getDownloadPercent(){
		return this.downloadPercent;
	}
	/**
	 * 获取下载速度
	 * @return
	 */
	public int getDownloadSpeed(){
		return this.downloadSpeed;
	}
	/**
	 * 修改默认线程数
	 * @param threadNum
	 */
	public void setThreadNum(int threadNum){
		this.threadNum = threadNum;
	}
	/**
	 * 分块下载完成的标志
	 * @return
	 */
	public boolean isCompleted(){
		return this.completed;
	}

	@Override
	public String toString() {
		return "MultiThreadDownload [threadNum=" + threadNum + ", fileSize="
				+ fileSize + ", urlStr=" + urlStr + ", ThreadNo=" + ThreadNo
				+ "]";
	}

	/**
	 * 发送消息，用户提示
	 * */
	private void sendMsg(int what)
	{
		if (handler!=null) {
			Message msg = new Message();
			msg.what = what;
			handler.sendMessage(msg);
		}
	}

	private void sendMsg(int what,int downloadPercent) {
		if (handler!=null) {
			Message msg = new Message();
			msg.what = what;
			Bundle bundle = new Bundle();
			bundle.putInt("percent", downloadPercent);
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}

	private void sendCompleteMsg(int downloadSpeed) {
		if (handler!=null) {
			Message msg = new Message();
			msg.what = DownloadFileUtil.DOWNLOAD_SUCCESS_MSG;
			Bundle bundle = new Bundle();
			bundle.putInt("downloadSpeed", downloadSpeed);
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}
}
