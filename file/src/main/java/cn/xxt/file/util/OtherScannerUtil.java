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

public class OtherScannerUtil {
    private final static String[] FILE_PROJECTION = new String[]{
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns._ID,
    };

    Fragment fragment;
    int type;

    public OtherScannerUtil(Fragment fragment, int type) {
        this.fragment = fragment;
        this.type = type;
    }

    public interface OtherFileScannerCallBack{
        public boolean otherScannerComplete(List<FileInfo> fileInfos);
    }

    public void getOtherFromLocal(final OtherFileScannerCallBack otherFileScannerCallBack) {
        fragment.getLoaderManager().restartLoader(type, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String select = "(" + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.txt'" + " or "
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.zip'" + " or "
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.rar'" + " or "
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.apk'" + ")";

                CursorLoader cursorLoader = null;
                if (id == type) {
                    cursorLoader = new CursorLoader(
                            fragment.getActivity(), MediaStore.Files.getContentUri("external"),
                            FILE_PROJECTION, select, null,
                            FILE_PROJECTION[2] + " DESC");
                }
                return cursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                try {
                    List<FileInfo> fileInfos = new ArrayList<>();
                    if (data != null) {
                        int count = data.getCount();
                        if (count > 0) {
                            data.moveToFirst();
                            do {
                                String path = data.getString(data.getColumnIndexOrThrow(FILE_PROJECTION[0]));
                                if (TextUtils.isEmpty(path) || !new File(path).exists() || new File(path).isDirectory()) {
                                    continue;
                                }
                                FileInfo fileInfo = FileUtil.getFileInfoFromFile(new File(path));
                                fileInfo.fileId = data.getInt(data.getColumnIndexOrThrow(FILE_PROJECTION[3]));
                                fileInfo.setFileType(FileTypeEnum.TYPE_OTHER.getFileType());

//                                //FIXME for test
                                fileInfo.setDownloadStatus(FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADED.getFileDownloadStatus());

                                if (fileInfo.fileSize > 0) {
                                    fileInfos.add(fileInfo);
                                }
                            } while (data.moveToNext());
                            otherFileScannerCallBack.otherScannerComplete(fileInfos);
                            data.close();
                        } else {
                            // 如果没有对应文件
                            Toast.makeText(fragment.getActivity(), "sorry,没有读取到文件", Toast.LENGTH_LONG).show();
                            otherFileScannerCallBack.otherScannerComplete(null);
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
