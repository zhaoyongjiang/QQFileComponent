package cn.xxt.file.ui.fileFragment;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import cn.xxt.file.internal.data.local.FileDb;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.library.ui.base.BasePresenter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * 跟FileBaseFragment数据交互
 *
 * Created by zyj on 2017/8/25.
 */

public class FragmentPresenter extends BasePresenter<FragmentMvpView> {

    private Subscription getFileInfoSubscription;

    @Inject
    public FragmentPresenter() {
    }

    @Override
    public void attachView(FragmentMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        unsubscribe(getFileInfoSubscription);
        super.detachView();
    }

    public void getFileInfoListFromLocalDb(final Context context, final int webId, final int fileType) {
        try {
            checkViewAttached();
            unsubscribe(getFileInfoSubscription);
            getFileInfoSubscription = Observable.create(new Observable.OnSubscribe<List<FileInfo>>() {
                @Override
                public void call(Subscriber<? super List<FileInfo>> subscriber) {
                    List<FileInfo> list = new ArrayList<>();
                    if (fileType == FileTypeEnum.TYPE_ALL.getFileType()) {
                        list = FileDb.getInstance(context).getAllFile(webId);
                    } else {
                        list = FileDb.getInstance(context).getFileWithType(webId, fileType);
                    }
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<List<FileInfo>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            //
                        }

                        @Override
                        public void onNext(List<FileInfo> fileInfoList) {
                            if (getMvpView() != null) {
                                getMvpView().onSuccessGetFileInfo(fileInfoList);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unsubscribe(Subscription subscription) {
        if (null != subscription) {
            subscription.unsubscribe();
        }
    }
}
