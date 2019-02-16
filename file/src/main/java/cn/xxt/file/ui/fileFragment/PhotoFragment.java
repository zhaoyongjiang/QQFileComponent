package cn.xxt.file.ui.fileFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.ui.image.ImageShowActivity;
import cn.xxt.commons.util.ActivityUtil;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.internal.data.local.FileDb;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.file.internal.domain.FolderInfo;
import cn.xxt.file.internal.domain.FolderItem;
import cn.xxt.file.internal.domain.ItemUiTypeEnum;
import cn.xxt.file.ui.base.FileBaseFragment;
import cn.xxt.file.ui.manager.FileLocalMainActivity;
import cn.xxt.file.ui.manager.FileRecentMainActivity;
import cn.xxt.file.ui.selector.FileSelectorMainActivity;
import cn.xxt.file.util.FileUtil;
import cn.xxt.file.util.MediaStoreUtil;
import cn.xxt.file.util.PhotoScannerUtil;
import cn.xxt.file.util.UiUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zyj on 2017/8/16.
 */

public class PhotoFragment extends FileBaseFragment {
    @Inject
    @ActivityContext
    Context context;

    @BindView(R2.id.rlv_photo)
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
        return R.layout.fragment_photo_file;
    }

    @Override
    public void initView() {
        expandableItemAdapter = new ExpandableItemAdapter(context, fileTypeList, getActivitySelectedFileInfoList(), getItemUiType());
        expandableItemAdapter.setOnExpandableItemAdapterListener(this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Log.e("GridLayoutManager", "getItemViewType:" + expandableItemAdapter.getItemViewType(position));
                return expandableItemAdapter.getItemViewType(position) == ExpandableItemAdapter.PHOTO ? 1 : gridLayoutManager.getSpanCount();
        }
        });
        recyclerView.setAdapter(expandableItemAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);

        //异步获取文件
        //加载中页面
        expandableItemAdapter.setEmptyView(UiUtil.getLoadingView(context, (ViewGroup) recyclerView.getParent()));
        if (dataFlag == FLAG_DATA_LOCAL) {
            getDataOfPhone();
        } else if (dataFlag == FLAG_DATA_RECENT){
            getDataOfDb();
        }
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
                            List<FolderInfo> folderInfoList = FileUtil.fileinfoTypeWithDate(fileInfoList);
                            handleFiles(folderInfoList);
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

    @Override
    public void onFileLongPressToDelete(FileInfo fileInfo) {
//        Activity activity = getActivity();
//        if (activity instanceof FileLocalMainActivity) {
//            //本机
//            FileUtil.deleteFile(fileInfo.getFileLocalPath());
//            MediaStoreUtil.updateMediaStore(context, fileInfo.getFileLocalPath());
//        } else {
//            //最近
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
        itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_PHOTO_NORMAL.getItemUiType();
        if (getActivity() instanceof FileRecentMainActivity) {
            if (((FileRecentMainActivity) getActivity()).isEditStatus()) {
                itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_PHOTO_EDIT.getItemUiType();
            }
        } else if (getActivity() instanceof FileLocalMainActivity) {
            if (((FileLocalMainActivity) getActivity()).isEditStatus()) {
                itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_PHOTO_EDIT.getItemUiType();
            }
        } else {
            itemUiType = ItemUiTypeEnum.ITEM_UI_TYPE_PHOTO_EDIT.getItemUiType();
        }

        return itemUiType;
    }

    private void getDataOfPhone() {
        new PhotoScannerUtil(this, FileTypeEnum.TYPE_IMAGE.getFileType()).getPhotoFromLocal(new PhotoScannerUtil.PhotoScannerCallBack() {
            @Override
            public boolean photoScannerComplete(List<FolderInfo> folders) {
                //扫描完成
                if (folders != null && folders.size() > 0) {
                    handleFiles(folders);
                } else {
                    //加载空白页
                    expandableItemAdapter.setEmptyView(UiUtil.getEmptyView(context, (ViewGroup) recyclerView.getParent()));
                }
                return true;
            }
        });
    }

    private void getDataOfDb() {
        //异步获取
        fragmentPresenter.getFileInfoListFromLocalDb(context, getWebId(), FileTypeEnum.TYPE_IMAGE.getFileType());
    }

    private void handleFiles(List<FolderInfo> folderInfos) {
        try {
            //处理数据
            fileInfos.clear();

            //备份数据
            folderItemList.clear();
            folderItemList.addAll(fileTypeList);

            fileTypeList.clear();
            for (FolderInfo folderInfo : folderInfos) {
                FolderItem folderItem = new FolderItem(folderInfo.getName());
                List<FileInfo> images = folderInfo.getFileInfos();
                for (FileInfo fileInfo : images) {
                    folderItem.addSubItem(fileInfo);
                    fileInfos.add(fileInfo);
                }

//                //FIXME for test
//                FileDb.getInstance(context).insertFileInfoList(0, images);

                fileTypeList.add(folderItem);
            }

            expandableItemAdapter.setNewData(fileTypeList);
            expandableItemAdapter.setItemUiType(getItemUiType());
            expandableItemAdapter.notifyDataSetChanged();
            if (expanFirstFolder) {
                expandableItemAdapter.expand(0, true);
                expanFirstFolder = false;
            }

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

            if (fileTypeList == null || fileTypeList.size() == 0) {
                expandableItemAdapter.setEmptyView(UiUtil.getEmptyView(context, (ViewGroup) recyclerView.getParent()));
            }
        } catch (Exception e) {
            e.printStackTrace();

            expandableItemAdapter.setEmptyView(UiUtil.getEmptyView(context, (ViewGroup) recyclerView.getParent()));
        }
    }
}
