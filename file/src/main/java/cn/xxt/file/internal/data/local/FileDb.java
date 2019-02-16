package cn.xxt.file.internal.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import cn.xxt.commons.util.StringUtil;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileStatusEnum;
import cn.xxt.file.util.DateUtil;

/**
 * Created by zyj on 2017/8/16.
 */

public class FileDb extends SQLiteOpenHelper {
    //数据库
    /** 数据库名称 */
    private static final String FILE_DATABASE_NAME = "file.db";

    /** 数据库版本 */
    private static final int DATABASE_VERSION = 1;

    //表
    /** 文件信息表 */
    private static final String TABLE_M_FILE = "m_file_recv";

    //字段

    public static final String COLUMN_WEB_ID = "web_id";

    public static final String COLUMN_FILE_ID = "file_id";

    public static final String COLUMN_FILE_NAME = "file_name";

    public static final String COLUMN_FILE_SIZE = "file_size";

    public static final String COLUMN_VOICE_DURATION = "voice_duration";

    public static final String COLUMN_FILE_SUFFIX = "file_suffix";

    public static final String COLUMN_FILE_TYPE = "file_type";

    public static final String COLUMN_FILE_SERVER_PATH = "file_server_path";

    public static final String COLUMN_FILE_LOCAL_PATH = "file_local_path";

    public static final String COLUMN_DOWNLOAD_STATUS = "download_status";

    //1：发送   2：接收
    public static final String COLUMN_SEND_TYPE = "send_type";

    public static final String COLUMN_DEST_NAME = "dest_name";

    public static final String COLUMN_SEND_SOURCE = "send_source";

    public static final String COLUMN_CREATE_DATE = "create_date";

    public static final String COLUMN_UPDATE_DATE = "update_date";

    //0正常，1删除，2失效
    public static final String COLUMN_STATUS = "status";

    //建表语句
    /** 文件表 */
    private static final String CREATE_M_FILE_TABLE =
            StringUtil.connectStrings("CREATE TABLE if not exists ",
                    TABLE_M_FILE,
                    "(",
                    COLUMN_WEB_ID, " integer, ",
                    COLUMN_FILE_ID, " integer primary key, ",
                    COLUMN_FILE_NAME, " text, ",
                    COLUMN_FILE_SIZE, " integer, ",
                    COLUMN_VOICE_DURATION, " integer, ",
                    COLUMN_FILE_SUFFIX, " text, ",
                    COLUMN_FILE_TYPE, " integer, ",
                    COLUMN_FILE_SERVER_PATH, " text, ",
                    COLUMN_FILE_LOCAL_PATH, " text, ",
                    COLUMN_DOWNLOAD_STATUS, " integer, ",
                    COLUMN_SEND_TYPE, " integer, ",
                    COLUMN_DEST_NAME, " text, ",
                    COLUMN_SEND_SOURCE, " integer, ",
                    COLUMN_CREATE_DATE, " timestamp, ",
                    COLUMN_UPDATE_DATE, " timestamp, ",
                    COLUMN_STATUS, " integer",
                    ")");

    /** 上下文 */
    private Context context;

    /** 数据库实例 */
    private static FileDb instance = null;

    private FileDb(Context context) {
        super(context, FILE_DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * 单例模式
     * @param context 上下文
     * @return  数据库的单例
     */
    public static FileDb getInstance(Context context) {
        //先检查实例是否存在，如果不存在才进入下面的同步块
        if (instance == null) {
            //同步块，线程安全地创建实例
            synchronized(FileDb.class) {
                //再次检查实例是否存在，如果不存在才真正地创建实例
                if (instance == null) {
                    instance = new FileDb(context);
//                    SQLiteDatabase.loadLibs(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_M_FILE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            for(int i = 1; i + oldVersion <= newVersion; i++) {
                updateOnVersion(db, i + oldVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateOnVersion(SQLiteDatabase db, int version) {

    }

    /**
     * 插入文件信息数组
     * @param fileInfoList
     * @return
     */
    public synchronized boolean insertFileInfoList(int webId, List<FileInfo> fileInfoList) {
        boolean result = true;
        if (fileInfoList != null && fileInfoList.size() > 0) {
            SQLiteDatabase db = null;
            try {
                db = getWritableDatabase();
                //开始事务
                db.beginTransactionNonExclusive();
                for (int i = 0; i < fileInfoList.size(); i++) {
                    FileInfo fileInfo = fileInfoList.get(i);
                    result = result & insertFileInfo(db, webId, fileInfo);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    //db.close();
                }
            }
        }
        return result;
    }

    public synchronized long insertFileInfo(int webId, FileInfo fileInfo) {
        long fileId = 0;
        if (fileInfo != null) {
            SQLiteDatabase db = null;
            try {
                db = getWritableDatabase();
                //开始事务
                db.beginTransactionNonExclusive();

                boolean result = insertFileInfo(db, webId, fileInfo);

                String selectStr = StringUtil.connectStrings("select max(file_id) from ",
                        TABLE_M_FILE);
                Cursor cursor = db.rawQuery(selectStr, null);
                while (cursor.moveToNext()) {
                    int index = cursor.getInt(0);
                    fileId = Long.valueOf(index);
                }

                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    //db.close();
                }
            }
        }
        return fileId;
    }

    /**
     * 插入单条文件信息
     * @param fileInfo
     * @return
     */
    public synchronized boolean insertFileInfo(SQLiteDatabase db, int webId, FileInfo fileInfo) {
        boolean result = false;

        try {
            if (db == null || !db.isOpen()) {
                db = getWritableDatabase();
            }
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_WEB_ID, webId);
            cv.put(COLUMN_FILE_NAME, fileInfo.getFileName());
            cv.put(COLUMN_FILE_SIZE, fileInfo.getFileSize());
            cv.put(COLUMN_VOICE_DURATION, fileInfo.getVoiceDuration());
            cv.put(COLUMN_FILE_SUFFIX, fileInfo.getFileSuffix());
            cv.put(COLUMN_FILE_TYPE, fileInfo.getFileType());
            cv.put(COLUMN_FILE_SERVER_PATH, fileInfo.getFileServerPath());
            cv.put(COLUMN_FILE_LOCAL_PATH, fileInfo.getFileLocalPath());
            cv.put(COLUMN_DOWNLOAD_STATUS, fileInfo.getDownloadStatus());
            cv.put(COLUMN_SEND_TYPE, fileInfo.getSendType());
            cv.put(COLUMN_DEST_NAME, fileInfo.getDestName());
            cv.put(COLUMN_SEND_SOURCE, fileInfo.getSendSource());
            cv.put(COLUMN_CREATE_DATE, fileInfo.getCreateDate());
            cv.put(COLUMN_UPDATE_DATE, fileInfo.getUpdateDate());
            cv.put(COLUMN_STATUS, fileInfo.getStatus());

            if (!db.isOpen()) {
                db = getWritableDatabase();
            }
            result = db.insert(TABLE_M_FILE, null, cv) > -1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取所有文件
     * @return
     */
    public synchronized List<FileInfo> getAllFile(int webId){
        List<FileInfo> fileInfos = new ArrayList<>();

        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            String where = StringUtil.connectStrings(COLUMN_WEB_ID, " =? and ",
                    COLUMN_STATUS, " !=? and ",
                    COLUMN_UPDATE_DATE, " >? "
            );

            long timeNode = DateUtil.timeNodeMonthAgo(6);

//            //FIXME TEST:now
//            timeNode = 0;

            String[] whereValues = {String.valueOf(webId),
                    String.valueOf(FileStatusEnum.FILE_STATUS_DELETE.getFileStatus()),
                    String.valueOf(timeNode)
            };
            String orderBy = COLUMN_UPDATE_DATE;
            cursor = db.query(TABLE_M_FILE,
                    null,
                    where,
                    whereValues,
                    null,
                    null,
                    orderBy);

            int fileIdPosition = 0;
            int fileNamePosition = 0;
            int fileSizePosition = 0;
            int voiceDurationPosition = 0;
            int fileSuffixPosition = 0;
            int fileTypePosition = 0;
            int fileServerPathPosition = 0;
            int fileLocalPathPosition = 0;
            int downloadStatusPosition = 0;
            int sendTypePosition = 0;
            int destNamePosition = 0;
            int sendSourcePosition = 0;
            int createDatePosition = 0;
            int updateDatePosition = 0;
            int statusPosition = 0;

            if (cursor.getCount() > 0) {
                fileIdPosition = cursor.getColumnIndex(COLUMN_FILE_ID);
                fileNamePosition = cursor.getColumnIndex(COLUMN_FILE_NAME);
                fileSizePosition = cursor.getColumnIndex(COLUMN_FILE_SIZE);
                voiceDurationPosition = cursor.getColumnIndex(COLUMN_VOICE_DURATION);
                fileSuffixPosition = cursor.getColumnIndex(COLUMN_FILE_SUFFIX);
                fileTypePosition = cursor.getColumnIndex(COLUMN_FILE_TYPE);
                fileServerPathPosition = cursor.getColumnIndex(COLUMN_FILE_SERVER_PATH);
                fileLocalPathPosition = cursor.getColumnIndex(COLUMN_FILE_LOCAL_PATH);
                downloadStatusPosition = cursor.getColumnIndex(COLUMN_DOWNLOAD_STATUS);
                sendTypePosition = cursor.getColumnIndex(COLUMN_SEND_TYPE);
                destNamePosition = cursor.getColumnIndex(COLUMN_DEST_NAME);
                sendSourcePosition = cursor.getColumnIndex(COLUMN_SEND_SOURCE);
                createDatePosition = cursor.getColumnIndex(COLUMN_CREATE_DATE);
                updateDatePosition = cursor.getColumnIndex(COLUMN_UPDATE_DATE);
                statusPosition = cursor.getColumnIndex(COLUMN_STATUS);
            }
            while (cursor.moveToNext()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileId(cursor.getLong(fileIdPosition));
                fileInfo.setFileName(cursor.getString(fileNamePosition));
                fileInfo.setFileSize(cursor.getLong(fileSizePosition));
                fileInfo.setVoiceDuration(cursor.getLong(voiceDurationPosition));
                fileInfo.setFileSuffix(cursor.getString(fileSuffixPosition));
                fileInfo.setFileType(cursor.getInt(fileTypePosition));
                fileInfo.setFileServerPath(cursor.getString(fileServerPathPosition));
                fileInfo.setFileLocalPath(cursor.getString(fileLocalPathPosition));
                fileInfo.setDownloadStatus(cursor.getInt(downloadStatusPosition));
                fileInfo.setSendType(cursor.getInt(sendTypePosition));
                fileInfo.setDestName(cursor.getString(destNamePosition));
                fileInfo.setSendSource(cursor.getInt(sendSourcePosition));
                fileInfo.setCreateDate(cursor.getLong(createDatePosition));
                fileInfo.setUpdateDate(cursor.getLong(updateDatePosition));
                fileInfo.setStatus(cursor.getInt(statusPosition));
                fileInfos.add(fileInfo);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                //db.close();
            }
        }

        return fileInfos;
    }

    /**
     * 获取某类别的文件
     * @param fileType
     * @return
     */
    public synchronized List<FileInfo> getFileWithType(int webId, int fileType) {
        List<FileInfo> fileInfos = new ArrayList<>();

        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            String where = StringUtil.connectStrings(COLUMN_WEB_ID, "=? and ",
                    COLUMN_FILE_TYPE, " =? and ",
                    COLUMN_STATUS, " !=? and ",
                    COLUMN_UPDATE_DATE, ">?"
            );

            long timeNode = DateUtil.timeNodeMonthAgo(6);

//            //FIXME TEST:now
//            timeNode = 0;

            String[] whereValues = {String.valueOf(webId),
                    String.valueOf(fileType),
                    String.valueOf(FileStatusEnum.FILE_STATUS_DELETE.getFileStatus()),
                    String.valueOf(timeNode)
            };
            String orderBy = COLUMN_UPDATE_DATE;
            cursor = db.query(TABLE_M_FILE,
                    null,
                    where,
                    whereValues,
                    null,
                    null,
                    orderBy);


            int fileIdPosition = 0;
            int fileNamePosition = 0;
            int fileSizePosition = 0;
            int voiceDurationPosition = 0;
            int fileSuffixPosition = 0;
            int fileTypePosition = 0;
            int fileServerPathPosition = 0;
            int fileLocalPathPosition = 0;
            int downloadStatusPosition = 0;
            int sendTypePosition = 0;
            int destNamePosition = 0;
            int sendSourcePosition = 0;
            int createDatePosition = 0;
            int updateDatePosition = 0;
            int statusPosition = 0;

            if (cursor.getCount() > 0) {
                fileIdPosition = cursor.getColumnIndex(COLUMN_FILE_ID);
                fileNamePosition = cursor.getColumnIndex(COLUMN_FILE_NAME);
                fileSizePosition = cursor.getColumnIndex(COLUMN_FILE_SIZE);
                voiceDurationPosition = cursor.getColumnIndex(COLUMN_VOICE_DURATION);
                fileSuffixPosition = cursor.getColumnIndex(COLUMN_FILE_SUFFIX);
                fileTypePosition = cursor.getColumnIndex(COLUMN_FILE_TYPE);
                fileServerPathPosition = cursor.getColumnIndex(COLUMN_FILE_SERVER_PATH);
                fileLocalPathPosition = cursor.getColumnIndex(COLUMN_FILE_LOCAL_PATH);
                downloadStatusPosition = cursor.getColumnIndex(COLUMN_DOWNLOAD_STATUS);
                sendTypePosition = cursor.getColumnIndex(COLUMN_SEND_TYPE);
                destNamePosition = cursor.getColumnIndex(COLUMN_DEST_NAME);
                sendSourcePosition = cursor.getColumnIndex(COLUMN_SEND_SOURCE);
                createDatePosition = cursor.getColumnIndex(COLUMN_CREATE_DATE);
                updateDatePosition = cursor.getColumnIndex(COLUMN_UPDATE_DATE);
                statusPosition = cursor.getColumnIndex(COLUMN_STATUS);
            }
            while (cursor.moveToNext()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileId(cursor.getLong(fileIdPosition));
                fileInfo.setFileName(cursor.getString(fileNamePosition));
                fileInfo.setFileSize(cursor.getLong(fileSizePosition));
                fileInfo.setVoiceDuration(cursor.getLong(voiceDurationPosition));
                fileInfo.setFileSuffix(cursor.getString(fileSuffixPosition));
                fileInfo.setFileType(cursor.getInt(fileTypePosition));
                fileInfo.setFileServerPath(cursor.getString(fileServerPathPosition));
                fileInfo.setFileLocalPath(cursor.getString(fileLocalPathPosition));
                fileInfo.setDownloadStatus(cursor.getInt(downloadStatusPosition));
                fileInfo.setSendType(cursor.getInt(sendTypePosition));
                fileInfo.setDestName(cursor.getString(destNamePosition));
                fileInfo.setSendSource(cursor.getInt(sendSourcePosition));
                fileInfo.setCreateDate(cursor.getLong(createDatePosition));
                fileInfo.setUpdateDate(cursor.getLong(updateDatePosition));
                fileInfo.setStatus(cursor.getInt(statusPosition));
                fileInfos.add(fileInfo);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                //db.close();
            }
        }

        return fileInfos;
    }

    public synchronized List<FileInfo> getFileWithinToday(int webId) {
        List<FileInfo> fileInfos = new ArrayList<>();

        return fileInfos;
    }

    public synchronized List<FileInfo> getFileWithinYesterday(int webId) {
        List<FileInfo> fileInfos = new ArrayList<>();

        return fileInfos;
    }

    public synchronized List<FileInfo> getFileWithinWeek(int webId) {
        List<FileInfo> fileInfos = new ArrayList<>();

        return fileInfos;
    }

    public synchronized List<FileInfo> getFileWithinMonth(int webId) {
        List<FileInfo> fileInfos = new ArrayList<>();

        return fileInfos;
    }

    public synchronized List<FileInfo> getFileWithinSixMonth(int webId) {
        List<FileInfo> fileInfos = new ArrayList<>();

        return fileInfos;
    }

    /**
     * 获取单个文件信息
     * @param webId
     * @param fileId
     * @return
     */
    public synchronized FileInfo getFileWithFileId(int webId, long fileId) {
        FileInfo fileInfo = new FileInfo();

        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            String where = StringUtil.connectStrings(COLUMN_WEB_ID, "=? and ",
                    COLUMN_FILE_ID, " =?"
            );
            String[] whereValues = {String.valueOf(webId),
                    String.valueOf(fileId)
            };
            String orderBy = COLUMN_UPDATE_DATE;
            cursor = db.query(TABLE_M_FILE,
                    null,
                    where,
                    whereValues,
                    null,
                    null,
                    orderBy);

            int fileIdPosition = 0;
            int fileNamePosition = 0;
            int fileSizePosition = 0;
            int voiceDurationPosition = 0;
            int fileSuffixPosition = 0;
            int fileTypePosition = 0;
            int fileServerPathPosition = 0;
            int fileLocalPathPosition = 0;
            int downloadStatusPosition = 0;
            int sendTypePosition = 0;
            int destNamePosition = 0;
            int sendSourcePosition = 0;
            int createDatePosition = 0;
            int updateDatePosition = 0;
            int statusPosition = 0;

            if (cursor.getCount() > 0) {
                fileIdPosition = cursor.getColumnIndex(COLUMN_FILE_ID);
                fileNamePosition = cursor.getColumnIndex(COLUMN_FILE_NAME);
                fileSizePosition = cursor.getColumnIndex(COLUMN_FILE_SIZE);
                voiceDurationPosition = cursor.getColumnIndex(COLUMN_VOICE_DURATION);
                fileSuffixPosition = cursor.getColumnIndex(COLUMN_FILE_SUFFIX);
                fileTypePosition = cursor.getColumnIndex(COLUMN_FILE_TYPE);
                fileServerPathPosition = cursor.getColumnIndex(COLUMN_FILE_SERVER_PATH);
                fileLocalPathPosition = cursor.getColumnIndex(COLUMN_FILE_LOCAL_PATH);
                downloadStatusPosition = cursor.getColumnIndex(COLUMN_DOWNLOAD_STATUS);
                sendTypePosition = cursor.getColumnIndex(COLUMN_SEND_TYPE);
                destNamePosition = cursor.getColumnIndex(COLUMN_DEST_NAME);
                sendSourcePosition = cursor.getColumnIndex(COLUMN_SEND_SOURCE);
                createDatePosition = cursor.getColumnIndex(COLUMN_CREATE_DATE);
                updateDatePosition = cursor.getColumnIndex(COLUMN_UPDATE_DATE);
                statusPosition = cursor.getColumnIndex(COLUMN_STATUS);
            }
            while (cursor.moveToNext()) {
                fileInfo.setFileId(cursor.getLong(fileIdPosition));
                fileInfo.setFileName(cursor.getString(fileNamePosition));
                fileInfo.setFileSize(cursor.getLong(fileSizePosition));
                fileInfo.setVoiceDuration(cursor.getLong(voiceDurationPosition));
                fileInfo.setFileSuffix(cursor.getString(fileSuffixPosition));
                fileInfo.setFileType(cursor.getInt(fileTypePosition));
                fileInfo.setFileServerPath(cursor.getString(fileServerPathPosition));
                fileInfo.setFileLocalPath(cursor.getString(fileLocalPathPosition));
                fileInfo.setDownloadStatus(cursor.getInt(downloadStatusPosition));
                fileInfo.setSendType(cursor.getInt(sendTypePosition));
                fileInfo.setDestName(cursor.getString(destNamePosition));
                fileInfo.setSendSource(cursor.getInt(sendSourcePosition));
                fileInfo.setCreateDate(cursor.getLong(createDatePosition));
                fileInfo.setUpdateDate(cursor.getLong(updateDatePosition));
                fileInfo.setStatus(cursor.getInt(statusPosition));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                //db.close();
            }
        }

        return fileInfo;
    }

    /**
     * 更新文件下载状态
     * @param downloadStatus
     * @return
     */
    public synchronized boolean updateFileDownloadStatus(int webId, long fileId, int downloadStatus) {
        boolean result = false;

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_DOWNLOAD_STATUS, downloadStatus);
            String where = StringUtil.connectStrings(COLUMN_WEB_ID, "=? and ",
                    COLUMN_FILE_ID, "=? "
            );
            String[] whereValues = {String.valueOf(webId), String.valueOf(fileId)};
            result =  db.update(TABLE_M_FILE, cv, where, whereValues) > -1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                //db.close();
            }
        }

        return result;
    }

    /**
     * 更新文件状态
     * @param status
     * @return
     */
    public synchronized boolean updateFileStatus(int webId, long fileId, int status) {
        boolean result = false;

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_STATUS, status);
            String where = StringUtil.connectStrings(COLUMN_WEB_ID, " =? and ",
                    COLUMN_FILE_ID, " =? "
            );
            String[] whereValues = {String.valueOf(webId), String.valueOf(fileId)};
            result =  db.update(TABLE_M_FILE, cv, where, whereValues) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                //db.close();
            }
        }

        return result;
    }

    /**
     * 批量删除文件
     * @param ids
     * @return
     */
    public synchronized boolean deleteFileByFileIdList(int webId, List<Long> ids) {
        boolean result = true;

        if (ids != null && ids.size() > 0) {
            SQLiteDatabase db = null;
            try {
                db = getWritableDatabase();
                //开始事务
                db.beginTransactionNonExclusive();
                for (int i = 0; i < ids.size(); i++) {
                    long fileId = ids.get(i);
                    result = result & deleteFileByFileId(webId, fileId);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    //db.close();
                }
            }
        }

        return result;
    }

    /**
     * 删除文件
     * @param fileId
     * @return
     */
    public synchronized boolean deleteFileByFileId(int webId, long fileId) {
        boolean result = false;

        result = updateFileStatus(webId, fileId, FileStatusEnum.FILE_STATUS_DELETE.getFileStatus());

        return result;
    }

    /**
     * 清除过期的文件信息
     * @return
     */
    public synchronized boolean clearExpirFile() {
        boolean result = false;



        return result;
    }

    /**
     * 获取文件信息表中最大fileId
     * @return
     */
    private synchronized long getFileMaxId() {
        long fileId = 0;
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();

            if (!db.isOpen()) {
                db = getWritableDatabase();
            }

            String selectStr = StringUtil.connectStrings("select max(file_id) from ",
                    TABLE_M_FILE);
            Cursor cursor = db.rawQuery(selectStr, null);
            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndex(COLUMN_FILE_ID);
                fileId = Long.valueOf(cursor.getString(index));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileId;
    }
}
