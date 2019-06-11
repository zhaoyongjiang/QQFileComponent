package cn.xxt.file.ui.fileFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import cn.xxt.commons.util.ActivityUtil;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.internal.data.local.FileDb;
import cn.xxt.file.internal.domain.FileDownloadStatusEnum;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.file.internal.domain.FolderItem;
import cn.xxt.file.internal.domain.ItemUiTypeEnum;
import cn.xxt.file.ui.base.FileBaseFragment;
import cn.xxt.file.ui.manager.FileLocalMainActivity;
import cn.xxt.file.ui.manager.FileOpenActivity;
import cn.xxt.file.ui.manager.FileRecentMainActivity;
import cn.xxt.file.ui.selector.FileSelectorMainActivity;
import cn.xxt.file.util.AudioScannerUtil;
import cn.xxt.file.util.FileUtil;
import cn.xxt.file.util.MediaStoreUtil;
import cn.xxt.file.util.UiUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zyj on 2017/8/16.
 */

public class AudioFragment extends FileBaseFragment {
    @Inject
    @ActivityContext
    Context context;

    @BindView(R2.id.rlv_audio)
    RecyclerView recyclerView;

    /** 备份数据 */
    private List<MultiItemEntity> folderItemList = new ArrayList<>();
    private List<MultiItemEntity> fileTypeList = new ArrayList<>();
    private List<FileInfo> fileInfos = new ArrayList<>();

    ExpandableItemAdapter expandableItemAdapter;

    /**
     * 是否有删除文件标记：activity中点击删除，
     * 不会及时的刷新该fragment（未显示的），、
     * 当fragment显示的时候，重载一下数据。：：：仿qq
     * @param deleteThisFragmentFile
     */
    public void setDeleteThisFragmentFile(boolean deleteThisFragmentFile) {
        isDeleteThisFragmentFile = deleteThisFragmentFile;
    }

    public boolean isDeleteThisFragmentFile;

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
                loadData();

                this.isDeleteThisFragmentFile = false;
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
        return R.layout.fragment_audio_file;
    }

    @Override
    public void initView() {
        expandableItemAdapter = new ExpandableItemAdapter(context, fileTypeList, getActivitySelectedFileInfoList(), getItemUiType());
        expandableItemAdapter.setOnExpandableItemAdapterListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(expandableItemAdapter);

        //异步获取文件
        //加载中页面
        expandableItemAdapter.setEmptyView(UiUtil.getLoadingView(context, (ViewGroup) recyclerView.getParent()));
        loadData();
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
                        if (fileInfoList != null && fileInfoList.size() > 0) {
                            //处理

                            handleFiles(fileInfoList);
                        } else {
                            //加载空白页
                            expandableItemAdapter.setEmptyView(UiUtil.getEmptyView(context, (ViewGroup) recyclerView.getParent()));
                        }
                    }
                });
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
//        Activity activity = getActivity();
//        if (activity instanceof FileLocalMainActivity) {
//            FileUtil.deleteFile(fileInfo.getFileLocalPath());
//        } else {
//            FileDb fileDb = FileDb.getInstance(context);
//            fileDb.deleteFileByFileId(getWebId(),fileInfo.getFileId());
//        }

        List<FileInfo> fileInfoList = new ArrayList<>();
        fileInfoList.add(fileInfo);
        notifyToDeleteFile(fileInfoList);
    }

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

            Activity activity = getActivity();
            if (activity instanceof FileLocalMainActivity) {
                //本机
                List<String> pathList = new ArrayList<>();
                for (int i = 0; i < fileInfoList.size(); i++) {
                    FileInfo fileInfo = fileInfoList.get(i);
                    pathList.add(fileInfo.getFileLocalPath());

                    FileUtil.deleteFile(fileInfo.getFileLocalPath());
                }

                MediaStoreUtil.setMediaStoreUtilInterface(new MediaStoreUtil.MediaStoreUtilInterface() {
                    @Override
                    public void syncComplete() {

                        //多媒体数据那是异步线程同步，到这里再次去获取数据，要先回调到主线程。要不然，PhotoScannerUtil中Loader不会去加载数据
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
                                        loadData();
                                    }
                                });
                    }
                });
                MediaStoreUtil.updateMediaStore(context, pathList);
            } else {
                //最近
                List<Long> idList = new ArrayList<>();
                for (int i = 0; i < fileInfoList.size(); i++) {
                    FileInfo fileInfo = fileInfoList.get(i);
                    idList.add(fileInfo.getFileId());

                    FileUtil.deleteFile(fileInfo.getFileLocalPath());
                }
                FileDb fileDb = FileDb.getInstance(context);
                fileDb.deleteFileByFileIdList(getWebId(), idList);

                loadData();
            }
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
            if (fileInfo != null) {
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

    private void loadData() {
        if (dataFlag == FLAG_DATA_LOCAL) {
            getDataOfPhone();
        } else if (dataFlag == FLAG_DATA_RECENT){
            getDataOfDb();
        }
    }

    private int getItemUiType() {
        int itemUiType;

        if (getActivity() instanceof FileRecentMainActivity) {
            itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_CONTENT_STATUS.getItemUiType();
            if (((FileRecentMainActivity) getActivity()).isEditStatus()) {
                itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_CHECKBOX_CONTENT.getItemUiType();
            }
        } else if (getActivity() instanceof FileLocalMainActivity) {
            itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_CONTENT.getItemUiType();
            if (((FileLocalMainActivity) getActivity()).isEditStatus()) {
                itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_CHECKBOX_CONTENT.getItemUiType();
            }
        } else {
            itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_CHECKBOX_CONTENT.getItemUiType();
        }

        return itemUiType;
    }

    private void getDataOfPhone() {
        new AudioScannerUtil(this, FileTypeEnum.TYPE_AUDIO.getFileType()).getAudioFromLocal(new AudioScannerUtil.AudioScannerCallBack() {
            @Override
            public boolean audioScannerComplete(List<FileInfo> fileInfoList) {
                //扫描完成
                if (fileInfoList != null && fileInfoList.size() > 0) {
//                    //FIXME for test
//                    FileDb.getInstance(context).insertFileInfoList(0, fileInfos);

                    handleFiles(fileInfoList);
                } else {
                    //加载空白页
                    expandableItemAdapter.setEmptyView(UiUtil.getEmptyView(context, (ViewGroup) recyclerView.getParent()));
                }
                return false;
            }
        });
    }

    private void getDataOfDb() {
        //异步获取
        fragmentPresenter.getFileInfoListFromLocalDb(context, getWebId(), FileTypeEnum.TYPE_AUDIO.getFileType());
    }

    private void handleFiles(List<FileInfo> fileInfoList) {
        try {
            FolderItem audioItem = new FolderItem("音乐");

            fileInfos.clear();
            fileInfos.addAll(fileInfoList);

            //备份数据
            folderItemList.clear();
            folderItemList.addAll(fileTypeList);

            fileTypeList.clear();
            for (FileInfo fileInfo : fileInfoList) {
                fileInfo.fileType = FileTypeEnum.TYPE_AUDIO.getFileType();
                audioItem.addSubItem(fileInfo);
            }

            if (audioItem.getSubItems() != null && audioItem.getSubItems().size() > 0) {
                fileTypeList.add(audioItem);
            }

            expandableItemAdapter.setNewData(fileTypeList);
            expandableItemAdapter.setItemUiType(getItemUiType());
            expandableItemAdapter.notifyDataSetChanged();
            if (expanFirstFolder) {
                expandableItemAdapter.expand(0, true);
                expanFirstFolder = false;
            }

            //回复组的展开状态
            List<MultiItemEntity> tmpList = new ArrayList<>();
            tmpList.addAll(folderItemList);

            for (int i = 0; i < fileTypeList.size(); i++) {
                MultiItemEntity multiItemEntity = fileTypeList.get(i);
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

            //设置空白页
            if (fileTypeList == null || fileTypeList.size() == 0) {
                expandableItemAdapter.setEmptyView(UiUtil.getEmptyView(context, (ViewGroup) recyclerView.getParent()));
            }
        } catch (Exception e) {
            e.printStackTrace();

            expandableItemAdapter.setEmptyView(UiUtil.getEmptyView(context, (ViewGroup) recyclerView.getParent()));
        }
    }
}
