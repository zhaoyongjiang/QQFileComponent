package cn.xxt.file.util;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.file.internal.domain.FolderInfo;


/**
 * Created by zyj on 2017/8/12.
 */

public class PhotoScannerUtil {
    private final static String[] IMAGE_PROJECTION = new String[]{
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media._ID,
    };

    public interface PhotoScannerCallBack{
        public boolean photoScannerComplete(List<FolderInfo> folders);
    }

    Fragment fragment;
    int type;

    public PhotoScannerUtil(Fragment fragment, int type) {
        this.fragment = fragment;
        this.type = type;
    }

    public void getPhotoFromLocal(final PhotoScannerCallBack photoScannerCallBack) {
        //这里不能使用initLoader。同一个fg，二次获取数据，cursor已经关闭。可以采用restartLoader。
        // https://www.cnblogs.com/zhujiabin/p/6595066.html
        fragment.getLoaderManager().restartLoader(type, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader cursorLoader = null;
                if (id == type) {
                    String select = "(" + MediaStore.Images.ImageColumns.DATA
                            + " LIKE '%.png'" + " or "
                            + MediaStore.Images.ImageColumns.DATA
                            + " LIKE '%.jpg'" + " or "
                            + MediaStore.Images.ImageColumns.DATA
                            + " LIKE '%.jpeg'" + " or "
                            + MediaStore.Images.ImageColumns.DATA
                            + " LIKE '%.bmp'" + ")";
                    cursorLoader = new CursorLoader(
                            fragment.getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            IMAGE_PROJECTION, select, null,
                            IMAGE_PROJECTION[2] + " DESC");
                }
                return cursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                try {
                    ArrayList<FolderInfo> imageFolders = new ArrayList<FolderInfo>();
                    if (data != null) {
                        int count = data.getCount();
                        if (count > 0) {
                            data.moveToFirst();
                            do {
                                String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                                if (TextUtils.isEmpty(path) || !new File(path).exists() || new File(path).isDirectory()) {
                                    continue;
                                }
                                FileInfo fileInfo = FileUtil.getFileInfoFromFile(new File(path));
                                fileInfo.fileId = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
                                fileInfo.fileType = FileTypeEnum.TYPE_IMAGE.getFileType();
                                FolderInfo folder = getImageFolder(path, imageFolders);
                                folder.getFileInfos().add(fileInfo);
                            } while (data.moveToNext());
                            Collections.sort(imageFolders, new FileNameComparator());
                            photoScannerCallBack.photoScannerComplete(imageFolders);
                            data.close();
                        } else {
                            // 如果没有相册
                            Toast.makeText(fragment.getActivity(), "sorry,没有读取到图片!", Toast.LENGTH_LONG).show();
                            photoScannerCallBack.photoScannerComplete(null);
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

    /**
     * 根据文件名进行比较排序
     */
    public static class FileNameComparator implements Comparator<FolderInfo> {
        @Override
        public int compare(FolderInfo lhs, FolderInfo rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    }

    private FolderInfo getImageFolder(String path, List<FolderInfo> imageFolders) {
        File imageFile = new File(path);
        File folderFile = imageFile.getParentFile();

        for (FolderInfo folder : imageFolders) {
            if (folder.getName().equals(folderFile.getName())) {
                return folder;
            }
        }
        FolderInfo newFolder = new FolderInfo();
        newFolder.setName(folderFile.getName());
        newFolder.setPath(folderFile.getAbsolutePath());
        imageFolders.add(newFolder);
        return newFolder;
    }
}
