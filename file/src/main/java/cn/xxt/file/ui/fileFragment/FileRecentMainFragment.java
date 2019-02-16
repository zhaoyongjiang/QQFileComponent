package cn.xxt.file.ui.fileFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flyco.tablayout.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.api.FileComponent;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.file.ui.base.FileBaseFragment;
import cn.xxt.file.ui.base.FileTabPagerAdapter;
import cn.xxt.file.ui.manager.FileLocalMainActivity;
import cn.xxt.file.ui.manager.FileRecentMainActivity;
import cn.xxt.file.ui.selector.FileSelectorMainActivity;

import static cn.xxt.file.internal.domain.FileTypeEnum.TYPE_AUDIO;
import static cn.xxt.file.internal.domain.FileTypeEnum.TYPE_DOC;
import static cn.xxt.file.internal.domain.FileTypeEnum.TYPE_IMAGE;
import static cn.xxt.file.internal.domain.FileTypeEnum.TYPE_OTHER;

/**
 * Created by zyj on 2017/8/16.
 */

public class FileRecentMainFragment extends FileBaseFragment {
    @Inject
    @ActivityContext
    Context context;

    @BindView(R2.id.stl_slid)
    SlidingTabLayout slidingTabLayout;

    @BindView(R2.id.vp_viewpager)
    ViewPager viewPager;

    private List<String> fileTypeFragmentTitles = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();

    private DocFragment docFragment;
    private AudioFragment audioFragment;
    private OtherFragment otherFragment;
    private PhotoFragment photoFragment;
    private AllFragment allFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        injectThis();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_file_recent_main_file;
    }

    @Override
    public void initView() {

        List<Integer> fileTypeList = FileComponent.builder(context).getShowFileType();

        if (fileTypeList.size() > 0) {
            fileTypeFragmentTitles.add("全部");

            allFragment = new AllFragment();

            fragments.add(allFragment);
        }
        if (fileTypeList.contains(TYPE_DOC.getFileType())) {
            fileTypeFragmentTitles.add("文档");

            docFragment = new DocFragment();
            docFragment.setDataFlag(FileBaseFragment.FLAG_DATA_RECENT);

            fragments.add(docFragment);
        }
        if (fileTypeList.contains(TYPE_AUDIO.getFileType())) {
            fileTypeFragmentTitles.add("音乐");

            audioFragment = new AudioFragment();
            audioFragment.setDataFlag(FileBaseFragment.FLAG_DATA_RECENT);

            fragments.add(audioFragment);
        }
        if (fileTypeList.contains(TYPE_IMAGE.getFileType())) {
            fileTypeFragmentTitles.add("图片");

            photoFragment = new PhotoFragment();
            photoFragment.setDataFlag(FileBaseFragment.FLAG_DATA_RECENT);

            fragments.add(photoFragment);
        }
        if (fileTypeList.contains(TYPE_OTHER.getFileType())) {
            fileTypeFragmentTitles.add("其他");

            otherFragment = new OtherFragment();
            otherFragment.setDataFlag(FileBaseFragment.FLAG_DATA_RECENT);

            fragments.add(otherFragment);
        }

        FileTabPagerAdapter fileTabPagerAdapter = new FileTabPagerAdapter(getChildFragmentManager(), fileTypeFragmentTitles, fragments
        );

        if (viewPager != null) {
            viewPager.setAdapter(fileTabPagerAdapter);
        }

        slidingTabLayout.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //切换fragment，不管是使用滑动切换，还是tab切换。该处都会运行一次。。
                // 所以，通知各fragment变化，就在这。
                //如果是刚运行起来，切换fragment，这时候fragment还没有创建，
                // 所以，notify会导致空指针，所以，实现管理-编辑功能，
                // 两步走：第一，点击了编辑，setter赋值给相应的adapter。
                // 第二：切换时候，在该处nofity
                notifyFragmentRefresh(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void refreshFragmentWithOperType(List<FileInfo> fileInfoList, int operType) {
        switch (operType){
            case FLAG_OPER_TYPE_DELETE:
                notifyAndrefreshFragmentForBatchDelete(fileInfoList);
                break;
            case FLAG_OPER_TYPE_DOWNLOAD:
                notifyFragmentBatchDownload(fileInfoList);
                break;
            case FLAG_OPER_TYPE_SHARE:
            default:
                refreshCurrentFragment();
                break;
        }
    }

    /**
     * 刷新当前文件fragment：ui状态的变化触发该函数
     */
    private void refreshCurrentFragment() {
        if (fragments == null || fragments.size() == 0) {
            return;
        }
        if (fragments.get(viewPager.getCurrentItem()) == null) {
            return;
        }
        notifyFragmentRefresh(viewPager.getCurrentItem());
    }



    /**
     * 刷新文件fragment：activity控制删除文件。
     * 调用该方法，分发到各个文件fragment，标记一下是否有删除，
     * 然后，各个fragment 显示的时候，重新加载一下数据。reloadData
     * @param fileInfoList
     */
    private void notifyAndrefreshFragmentForBatchDelete(List<FileInfo> fileInfoList) {
        if (fileInfoList == null || fileInfoList.size() == 0) {
            return;
        }
        //1：刷新当前fragment的ui
        notifyFragmentDeleteFiles(viewPager.getCurrentItem(), fileInfoList);

        //2：分发各个fragment文件删除标识
        distributionFragmentFileDeleteFlagWithDeletedFiles(fileInfoList);
    }

    private void injectThis(){
        Activity activity = getActivity();
        if (activity instanceof FileSelectorMainActivity) {
            ((FileSelectorMainActivity) activity).getActivityComponent().inject(this);
        } else if (activity instanceof FileRecentMainActivity) {
            ((FileRecentMainActivity) activity).getActivityComponent().inject(this);
        } else if (activity instanceof FileLocalMainActivity) {
            ((FileLocalMainActivity) activity).getActivityComponent().inject(this);
        }
    }

    /**
     * 刷新当前fragment的ui:
     *
     * nofifyUi是各个文件fragment类中public出来的方法。
     * 其中的处理用到了文件对比，已经重写了fileinfo实体类的equal方法
     *
     * @param fragmentPosi
     */
    private void notifyFragmentRefresh(int fragmentPosi){
        Fragment fragment = fragments.get(fragmentPosi);

        if (fragment instanceof AllFragment) {
            ((AllFragment) fragment).notifyToRefreshUi();
        } else if (fragment instanceof DocFragment) {
            ((DocFragment) fragment).notifyToRefreshUi();
        } else if (fragment instanceof AudioFragment) {
            ((AudioFragment) fragment).notifyToRefreshUi();
        } else if (fragment instanceof PhotoFragment) {
            ((PhotoFragment) fragment).notifyToRefreshUi();
        } else if (fragment instanceof OtherFragment) {
            ((OtherFragment) fragment).notifyToRefreshUi();
        }
    }

    private void notifyFragmentDeleteFiles(int fragmentPosi, List<FileInfo> fileInfoList) {
        Fragment fragment = fragments.get(fragmentPosi);

        if (fragment instanceof AllFragment) {
            ((AllFragment) fragment).notifyToDeleteFile(fileInfoList);
        } else if (fragment instanceof DocFragment) {
            ((DocFragment) fragment).notifyToDeleteFile(fileInfoList);
        } else if (fragment instanceof AudioFragment) {
            ((AudioFragment) fragment).notifyToDeleteFile(fileInfoList);
        } else if (fragment instanceof PhotoFragment) {
            ((PhotoFragment) fragment).notifyToDeleteFile(fileInfoList);
        } else if (fragment instanceof OtherFragment) {
            ((OtherFragment) fragment).notifyToDeleteFile(fileInfoList);
        }
    }

    private void notifyFragmentBatchDownload(List<FileInfo> fileInfoList){
        if (fileInfoList != null && fileInfoList.size() > 0) {
            for (int i = 0; i < fragments.size(); i++) {
                Fragment fragment = fragments.get(i);

                if (fragment instanceof AllFragment) {
                    ((AllFragment) fragment).notifyToBatchDownloadFile(fileInfoList);
                } else if (fragment instanceof DocFragment) {
                    ((DocFragment) fragment).notifyToBatchDownloadFile(fileInfoList);
                } else if (fragment instanceof AudioFragment) {
                    ((AudioFragment) fragment).notifyToBatchDownloadFile(fileInfoList);
                } else if (fragment instanceof OtherFragment) {
                    ((OtherFragment) fragment).notifyToBatchDownloadFile(fileInfoList);
                }
            }
        }
    }

    /**
     * 将管理器删除文件点击事件 转为 删除标识，分发到各个文件framgent中。
     * 待fragment显示的时候，重载数据
     * @param fileInfoList
     */
    private void distributionFragmentFileDeleteFlagWithDeletedFiles(List<FileInfo> fileInfoList) {
        //过滤各个fragment文件是否有删除
        if (fileInfoList == null || fileInfoList.size() == 0) {
            return;
        }

        boolean allTypeDistributed = false;
        boolean docTypeDistributed = false;
        boolean audioTypeDistributed = false;
        boolean photoTypeDistributed = false;
        boolean otherTypeDistributed = false;

        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo != null) {
                if (fileInfo.getFileType() == FileTypeEnum.TYPE_AUDIO.getFileType()
                        && !audioTypeDistributed) {
                    if (audioFragment != null) {
                        audioFragment.isDeleteThisFragmentFile = true;
                    }

                    audioTypeDistributed = true;
                } else if (fileInfo.getFileType() == FileTypeEnum.TYPE_IMAGE.getFileType()
                        && !photoTypeDistributed) {
                    if (photoFragment != null) {
                        photoFragment.isDeleteThisFragmentFile = true;
                    }

                    photoTypeDistributed = true;
                } else if ((fileInfo.getFileType() == FileTypeEnum.TYPE_DOC_EXCEL.getFileType()
                        || fileInfo.getFileType() == FileTypeEnum.TYPE_DOC_PDF.getFileType()
                        || fileInfo.getFileType() == FileTypeEnum.TYPE_DOC_PPT.getFileType()
                        || fileInfo.getFileType() == FileTypeEnum.TYPE_DOC_WORD.getFileType())
                        && !docTypeDistributed) {
                    if (docFragment != null) {
                        docFragment.isDeleteThisFragmentFile = true;
                    }

                    docTypeDistributed = true;
                } else if ((fileInfo.getFileType() == FileTypeEnum.TYPE_OTHER_APK.getFileType()
                        || fileInfo.getFileType() == FileTypeEnum.TYPE_OTHER_RAR.getFileType()
                        || fileInfo.getFileType() == FileTypeEnum.TYPE_OTHER_ZIP.getFileType()
                        || fileInfo.getFileType() == FileTypeEnum.TYPE_OTHER_TXT.getFileType())
                        && !otherTypeDistributed) {
                    if (otherFragment != null) {
                        otherFragment.isDeleteThisFragmentFile = true;
                    }

                    otherTypeDistributed = true;
                }
                //FIXME 此备注不要删除：后期拓展视频，该处需要添加一个else if 分支
            }
        }

        //分发标识
        if (allFragment != null) {
            allFragment.isDeleteThisFragmentFile = true;
        }
    }
}
