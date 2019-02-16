package cn.xxt.file.injection.component;

import cn.xxt.commons.injection.PerActivity;
import cn.xxt.file.injection.module.FileActivityModule;
import cn.xxt.file.ui.fileFragment.AllFragment;
import cn.xxt.file.ui.fileFragment.AudioFragment;
import cn.xxt.file.ui.fileFragment.DocFragment;
import cn.xxt.file.ui.fileFragment.FileLocalMainFragment;
import cn.xxt.file.ui.fileFragment.FileRecentMainFragment;
import cn.xxt.file.ui.fileFragment.OtherFragment;
import cn.xxt.file.ui.fileFragment.PhotoFragment;
import cn.xxt.file.ui.fileFragment.VedioFragment;
import cn.xxt.file.ui.manager.FileOpenApkInstallFragment;
import cn.xxt.file.ui.manager.FileOpenAudioPlayFragment;
import cn.xxt.file.ui.manager.FileOpenActivity;
import cn.xxt.file.ui.manager.FileOpenDownloadFragment;
import cn.xxt.file.ui.manager.FileLocalMainActivity;
import cn.xxt.file.ui.manager.FileManagerMainActivity;
import cn.xxt.file.ui.manager.FileOpenFragment;
import cn.xxt.file.ui.manager.FileRecentMainActivity;
import cn.xxt.file.ui.manager.FileOpenUnVailableFragment;
import cn.xxt.file.ui.selector.FileSelectorMainActivity;
import dagger.Component;

/**
 * Created by zyj on 2017/8/16.
 */

@PerActivity
@Component(dependencies = FileApplicationComponent.class, modules = FileActivityModule.class)
public interface FileActivityComponent {
    void inject (FileSelectorMainActivity fileSelectorMainActivity);
    void inject (FileManagerMainActivity fileManagerMainActivity);
    void inject (FileLocalMainActivity fileLocalMainActivity);
    void inject (FileRecentMainActivity fileRecentMainActivity);
    void inject (FileOpenActivity fileOpenActivity);
    void inject (FileRecentMainFragment fileRecentMainFragment);
    void inject (FileLocalMainFragment fileLocalMainFragment);
    void inject (AllFragment allFragment);
    void inject (DocFragment docFragment);
    void inject (PhotoFragment photoFragment);
    void inject (AudioFragment audioFragment);
    void inject (VedioFragment vedioFragment);
    void inject (OtherFragment otherFragment);
    void inject (FileOpenAudioPlayFragment fileOpenAudioPlayFragment);
    void inject (FileOpenDownloadFragment fileOpenDownloadFragment);
    void inject (FileOpenUnVailableFragment fileOpenUnVailableFragment);
    void inject (FileOpenFragment fileOpenFragment);
    void inject (FileOpenApkInstallFragment fileOpenApkInstallFragment);
}
