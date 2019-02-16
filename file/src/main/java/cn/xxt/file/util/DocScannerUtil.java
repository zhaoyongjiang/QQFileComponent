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
import java.util.Arrays;
import java.util.List;

import cn.xxt.file.internal.domain.FileDownloadStatusEnum;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;


/**
 * Created by zyj on 2017/8/12.
 */

public class DocScannerUtil {
    private final static String[] FILE_PROJECTION = new String[]{
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns._ID,
    };

    Fragment fragment;
    int type;

    public DocScannerUtil(Fragment fragment, int type) {
        this.fragment = fragment;
        this.type = type;
    }

    public interface DocScannerCallBack{
        boolean docScannerComplete(List<FileInfo> fileInfos);
    }

    String docSelection = MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? ";

    String dotSelection = MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? ";

    String pptSelection = MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? ";

    String xlsSelection = MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? ";

    String pdfSelection = MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? or "
            + MediaStore.Files.FileColumns.MIME_TYPE + " =? ";

    String selection = docSelection + " or "
            + dotSelection + " or "
            + pptSelection + " or "
            + xlsSelection + " or "
            + pdfSelection;

    String[] docSeletionArgs = new String[]{
            "application/msword",
            "application/doc",
            "appl/text",
            "application/vnd.msword",
            "application/vnd.ms-word",
            "application/winword",
            "application/word",
            "application/x-msw6",
            "application/x-msword"};
    List<String> docSelectionList = Arrays.asList(docSeletionArgs);

    String[] dotSelectionArgs = new String[]{
            "application/dot",
            "application/x-dot",
            "application/microsoft_word",
            "application/mswor2c",
            "zz-application/zz-winassoc-dot"};
    List<String> dotSelectionList = Arrays.asList(dotSelectionArgs);

    String[] pptSeletionArgs = new String[]{
            "application/mspowerpoint",
            "application/ms-powerpoint",
            "application/powerpoint",
            "application/x-powerpoint",
            "application/mspowerpnt",
            "application/vnd-mspowerpoint",
            "application/vnd.ms-powerpoint",
            "application/x-mspowerpoint",
            "application/x-m"};
    List<String> pptSelectionList = Arrays.asList(pptSeletionArgs);

    String[] xlsSeletionArgs = new String[]{
            "application/excel",
            "application/vnd.ms-excel",
            "application/msexcell",
            "application/x-msexcel",
            "application/x-excel",
            "application/x-msexcel",
            "application/x-dos_ms_excel",
            "application/xls"};
    List<String> xlsSelectionList = Arrays.asList(xlsSeletionArgs);

    String[] pdfSeletionArgs = new String[]{
            "application/pdf",
            "application/x-pdf",
            "application/acrobat",
            "applications/vnd.pdf",
            "text/pdf",
            "text/x-pdf",
            "application/x-bzpdf",
            "application/x-gzpdf"};
    List<String> pdfSelectionList = Arrays.asList(pdfSeletionArgs);

    List<String> selectionList = new ArrayList<>();
    {
        selectionList.addAll(docSelectionList);
        selectionList.addAll(dotSelectionList);
        selectionList.addAll(xlsSelectionList);
        selectionList.addAll(pdfSelectionList);
        selectionList.addAll(pptSelectionList);
    }

    String[] selectionArgs = selectionList.toArray(new String[selectionList.size()]);

    public void getDocFromLocal(final DocScannerCallBack docScannerCallBack) {
        fragment.getLoaderManager().restartLoader(type, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader cursorLoader = null;
                if (id == type) {
                    String select = "(" + MediaStore.Files.FileColumns.DATA
                            + " LIKE '%.doc'" + " or "
                            + MediaStore.Files.FileColumns.DATA
                            + " LIKE '%.docx'" + " or "
                            + MediaStore.Files.FileColumns.DATA
                            + " LIKE '%.dot'" + " or "
                            + MediaStore.Files.FileColumns.DATA
                            + " LIKE '%.dotx'" + " or "
                            + MediaStore.Files.FileColumns.DATA
                            + " LIKE '%.xls'" + " or "
                            + MediaStore.Files.FileColumns.DATA
                            + " LIKE '%.xlsx'" + " or "
                            + MediaStore.Files.FileColumns.DATA
                            + " LIKE '%.ppt'" + " or "
                            + MediaStore.Files.FileColumns.DATA
                            + " LIKE '%.pptx'" + " or "
                            + MediaStore.Files.FileColumns.DATA
                            + " LIKE '%.pdf'" + ")";

                    cursorLoader = new CursorLoader(
                            fragment.getContext(), MediaStore.Files.getContentUri("external"),
                            null, select, null,
                            FILE_PROJECTION[2] + " DESC");
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
                                String path = data.getString(data.getColumnIndexOrThrow(FILE_PROJECTION[0]));
                                if (TextUtils.isEmpty(path) || !new File(path).exists() || new File(path).isDirectory()) {
                                    continue;
                                }
                                FileInfo fileInfo = FileUtil.getFileInfoFromFile(new File(path));
                                fileInfo.fileId = data.getInt(data.getColumnIndexOrThrow(FILE_PROJECTION[3]));
                                fileInfo.setFileType(FileTypeEnum.TYPE_DOC.getFileType());
                                //TODO test
                                fileInfo.setDownloadStatus(FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADED.getFileDownloadStatus());
                                fileInfos.add(fileInfo);
                            } while (data.moveToNext());
                            docScannerCallBack.docScannerComplete(fileInfos);
                            data.close();
                        } else {
                            // 如果没有文件
                            Toast.makeText(fragment.getActivity(), "sorry,没有读取到文档文件", Toast.LENGTH_LONG).show();
                            docScannerCallBack.docScannerComplete(null);
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
