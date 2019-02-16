package cn.xxt.file.util;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.xxt.file.internal.domain.FileDownloadStatusEnum;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;


/**
 * Created by zyj on 2017/8/12.
 */

public class AudioScannerUtil {
    private final static String[] AUDIO_PROJECTION = new String[]{
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DURATION,
    };

    public interface AudioScannerCallBack{
        public boolean audioScannerComplete(List<FileInfo> fileInfos);
    }

    Fragment fragment;
    int type;

    public AudioScannerUtil(Fragment fragment, int type) {
        this.fragment = fragment;
        this.type = type;
    }

    public void getAudioFromLocal(final AudioScannerCallBack audioScannerCallBack) {
        fragment.getLoaderManager().restartLoader(type, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader cursorLoader = null;
                if (id == type) {
                    String select = "(" + MediaStore.Audio.AudioColumns.DATA
                            + " LIKE '%.mp3'" + " or "
                            + MediaStore.Audio.AudioColumns.DATA
                            + " LIKE '%.mp4'" + " or "
                            + MediaStore.Audio.AudioColumns.DATA
                            + " LIKE '%.wav'" + " or "
                            + MediaStore.Audio.AudioColumns.DATA
                            + " LIKE '%.aac'" + " or "
                            + MediaStore.Audio.AudioColumns.DATA
                            + " LIKE '%.m4a'" + ")";
                    cursorLoader = new CursorLoader(
                            fragment.getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            AUDIO_PROJECTION, select, null,
                            AUDIO_PROJECTION[2] + " DESC");
                }
                return cursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                try {
                    ArrayList<FileInfo> fileInfos = new ArrayList<>();
                    if (data != null) {
                        int count = data.getCount();
                        if (count > 0) {
                            data.moveToFirst();
                            do {
                                String path = data.getString(data.getColumnIndexOrThrow(AUDIO_PROJECTION[0]));
                                if (TextUtils.isEmpty(path) || !new File(path).exists() || new File(path).isDirectory()) {
                                    continue;
                                }
                                FileInfo fileInfo = FileUtil.getFileInfoFromFile(new File(path));
                                fileInfo.fileId = data.getInt(data.getColumnIndexOrThrow(AUDIO_PROJECTION[3]));
                                fileInfo.voiceDuration = data.getInt(data.getColumnIndexOrThrow(AUDIO_PROJECTION[4]));
                                fileInfo.setFileType(FileTypeEnum.TYPE_AUDIO.getFileType());

//                                //FIXME for test
                                fileInfo.setDownloadStatus(FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADED.getFileDownloadStatus());

                                fileInfos.add(fileInfo);
                            } while (data.moveToNext());
                            audioScannerCallBack.audioScannerComplete(fileInfos);
                            data.close();
                        } else {
                            // 如果没有音频
                            Toast.makeText(fragment.getActivity(), "sorry,没有读取到音乐文件", Toast.LENGTH_LONG).show();
                            audioScannerCallBack.audioScannerComplete(null);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });
    }
}
