package cn.xxt.commons.util.download;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.RandomAccessFile;

import cn.xxt.commons.util.RemoteServiceUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileDownloadThread extends Thread{
	private static final String TAG = "FileDownloadThread";
	/**缓冲区 */
	private static final int BUFF_SIZE = 1024;
	/**需要下载的URL*/
	private String urlStr;
	/**缓存的FIle*/
	private File file;
	/**开始位置*/
	private long startPosition;
	/**结束位置*/
	private long endPosition;
	/**当前位置*/
	private long curPosition;
	/**完成*/
	private boolean finished = false;
	private boolean isfailed = false;
	/**已经下载多少*/
	private int downloadSize = 0;

	private int tryNum = 0;

	private Context context;

	/***
	 * 分块文件下载，可以创建多线程模式
	 * @param urlStr   下载的URL
	 * @param file  下载的文件
	 * @param startPosition 开始位置
	 * @param endPosition   结束位置
	 */
	public FileDownloadThread(Context context, String urlStr, File file, long startPosition,
							  long endPosition) {
		this.context = context;
		this.urlStr = urlStr;
		this.file = file;
		this.startPosition = startPosition;
		this.curPosition = startPosition;
		this.endPosition = endPosition;
		Log.e(TAG, toString());
	}

	@Override
	public void run() {
		downloadFile();
		super.run();
	}

	public void downloadFile() {
		BufferedInputStream bis = null;
		RandomAccessFile rAccessFile = null;
		byte[] buf = new byte[BUFF_SIZE];
		try {
			finished = false;
			isfailed = false;
			tryNum++;

			OkHttpClient client = RemoteServiceUtil.getOkHttpClientNoInterceptor(context,5*60L,5*60L,5*60L);
			Request request=new Request.Builder()
					.addHeader("Range", "bytes=" + (curPosition) + "-" + endPosition)
					.url(urlStr)
					.build();

			try {
				Response response = client.newCall(request).execute();
				if(response!=null && response.isSuccessful()) {
					rAccessFile = new RandomAccessFile(file,"rwd");//读写
					//设置从什么位置开始写入数据
					rAccessFile.seek(curPosition);
					bis = new BufferedInputStream(response.body().byteStream(), BUFF_SIZE);
					while(curPosition<endPosition)  //当前位置小于结束位置  继续读
					{
						int len = bis.read(buf,0,BUFF_SIZE);
						if(len==-1)   //下载完成
						{
							break;
						}
						rAccessFile.write(buf,0,len);
						curPosition = curPosition +len;
						if(curPosition > endPosition)
						{	//如果下载多了，则减去多余部分
							System.out.println("  curPosition > endPosition  !!!!");
							long extraLen = curPosition-endPosition;
							downloadSize += (len-extraLen+1);
						}else{
							downloadSize+=len;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (curPosition>=endPosition-10) {
				this.finished = true;  //当前阶段下载完成
				Log.e(TAG, "FileDownloadThread "+ getName() + " finished");
			} else {
				isfailed = true;
			}

		} catch (Exception e) {
			isfailed = true;
			Log.e(TAG, "download error Exception "+e.getMessage());
		}finally{
			//关闭流
			try {
				if(bis!=null){
					bis.close();
				}
				if(rAccessFile!=null) {
					rAccessFile.close();
				}
			} catch (Exception e) {
				isfailed = true;
			}
		}
	}

	/**
	 * 是否完成当前段下载完成
	 * @return  true:当前端下载成功，false：当前的下载失败
	 */
	public boolean isFinished() {
		return finished;
	}

	public boolean isFailed() {
		return isfailed;
	}

	public int getTryNum() {
		return tryNum;
	}

	/**
	 * 已经下载多少
	 * @return 下载的多少
	 */
	public int getDownloadSize() {
		return downloadSize;
	}

	@Override
	public String toString() {
		return "FileDownloadThread "+ getName() + " [url=" + urlStr + ", file=" + file
				+ ", startPosition=" + startPosition + ", endPosition="
				+ endPosition + ", curPosition=" + curPosition + ", finished="
				+ finished + ", downloadSize=" + downloadSize + "]";
	}
}