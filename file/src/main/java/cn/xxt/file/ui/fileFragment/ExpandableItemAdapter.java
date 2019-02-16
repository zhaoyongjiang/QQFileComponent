package cn.xxt.file.ui.fileFragment;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.xxt.commons.util.BeanCopyUtil;
import cn.xxt.commons.util.DateUtil;
import cn.xxt.commons.util.RxBusWithTag;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.commons.util.ToastUtil;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.file.R;
import cn.xxt.file.internal.data.local.FileDb;
import cn.xxt.file.internal.domain.FileDownloadStatusEnum;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.file.internal.domain.FolderItem;
import cn.xxt.file.internal.domain.ItemUiTypeEnum;
import cn.xxt.file.ui.base.FileBaseActivity;
import cn.xxt.file.ui.manager.FileOpenActivity;
import cn.xxt.file.ui.view.CheckBox;
import cn.xxt.file.ui.view.FileCornerImageView;
import cn.xxt.file.util.FileDownloadManager;
import cn.xxt.file.util.FileUtil;
import cn.xxt.file.util.MediaStoreUtil;
import cn.xxt.file.util.TaskDispatcher;
import cn.xxt.file.util.TransmitClass;
import cn.xxt.file.util.UiUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zyj on 2017/8/17.
 */

public class ExpandableItemAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    public static final int HEAD = 0;
    public static final int PHOTO = 1;
    public static final int FILE = 2;

    public void setItemUiType(int itemUiType) {
        this.itemUiType = itemUiType;
    }

    private int itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_CHECKBOX_CONTENT.getItemUiType();

    private Context context;

    private List<MultiItemEntity> fileInfoDataSource = new ArrayList<>();
    private List<FileInfo> selectedFileInfoList = new ArrayList<>();

    public void setOnExpandableItemAdapterListener(OnExpandableItemAdapterListener onExpandableItemAdapterListener) {
        this.onExpandableItemAdapterListener = onExpandableItemAdapterListener;
    }

    private OnExpandableItemAdapterListener onExpandableItemAdapterListener;

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public ExpandableItemAdapter(Context context, List<MultiItemEntity> data, List<FileInfo> selectedFileInfoList, int itemUiType) {
        super(data);
        this.fileInfoDataSource = data;
        this.selectedFileInfoList = selectedFileInfoList;
        this.itemUiType = itemUiType;
        this.context = context;

        //添加adapter中所以可能的文件视图类型
        addItemType(HEAD, R.layout.item_head_file);
        addItemType(FILE, R.layout.item_file);
        addItemType(PHOTO, R.layout.item_photo_file);
    }

    public interface OnExpandableItemAdapterListener {
        void onPhotoItemClickedToPreview(FileInfo fileInfo);

        void onFileItemClickedToOpen( FileInfo fileInfo);

        void onFileLongPressToDelete(FileInfo fileInfo);
    }

    @Override
    protected void convert(final BaseViewHolder helper, MultiItemEntity item) {
        FileInfo fileInfo = null;
        if (helper.getItemViewType() != HEAD) {
            fileInfo = (FileInfo) item;

            if (helper.getItemViewType() == PHOTO) {
                helper.setVisible(R.id.v_cover, false);
                helper.setVisible(R.id.cb_file, false);
                ((CheckBox) helper.getView(R.id.cb_file)).setChecked(true, false);
            }

            if (selectedFileInfoList.contains(fileInfo)) {
                ((CheckBox) helper.getView(R.id.cb_file)).setChecked(true, false);

                if (helper.getItemViewType() == PHOTO) {
                    helper.setVisible(R.id.v_cover, true);
                    helper.setVisible(R.id.cb_file, true);
                }
            } else {
                ((CheckBox) helper.getView(R.id.cb_file)).setChecked(false, false);

                if (helper.getItemViewType() == PHOTO) {
                    helper.setVisible(R.id.v_cover, false);
                    helper.setVisible(R.id.cb_file, false);
                }
            }
        }
        switch (helper.getItemViewType()) {
            case HEAD:
                final FolderItem folderItem = (FolderItem)item;
                helper.setText(R.id.tv_title, folderItem.getTitle());
                if (folderItem.isExpanded()) {
                    ((IconFontTextView)helper.getView(R.id.iftv_expand)).setText(R.string.iconfont_arrow_down);
                } else {
                    ((IconFontTextView)helper.getView(R.id.iftv_expand)).setText(R.string.iconfont_arrow_right);
                }
                Log.d("headposition:", helper.getAdapterPosition() + " " + folderItem.isExpanded());
                helper.getConvertView().setTag(helper.getAdapterPosition());
//                helper.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        int pos = helper.getAdapterPosition();
//                        if (folderItem.isExpanded()) {
//                            collapse(pos);
//                        } else {
//                            expand(pos);
//                        }
//                    }
//                });
                break;
            case PHOTO:
                if (fileInfo.getFileLocalPath().length() > 0) {
                    Picasso.get()
                            .load(StringUtil.connectStrings("file://",fileInfo.getFileLocalPath()))
                            .placeholder(R.drawable.bg_placeholder_loading_small)
                            .error(R.drawable.bg_placeholder_load_fail_small)
                            .fit()
                            .into((ImageView) helper.getView(R.id.ri_image));
                } else if (fileInfo.getFileServerPath().length() > 0) {
                    String newFileUrl = StringUtil.urlStr2HttpsUrlStr(fileInfo.getFileServerPath());
                    Picasso.get()
                            .load(newFileUrl)
                            .placeholder(R.drawable.bg_placeholder_loading)
                            .error(R.drawable.bg_placeholder_load_fail)
                            .fit()
                            .into((ImageView) helper.getView(R.id.ri_image));
                } else {
                    Picasso.get()
                            .load(R.drawable.bg_placeholder_load_fail)
                            .into((ImageView) helper.getView(R.id.ri_image));
                }
                break;
            case FILE:
                String formatDateStr1 = DateUtil.format(new Date(fileInfo.getUpdateDate()),
                        DateUtil.DATE_FORMAT_STRING_YMDHMS);
                helper.setText(R.id.tv_file_name, fileInfo.getFileName())
                        .setText(R.id.tv_size, FileUtil.FormetFileSize(fileInfo.getFileSize()))
                        .setText(R.id.tv_time, formatDateStr1);
                if (fileInfo.fileType == FileTypeEnum.TYPE_IMAGE.getFileType()) {
                    helper.getView(R.id.ri_image).setVisibility(View.VISIBLE);
                    if (fileInfo.getFileServerPath() != null && fileInfo.getFileServerPath().length() > 0) {
                        Picasso.get()
                                .load(fileInfo.getFileServerPath())
                                .placeholder(R.drawable.bg_placeholder_loading_small)
                                .error(R.drawable.bg_placeholder_load_fail_small)
                                .resize(60, 60)
                                .into((ImageView) helper.getView(R.id.ri_image));
                    } else {
                        Picasso.get()
                                .load(StringUtil.connectStrings("file://",fileInfo.getFileLocalPath()))
                                .placeholder(R.drawable.bg_placeholder_loading_small)
                                .error(R.drawable.bg_placeholder_load_fail_small)
                                .resize(60, 60)
                                .into((ImageView) helper.getView(R.id.ri_image));
                    }
                } else {
//                    helper.getView(R.id.ri_image).setVisibility(View.GONE);
//                    ((IconFontTextView)helper.getView(R.id.iftv_file_icon)).setText(FileUtil.getFileIconCodeByFileType(context
//                            , fileInfo.fileType));
//                    ((IconFontTextView)helper.getView(R.id.iftv_file_icon)).setTextColor(ContextCompat.getColor(context
//                            , FileUtil.getFileIconColorByFileType(context, fileInfo.fileType)));
                    ((FileCornerImageView)helper.getView(R.id.ri_image)).setImageResource(FileUtil.getResourceIdByFileType(context, fileInfo.fileType));
                }

                if (itemUiType == ItemUiTypeEnum.ITEM_UI_TYPE_CONTENT.getItemUiType()) {
                    helper.getView(R.id.cb_file).setVisibility(View.GONE);
                    helper.getView(R.id.rl_file_oper).setVisibility(View.GONE);
                } else if (itemUiType == ItemUiTypeEnum.ITEM_UI_TYPE_CHECKBOX_CONTENT.getItemUiType()) {
                    helper.getView(R.id.cb_file).setVisibility(View.VISIBLE);
                    helper.getView(R.id.rl_file_oper).setVisibility(View.GONE);
                } else if (itemUiType == ItemUiTypeEnum.ITEM_UI_TYPE_CONTENT_STATUS.getItemUiType()) {
                    helper.getView(R.id.cb_file).setVisibility(View.GONE);
                    helper.getView(R.id.rl_file_oper).setVisibility(View.VISIBLE);

                    refreshItemViews(helper, fileInfo);
                }

                break;
            default:
                break;
        }

        registViewsClick(helper, item);

        List<FileInfo> fileInfoList = new ArrayList<>();
        fileInfoList.add(fileInfo);
        batchDownloadFileWithFileList(helper, fileInfoList);
    }

    /**
     *
     * 业务功能：批量下载文件。多任务并发控制。并发4。并发控制器是单例的。能实现不同fragment多文件并发控制
     *
     * 设计：
     * 1：由于adapter共用，不同文件fragment都是用该adapter。
     * 2：事件入口为"管理器-最近"中下载按钮。点击后，要通知到其上的fragment（FileRecentMainFragment）。
     * 然后，将事件分发到各个分类文件fragment。最后，文件fragment调用adapter方法进行批量下载。
     *
     * 3：adapter中同时进行多任务下载操作。跟下载界面还有下载进度交互。
     * 采用：adapter内通过map管理多下载器。以fileinfo的url为key。
     * （ps：也考虑了用fileId为key，但是记得之前看过glide的源码，其进行缓存就是用url作为key，不过人也只能用url了）
     *
     * 4：异常测试。~~~~~~~~TODO
     *
     * @param helper
     * @param fileInfoList
     */
    public void batchDownloadFileWithFileList(final BaseViewHolder helper
            , final List<FileInfo> fileInfoList) {
        if (fileInfoList == null
                || fileInfoList.size() == 0) {
            return;
        } else {
            //备注：批量下载。是通过将模型的下载状态改为下载中，通知到adapter进行执行的。
            for (int i = 0; i < fileInfoList.size(); i++) {
                final FileInfo fileInfo = fileInfoList.get(i);
                if (fileInfo != null
                        && fileInfo.getFileServerPath() != null
                        && fileInfo.getFileServerPath().length() >0) {
                    if (fileInfo.getDownloadStatus()
                            == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_READY.getFileDownloadStatus()) {

                        if (getFileDownloadManager(fileInfo) == null) {
                            TaskDispatcher taskDispatcher = TaskDispatcher.getInstance();

                            taskDispatcher.doTask(new Runnable() {
                                @Override
                                public void run() {
                                    //                                    Looper.prepare();
                                    Observable.just(1)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Subscriber<Integer>() {
                                                @Override
                                                public void onCompleted() {

                                                }

                                                @Override
                                                public void onError(Throwable e) {

                                                }

                                                @Override
                                                public void onNext(Integer integer) {
                                                    //下载
                                                    FileDownloadManager fileDownloadManager = downloadFile(helper, fileInfo);

                                                    //保存下载器
                                                    long fileId = fileInfo.getFileId();
                                                    TransmitClass.fileDownloadManagerMap.put(fileId, fileDownloadManager);
                                                }
                                            });

                                    //                                    Looper.loop();
                                }
                            });
                        } else {
                            updateDownloaderListener(helper, fileInfo, getFileDownloadManager(fileInfo));
                        }
                    }
                }
            }
        }
    }

    /**
     * 通过文件模型属性：是否下载标识，路径，url等。处理item上文件操作按钮的状态：下载，下载中，查看，失效
     * @param helper
     * @param fileInfo
     */
    private void refreshItemViews(BaseViewHolder helper, FileInfo fileInfo) {
        helper.setVisible(R.id.btn_ready_download, false);
        helper.setVisible(R.id.btn_download, false);
        helper.setVisible(R.id.btn_downloading, false);
        helper.setVisible(R.id.btn_look, false);
        helper.setVisible(R.id.btn_unvailible, false);
        helper.setVisible(R.id.pb_download, false);

        if (fileInfo.downloadStatus == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADED.getFileDownloadStatus()) {
            if (FileUtil.isFileExits(fileInfo)) {
                helper.setVisible(R.id.btn_look, true);
            } else if (fileInfo.getFileServerPath() != null
                    && fileInfo.getFileServerPath().length() > 0) {
                helper.setVisible(R.id.btn_download, true);
            } else {
                helper.setVisible(R.id.btn_unvailible, true);
            }
        } else if (fileInfo.downloadStatus == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_NOT_YET.getFileDownloadStatus()){
            //未下载
            if (FileUtil.isFileExits(fileInfo)) {
                helper.setVisible(R.id.btn_look, true);
            } else if (fileInfo.getFileServerPath() != null
                    && fileInfo.getFileServerPath().length() > 0) {
                helper.setVisible(R.id.btn_download, true);
            } else {
                helper.setVisible(R.id.btn_unvailible, true);
            }
        } else if (fileInfo.downloadStatus == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_READY.getFileDownloadStatus()) {
            //准备下载
            helper.setVisible(R.id.btn_ready_download, true);
        } else {
            //下载中
            //FIXME 是否处理下载链接是否存在问题
            helper.setVisible(R.id.btn_downloading, true);

            helper.setVisible(R.id.pb_download, true);
        }

        helper.setText(R.id.tv_file_name, fileInfo.getFileName());
    }

    private void registViewsClick(final BaseViewHolder helper, final MultiItemEntity item) {
        if (helper.getItemViewType() == FILE) {
            //下载
            RxView.clicks(helper.getView(R.id.btn_download))
                    .throttleFirst(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Void>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(Void aVoid) {
                            FileInfo fileInfo = (FileInfo)item;
                            fileInfo.setDownloadStatus(FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_READY.getFileDownloadStatus());
                            refreshItemViews(helper, fileInfo);

                            List<FileInfo> fileInfoList = new ArrayList<>();
                            fileInfoList.add(fileInfo);
                            batchDownloadFileWithFileList(helper, fileInfoList);
                        }
                    });

            //查看
            RxView.clicks(helper.getView(R.id.btn_look))
                    .throttleFirst(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Void>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(Void aVoid) {
                            FileInfo fileInfo = (FileInfo)item;

                            if (fileInfo.getFileType() == FileTypeEnum.TYPE_IMAGE.getFileType()) {
                                gotoPhotoPreview(fileInfo);
                            } else {
                                gotoFileOpenActivity(helper, fileInfo);
                            }
                        }
                    });
        }

        RxView.clicks(helper.itemView)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        if (helper.getItemViewType() == HEAD) {
                            FolderItem folderItem = (FolderItem)item;
                            int pos = helper.getAdapterPosition();
                            if (folderItem.isExpanded()) {
                                collapse(pos);
                            } else {
                                expand(pos);
                            }
                        } else if (helper.getItemViewType() == FILE) {
                            //点击的是file的item
                            FileInfo fileInfo = (FileInfo)item;
                            if (itemUiType == ItemUiTypeEnum.ITEM_UI_TYPE_CHECKBOX_CONTENT.getItemUiType()) {
                                selectFileItem(helper, fileInfo);
                            } else if (fileInfo.getDownloadStatus() == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_READY.getFileDownloadStatus()) {
                                //准备下载
                                ToastUtil.displayToastShort(context, "准备下载，无法操作");
                            } else {
                                //非编辑状态
                                if (fileInfo.fileType == FileTypeEnum.TYPE_IMAGE.getFileType()) {
                                    //图片
                                    gotoPhotoPreview(fileInfo);
                                } else {
                                    //其他文件 其他操作
                                    gotoFileOpenActivity(helper, fileInfo);
                                }

                                //TODO 注意测试：刚下载的文件，去本机分类中，看一下，是否能获取到。如果真不能，还需要手动将文件导入到多媒体库
                            }
                        } else if (helper.getItemViewType() == PHOTO) {
                            if (itemUiType == ItemUiTypeEnum.ITEM_UI_TYPE_PHOTO_NORMAL.getItemUiType()) {
                                //默认状态
                                gotoPhotoPreview((FileInfo)item);
                            } else {
                                //编辑状态
                                selectFileItem(helper, (FileInfo)item);

                            }
                        }
                    }
                });

        //长按文件item：编辑状态，长按同点击。正常状态，长按弹框删除
        RxView.longClicks(helper.itemView)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Void aVoid) {
                        if (itemUiType != ItemUiTypeEnum.ITEM_UI_TYPE_CHECKBOX_CONTENT.getItemUiType()) {
                            showItemDealPopUpList(helper, (FileInfo)item);
                        } else {
                            selectFileItem(helper, (FileInfo)item);
                        }
                    }
                });
    }

    private void selectFileItem(BaseViewHolder helper, FileInfo fileInfo) {
        Map<String, Object> rxMap = new HashMap<>();
        rxMap.put(FileBaseActivity.KEY_FILE_INFO, fileInfo);
        RxBusWithTag.getInstance().send(FileBaseActivity.FILE_RXBUS_TAG, rxMap);
    }

    private FileDownloadManager downloadFile(final BaseViewHolder helper, final FileInfo fileInfo) {
        FileDownloadManager downloadManager = new FileDownloadManager(fileInfo.getFileId(), null, fileInfo.getFileName(), fileInfo.fileServerPath, context);
        downloadManager.downloadFile(new FileDownloadManager.OnFileDownloadListener() {
            @Override
            public void onDownloadBegin(long fileId, String fileLocalPath) {
                //下载中
                dispatchBeginToMainThread(helper, fileInfo, fileLocalPath);
            }

            @Override
            public void onDownloadProgress(int percent) {
                dispatchProgressToMainThread(helper, fileInfo, percent);
            }

            @Override
            public void onDownloadPause(int percent) {

            }

            @Override
            public void onDownloadComplete(String url, long fileId, String fileLocalPath, String fileSaveName) {
                dispatchCompleteToMainThread(helper, fileInfo, url, fileLocalPath, fileSaveName);
            }

            @Override
            public void onDownloadError(String url) {
//                helper.setVisible(R.id.pb_download, false);
//
//                updateFileDownloadStatus(fileInfo, FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_NOT_YET.getFileDownloadStatus());
//
//                refreshItemViews(helper, fileInfo);
//
//                fileDownloadOverHandle(fileInfo);

                dispatchErrorToMainThread(helper, fileInfo, url);
            }
        });

        return downloadManager;
    }

    private void updateDownloaderListener(final BaseViewHolder helper,
                                          final FileInfo fileInfo,
                                          final FileDownloadManager fileDownloadManager) {
        if (fileInfo.getFileServerPath() == null
                || fileInfo.getFileServerPath().length() == 0
                || fileDownloadManager == null) {
            return;
        }

        //场景：进入下载页-开始下载-返回。这时候要先将ui显示出来
        refreshItemViews(helper, fileInfo);

        fileDownloadManager.updateDownLoadListener(new FileDownloadManager.OnFileDownloadListener() {
            @Override
            public void onDownloadBegin(long fileId, String fileLocalPath) {
                dispatchBeginToMainThread(helper, fileInfo, fileLocalPath);
            }

            @Override
            public void onDownloadProgress(int percent) {
                dispatchProgressToMainThread(helper, fileInfo, percent);
            }

            @Override
            public void onDownloadPause(int percent) {

            }

            @Override
            public void onDownloadComplete(String url, long fileId, String fileLocalPath, String fileSaveName) {
//                fileInfo.setFileLocalPath(fileLocalPath);
//                fileInfo.setFileName(fileSaveName);
//
//                helper.setVisible(R.id.pb_download, false);
//                updateFileDownloadStatus(fileInfo, FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADED.getFileDownloadStatus());
//
//                refreshItemViews(helper, fileInfo);
//
//                fileDownloadOverHandle(fileInfo);

                dispatchCompleteToMainThread(helper, fileInfo, url, fileLocalPath, fileSaveName);
            }

            @Override
            public void onDownloadError(String url) {
//                helper.setVisible(R.id.pb_download, false);
//
//                updateFileDownloadStatus(fileInfo, FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_NOT_YET.getFileDownloadStatus());
//
//                refreshItemViews(helper, fileInfo);
//
//                fileDownloadOverHandle(fileInfo);

                dispatchErrorToMainThread(helper, fileInfo, url);
            }
        });
    }

    private FileDownloadManager getFileDownloadManager(FileInfo fileInfo) {
        FileDownloadManager fileDownloadManager = null;
        if (fileInfo != null && fileInfo.getFileServerPath() != null && fileInfo.getFileServerPath().length() > 0) {
            fileDownloadManager = TransmitClass.fileDownloadManagerMap.get(fileInfo.getFileId());
        }

        return fileDownloadManager;
    }

    private void fileDownloadOverHandle(FileInfo fileInfo) {
        if (fileInfo != null && fileInfo.getFileServerPath() != null
                && fileInfo.getFileServerPath().length() > 0) {
            TransmitClass.fileDownloadManagerMap.remove(fileInfo.getFileId());

            //并发释放信号量,允许下一个下载线程启动
            TaskDispatcher.getInstance().releaseSemaphore();
        }
    }

    private void updateFileDownloadStatus(final FileInfo fileInfo) {
        Observable.just(1)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        FileDb fileDb = FileDb.getInstance(context);
                        fileDb.updateFileDownloadStatus(0, fileInfo.getFileId(), fileInfo.getDownloadStatus());
                    }
                });
    }

    /**
     * item点击回调，跳转到文件打开统一处理界面
     *
     * 跳转到文件打开界面，如果是下载文件，需要item跟下载界面保持同步，中间桥梁就是下载管理器。
     *
     * 所以：跳转到下载界面，将下载管理器以文件id为key保存下载器，回来时候，用文件id为key取到下载器，再updateDownLoadListener更新一下监听。
     *
     * @param helper
     * @param fileInfo
     */
    private void gotoFileOpenActivity(final BaseViewHolder helper, final FileInfo fileInfo) {
        //注册监听：下载器回传
        registRxBus(helper, fileInfo);

        if (onExpandableItemAdapterListener != null) {
            onExpandableItemAdapterListener.onFileItemClickedToOpen(fileInfo);
        }
    }

    private void gotoPhotoPreview(final FileInfo fileInfo) {
        if (onExpandableItemAdapterListener != null) {
            onExpandableItemAdapterListener.onPhotoItemClickedToPreview(fileInfo);
        }
    }

    private void registRxBus(final BaseViewHolder helper, final FileInfo fileInfo) {
        Observable<Map<String, Object>> rxObservable = RxBusWithTag.getInstance().register(String.valueOf(fileInfo.getFileId()));

        rxObservable.subscribeOn(Schedulers.io());
        rxObservable.observeOn(AndroidSchedulers.mainThread());
        rxObservable.subscribe(new Subscriber<Map<String, Object>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Map<String, Object> objectMap) {
                if (null != objectMap) {
                    final FileInfo tmpFileInfo = (FileInfo) objectMap.get(FileOpenActivity.BUNDLE_FILEINFO);
                    if (tmpFileInfo.equals(fileInfo)) {
                        //已经重写了文件模型的equals方法，内部比较的是fileId

                        BeanCopyUtil.copy(fileInfo, tmpFileInfo, false);

                        if (fileInfo.getDownloadStatus() == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADING.getFileDownloadStatus()) {
                            //FIXME need test 是否能拿到正确的下载器
//                            FileDownloadManager fileDownloadManager = (FileDownloadManager) objectMap.get(FileOpenActivity.BUNDLE_DOWNLOAD_MANAGER);

                            final FileDownloadManager fileDownloadManager = TransmitClass.fileDownloadManagerMap.get(fileInfo.getFileId());

                            updateDownloaderListener(helper, fileInfo, fileDownloadManager);
                        } else {
                            //从下载界面返回来，如果非下载状态，更新一下界面，：
                            // 场景：下载中，点击跳到下载界面，下载完成，
                            // 回来。这个时候下载监听就监听不到了。所以，只能这边强制更新下界面

                            if (fileInfoDataSource != null && fileInfoDataSource.size() > 0) {
                                int index = fileInfoDataSource.indexOf(fileInfo);
                                if (index >= 0) {
                                    notifyItemChanged(index);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 长按信息弹出menu:删除
     * @param helper
     * @param fileInfo
     */
    private void showItemDealPopUpList(final BaseViewHolder helper, final FileInfo fileInfo) {
        UiUtil.showPopMenu(context, helper, fileInfo, new UiUtil.PopMenuInterface() {
            @Override
            public void onItemClicked() {
//                if (onExpandableItemAdapterListener != null) {
//                    onExpandableItemAdapterListener.onFileLongPressToDelete(fileInfo);
//                }

                //单张删除，这种方式也行。上面方法为了和批量删除保持统一，但是动作太大
                int posi = helper.getAdapterPosition();
                remove(posi);

                FileUtil.deleteFile(fileInfo.getFileLocalPath());
                MediaStoreUtil.updateMediaStore(context, fileInfo.getFileLocalPath());
            }
        });
    }

    private void dispatchBeginToMainThread(final BaseViewHolder helper
            , final FileInfo fileInfo
            , final String fileLocalPath) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        //下载中
                        fileInfo.downloadStatus = FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADING.getFileDownloadStatus();
                        fileInfo.setFileLocalPath(fileLocalPath);
                        refreshItemViews(helper, fileInfo);

                        updateFileDownloadStatus(fileInfo);
                    }
                });
    }

    private void dispatchProgressToMainThread(final BaseViewHolder helper
            , final FileInfo fileInfo
            , final int percent) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        helper.setProgress(R.id.pb_download, percent);
                        Log.i("progress:", helper.toString() + " /n " + R.id.pb_download);
                    }
                });
    }

    private void dispatchCompleteToMainThread(final BaseViewHolder helper
            , final FileInfo fileInfo
            , final String url
            , final String fileLocalPath
            , final String fileSaveName) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        fileInfo.downloadStatus = FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADED.getFileDownloadStatus();
                        fileInfo.setFileLocalPath(fileLocalPath);
                        fileInfo.setFileName(fileSaveName);
                        refreshItemViews(helper, fileInfo);

                        updateFileDownloadStatus(fileInfo);

                        fileDownloadOverHandle(fileInfo);
                    }
                });
    }

    private void dispatchErrorToMainThread(final BaseViewHolder helper
            , final FileInfo fileInfo
            , final String url) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        fileInfo.downloadStatus = FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_NOT_YET.getFileDownloadStatus();
                        refreshItemViews(helper, fileInfo);

                        updateFileDownloadStatus(fileInfo);

                        fileDownloadOverHandle(fileInfo);
                    }
                });
    }
}
