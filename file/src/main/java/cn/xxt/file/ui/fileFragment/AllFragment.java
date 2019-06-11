package cn.xxt.file.ui.fileFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.ui.image.ImageShowActivity;
import cn.xxt.commons.util.ActivityUtil;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.internal.data.local.FileDb;
import cn.xxt.file.internal.domain.FileDownloadStatusEnum;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.file.internal.domain.FolderInfo;
import cn.xxt.file.internal.domain.FolderItem;
import cn.xxt.file.internal.domain.ItemUiTypeEnum;
import cn.xxt.file.ui.base.FileBaseFragment;
import cn.xxt.file.ui.manager.FileLocalMainActivity;
import cn.xxt.file.ui.manager.FileOpenActivity;
import cn.xxt.file.ui.manager.FileRecentMainActivity;
import cn.xxt.file.ui.selector.FileSelectorMainActivity;
import cn.xxt.file.util.FileUtil;
import cn.xxt.file.util.UiUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zyj on 2017/8/16.
 */

public class AllFragment extends FileBaseFragment {

    @Inject
    @ActivityContext
    Context context;

    @BindView(R2.id.rlv_all)
    RecyclerView recyclerView;

    /** 备份数据 */
    private List<MultiItemEntity> folderItemList = new ArrayList<>();
    private List<MultiItemEntity> folderList = new ArrayList<>();
    private List<FileInfo> fileInfos = new ArrayList<>();

    ExpandableItemAdapter expandableItemAdapter;

    public static final int FIRST_STICKY_VIEW = 1;
    public static final int HAS_STICKY_VIEW = 2;
    public static final int NONE_STICKY_VIEW = 3;

    /**
     * 是否有删除文件标记：activity中点击删除，
     * 不会及时的刷新该fragment（未显示的），、
     * 当fragment显示的时候，重载一下数据。：：：仿qq
     * @param deleteThisFragmentFile
     */
    public void setDeleteThisFragmentFile(boolean deleteThisFragmentFile) {
        isDeleteThisFragmentFile = deleteThisFragmentFile;
    }

    public boolean isDeleteThisFragmentFile = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        injectThis();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //相当于onresume
            if (isDeleteThisFragmentFile && expandableItemAdapter != null) {
                getDataOfDb();

                isDeleteThisFragmentFile = false;
            }

            expanFirstFolder = true;

            if (expandableItemAdapter != null) {
                expandableItemAdapter.expand(0);
            }
        } else {
            //相当于onPause
        }
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_all_file;
    }

    @Override
    public void initView() {
        expandableItemAdapter = new ExpandableItemAdapter(context, folderList, getActivitySelectedFileInfoList(), getItemUiType());
        expandableItemAdapter.setOnExpandableItemAdapterListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(expandableItemAdapter);

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            int mScrollThreshold;
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
////                recyclerView.getChildAt()
//
//                View stickyView = recyclerView.findChildViewUnder(
//                        headView.getMeasuredWidth() / 2, 5);
//
//                View scrollItemView = recyclerView.findChildViewUnder(
//                        headView.getMeasuredWidth() / 2, headView.getMeasuredHeight() + 1);
//
//                if (scrollItemView != null && scrollItemView.getTag() != null) {
//
//                    int transViewStatus = (int) scrollItemView.getTag();
//                    int dealtY = scrollItemView.getTop() - headView.getMeasuredHeight();
//
//                    if (scrollItemView.getTop() > 0) {
//                        headView.setTranslationY(dealtY);
//                    } else {
//                        headView.setTranslationY(0);
//                    }
//
////                    if (transViewStatus == HAS_STICKY_VIEW) {
////                        if (scrollItemView.getTop() > 0) {
////                            headView.setTranslationY(dealtY);
////                        } else {
////                            headView.setTranslationY(0);
////                        }
////                    } else if (transViewStatus == NONE_STICKY_VIEW) {
////                        headView.setTranslationY(0);
////                    }
//                } else {
//                    headView.setTranslationY(0);
//                }
//
//                boolean isSignificantDelta = Math.abs(dy) > mScrollThreshold;
//
//                if (isSignificantDelta) {
//                    if (dy > 0) {
//                        //上
//                        if (stickyView != null && stickyView.getTag() != null) {
//                            TextView tv = (TextView) stickyView.findViewById(R.id.tv_title);
//                            ((TextView)(headView.findViewById(R.id.tv_title_sticky))).setText(tv.getText());
//                            headView.setTag(stickyView.getTag());
//
////                    headView = stickyView;
//                        }
//                    } else {
//                        if (stickyView != null && stickyView.getTag() == null) {
//                            //非头，替换为当前headview的前一个head。
//                            ((TextView)(headView.findViewById(R.id.tv_title_sticky))).setText("fad");
//                        }
//                    }
//                }
//
//            }
//            public void setScrollThreshold(int scrollThreshold) {
//                mScrollThreshold = scrollThreshold;
//            }
//        });

//        headView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (headView.getTag() != null) {
//                    int headPosi = Integer.valueOf(headView.getTag().toString());
//                    MultiItemEntity itemEntity = expandableItemAdapter.getItem(headPosi);
//                    if (itemEntity instanceof FolderItem) {
//                        if (((FolderItem) itemEntity).isExpanded()) {
//                            expandableItemAdapter.collapse(headPosi);
//                        } else {
//                            expandableItemAdapter.expand(headPosi);
//                        }
//                    }
//                }
//            }
//        });

        //
//        RxView.clicks(headView)
//                .throttleFirst(500, TimeUnit.MILLISECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .compose(this.<Void>bindToLifecycle())
//                .subscribe(new Subscriber<Void>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onNext(Void aVoid) {
//                         if (headView.getTag() != null) {
//                            int headPosi = Integer.valueOf(headView.getTag().toString());
//                            MultiItemEntity itemEntity = expandableItemAdapter.getItem(headPosi);
//                            if (((FolderItem) itemEntity).isExpanded()) {
//                                expandableItemAdapter.collapse(headPosi);
//                            } else {
//                                expandableItemAdapter.expand(headPosi);
//                            }
//                        }
//                    }
//                });

        //异步获取文件
        //加载中页面
        expandableItemAdapter.setEmptyView(UiUtil.getLoadingView(context, (ViewGroup) recyclerView.getParent()));
        getDataOfDb();
    }

    @Override
    public void onSuccessGetFileInfo(final List<FileInfo> fileInfoList) {
        Observable.just(fileInfoList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<FileInfo>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<FileInfo> fileInfoList) {
                        //FIXME for test
                        fileInfoList = new ArrayList<>();
                        for (int i = 0; i < 30; i++) {
                            FileInfo file = new FileInfo();
                            file.setFileServerPath("http://dldir1.qq.com/weixin/android/weixin6516android1120.apk");
                            file.setFileType(FileTypeEnum.TYPE_OTHER_APK.getFileType());
                            file.setFileName("wechat.apk");
                            file.setDownloadStatus(FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_NOT_YET.getFileDownloadStatus());
                            long fileId = System.currentTimeMillis() + i;
                            file.setFileId(fileId);
                            //                        file.setFileSize();
                            file.setCreateDate(System.currentTimeMillis());
                            file.setUpdateDate(System.currentTimeMillis());

                            fileInfoList.add(file);
                        }

                        if (fileInfoList != null && fileInfoList.size() > 0) {
                            fileInfos.clear();
                            fileInfos.addAll(fileInfoList);

                            //备份数据
                            folderItemList.clear();
                            folderItemList.addAll(folderList);

                            Log.i("start:", "" + System.currentTimeMillis());
                            //处理
                            List<FolderInfo> folderInfoList = FileUtil.fileinfoTypeWithDate(fileInfoList);

                            folderList.clear();
                            for (FolderInfo folderInfo : folderInfoList) {
                                FolderItem folderItem = new FolderItem(folderInfo.getName());
                                List<FileInfo> tmpFileInfoList = folderInfo.getFileInfos();

                                for (FileInfo fileInfo : tmpFileInfoList) {
                                    if (fileInfo.getFileType() == FileTypeEnum.TYPE_IMAGE.getFileType()) {
                                        fileInfo.setGridPhoto(false);
                                    }
                                    if (FileUtil.checkSuffix(fileInfo.getFileLocalPath(), new String[]{"doc", "docx", "dot"})) {
                                        fileInfo.fileType = FileTypeEnum.TYPE_DOC_WORD.getFileType();
                                    } else if (FileUtil.checkSuffix(fileInfo.getFileLocalPath(), new String[]{"xls", "xlsx"})) {
                                        fileInfo.fileType = FileTypeEnum.TYPE_DOC_EXCEL.getFileType();
                                    } else if (FileUtil.checkSuffix(fileInfo.getFileLocalPath(), new String[]{"pdf"})) {
                                        fileInfo.fileType = FileTypeEnum.TYPE_DOC_PDF.getFileType();
                                    } else if (FileUtil.checkSuffix(fileInfo.getFileLocalPath(), new String[]{"ppt", "pptx"})) {
                                        fileInfo.fileType = FileTypeEnum.TYPE_DOC_PPT.getFileType();
                                    } else if (FileUtil.checkSuffix(fileInfo.getFileLocalPath(), new String[]{"txt"})) {
                                        fileInfo.fileType = FileTypeEnum.TYPE_OTHER_TXT.getFileType();
                                    } else if (FileUtil.checkSuffix(fileInfo.getFileLocalPath(), new String[]{"zip"})) {
                                        fileInfo.fileType = FileTypeEnum.TYPE_OTHER_ZIP.getFileType();
                                    } else if (FileUtil.checkSuffix(fileInfo.getFileLocalPath(), new String[]{"rar"})) {
                                        fileInfo.fileType = FileTypeEnum.TYPE_OTHER_RAR.getFileType();
                                    } else if (FileUtil.checkSuffix(fileInfo.getFileLocalPath(), new String[]{"apk"})) {
                                        fileInfo.fileType = FileTypeEnum.TYPE_OTHER_APK.getFileType();
                                    }
                                    folderItem.addSubItem(fileInfo);
                                }
                                folderList.add(folderItem);
                            }

                            expandableItemAdapter.setNewData(folderList);
                            expandableItemAdapter.setItemUiType(getItemUiType());
                            expandableItemAdapter.notifyDataSetChanged();
                            if (expanFirstFolder) {
                                expandableItemAdapter.expand(0, true);
                                expanFirstFolder = false;
                            }

                            //回复组的展开状态
                            List<MultiItemEntity> tmpList = new ArrayList<>();
                            tmpList.addAll(folderItemList);

                            for (int i = 0; i < folderList.size(); i++) {
                                MultiItemEntity multiItemEntity = folderList.get(i);
                                if (multiItemEntity instanceof FolderItem) {

                                    for (int j = 0; j < tmpList.size(); j++) {
                                        MultiItemEntity itemEntity = (MultiItemEntity)tmpList.get(j);

                                        if (itemEntity instanceof FolderItem) {
                                            if (((FolderItem) multiItemEntity).title.equals(((FolderItem) itemEntity).title)) {
                                                if (((FolderItem) itemEntity).isExpanded()) {
                                                    int index = expandableItemAdapter.getData().indexOf(multiItemEntity);
                                                    expandableItemAdapter.expand(index);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            //设置kogn'bai
                            if (folderList == null || folderList.size() == 0) {
                                expandableItemAdapter.setEmptyView(UiUtil.getEmptyView(context, (ViewGroup) recyclerView.getParent()));
                            }
                        } else {
                            //加载空白页
                            expandableItemAdapter.setEmptyView(UiUtil.getEmptyView(context, (ViewGroup) recyclerView.getParent()));
                        }
                    }
                });
    }

    @Override
    public void onPhotoItemClickedToPreview(FileInfo fileInfo) {
        super.onPhotoItemClickedToPreview(fileInfo);
        if (fileInfo.getFileType() == FileTypeEnum.TYPE_IMAGE.getFileType()) {
            if (fileInfo != null && fileInfos != null && fileInfos.size() > 0) {
                ArrayList<String> urlList = new ArrayList<>();
                Bundle bundle =  new Bundle();

                for (FileInfo sourceFileInfo : fileInfos) {
                    if (sourceFileInfo != null
                            && sourceFileInfo.getFileType() == FileTypeEnum.TYPE_IMAGE.getFileType()) {
                        if (sourceFileInfo.getFileLocalPath() != null
                                && sourceFileInfo.getFileLocalPath().length() > 0) {
                            urlList.add(sourceFileInfo.getFileLocalPath());
                        } else if (sourceFileInfo.getFileServerPath() != null
                                && sourceFileInfo.getFileServerPath().length() > 0) {
                            urlList.add(sourceFileInfo.getFileServerPath());
                        }
                    }
                }

                //预览的图片在该组所有图片中的索引位置
                if (urlList.size() == 0) {
                    return;
                }

                int filePosition = -1;
                if (fileInfo.getFileLocalPath() != null && fileInfo.getFileLocalPath().length() > 0) {
                    filePosition = urlList.indexOf(fileInfo.getFileLocalPath());
                } else if (fileInfo.getFileServerPath() != null && fileInfo.getFileServerPath().length() > 0) {
                    filePosition = urlList.indexOf(fileInfo.getFileServerPath());
                }

                bundle.putStringArrayList(ImageShowActivity.BUNDLE_IMAGE_LIST, urlList);
                bundle.putInt(ImageShowActivity.BUNDLE_SELECTED_INDEX, filePosition);
                ActivityUtil.changeActivity(getActivity(), ImageShowActivity.class, bundle, false);
            }
        }
    }

    @Override
    public void onFileItemClickedToOpen(FileInfo fileInfo) {
        super.onFileItemClickedToOpen(fileInfo);
        //界面跳转

        Bundle bundle = new Bundle();
        bundle.putSerializable(FileOpenActivity.BUNDLE_FILEINFO, fileInfo);
        ActivityUtil.changeActivity(getActivity(), FileOpenActivity.class, bundle, false);
    }

    @Override
    public void onFileLongPressToDelete(FileInfo fileInfo) {
        super.onFileLongPressToDelete(fileInfo);

//        FileDb fileDb = FileDb.getInstance(context);
//        fileDb.deleteFileByFileId(getWebId(),fileInfo.getFileId());

        List<FileInfo> fileInfoList = new ArrayList<>();
        fileInfoList.add(fileInfo);
        notifyToDeleteFile(fileInfoList);
    }

    /**
     * 状态改变之类的：刷新ui
     */
    public void notifyToRefreshUi() {
        if (expandableItemAdapter != null) {
            expandableItemAdapter.setItemUiType(getItemUiType());
            expandableItemAdapter.notifyDataSetChanged();
        }
    }

    public void notifyToDeleteFile(List<FileInfo> fileInfoList) {
        if (expandableItemAdapter != null) {
            if (fileInfoList != null && fileInfoList.size() > 0) {
                if (fileInfos != null && fileInfos.size() > 0) {
                    fileInfos.removeAll(fileInfoList);
                }
            }

            //最近
            List<Long> idList = new ArrayList<>();
            for (int i = 0; i < fileInfoList.size(); i++) {
                FileInfo fileInfo = fileInfoList.get(i);
                idList.add(fileInfo.getFileId());

                FileUtil.deleteFile(fileInfo.getFileLocalPath());
            }
            FileDb fileDb = FileDb.getInstance(context);
            fileDb.deleteFileByFileIdList(getWebId(), idList);

            getDataOfDb();
        }
    }

    /**
     * 批量下载处理：通过modle反应到view上
     * @param fileInfoList
     */
    public void notifyToBatchDownloadFile(List<FileInfo> fileInfoList) {
        if (fileInfoList == null || fileInfoList.size() == 0) {
            return;
        }

        for (int i = 0; i < fileInfos.size(); i++) {
            FileInfo fileInfo = fileInfos.get(i);
            if (fileInfo != null
                    && fileInfo.getDownloadStatus() == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_NOT_YET.getFileDownloadStatus()) {
                if (fileInfoList.contains(fileInfo)) {
                    fileInfo.setDownloadStatus(FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_READY.getFileDownloadStatus());
                }
            }
        }

        notifyToRefreshUi();
    }

    private void injectThis(){
        Activity activity = getActivity();
        if (activity instanceof FileSelectorMainActivity) {
            ((FileSelectorMainActivity) activity).getActivityComponent().inject(this);
        } else if (activity instanceof FileLocalMainActivity) {
            ((FileLocalMainActivity) activity).getActivityComponent().inject(this);
        } else if (activity instanceof FileRecentMainActivity) {
            ((FileRecentMainActivity) activity).getActivityComponent().inject(this);
        }
    }

    private int getItemUiType() {
        int itemUiType;

        if (getActivity() instanceof FileRecentMainActivity) {
            itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_CONTENT_STATUS.getItemUiType();
            if (((FileRecentMainActivity) getActivity()).isEditStatus()) {
                itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_CHECKBOX_CONTENT.getItemUiType();
            }
        } else {
            itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_CHECKBOX_CONTENT.getItemUiType();
        }

        return itemUiType;
    }

    private void getDataOfDb() {
        //异步获取
        fragmentPresenter.getFileInfoListFromLocalDb(context, getWebId(), FileTypeEnum.TYPE_ALL.getFileType());
    }
}
