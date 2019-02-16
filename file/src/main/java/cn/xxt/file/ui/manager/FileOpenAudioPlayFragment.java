package cn.xxt.file.ui.manager;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jakewharton.rxbinding.view.RxView;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.ui.base.FileBaseFragment;
import cn.xxt.file.util.FileUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.chad.library.adapter.base.listener.SimpleClickListener.TAG;

/**
 * Created by zyj on 2017/9/9.
 */

public class FileOpenAudioPlayFragment extends FileBaseFragment {

    @BindView(R2.id.iftv_audio_icon)
    IconFontTextView iftvAudioIcon;

    @BindView(R2.id.iv_pause)
    ImageView ivPause;

    @BindView(R2.id.iv_play)
    ImageView ivPlay;

    @BindView(R2.id.tv_file_name)
    TextView tvFileName;

    @BindView(R2.id.tv_file_size)
    TextView tvFileSize;

    @BindView(R2.id.tv_played)
    TextView tvAudioPlayed;

    @BindView(R2.id.tv_length)
    TextView tvAudioLength;

    @BindView(R2.id.sb_progress)
    SeekBar seekBar;

    @Inject
    @ActivityContext
    Context context;

    private FileInfo fileInfo;

    /** 定时器，用于更新进度条 */
    private ScheduledExecutorService progressExecutorService;

    private static MediaPlayer mediaPlayer;

    private boolean isDragging = false;

    private boolean isPrepared = false;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            updateProgressUi();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        injectThis();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_file_open_audio_play_file;
    }

    @Override
    public void initView() {
        initViews();

        initPlayer();
        play();
    }

    @Override
    public void onResume() {
        super.onResume();
//        play();
    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stop();
    }

    /**
     * 开发给openActivity调用。传进来：待定，文件实体
     * @param fileInfo
     */
    public void initData(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    private void initViews() {
        //图标
        int iconCode = FileUtil.getFileIconCodeByFileType(context, fileInfo.fileType);
        int iconColor = FileUtil.getFileIconColorByFileType(context, fileInfo.fileType);
        iftvAudioIcon.setText(iconCode);
        iftvAudioIcon.setTextColor(ContextCompat.getColor(context, iconColor));

        //文件名称
        String fileName = fileInfo.fileName;
        tvFileName.setText(fileName);

        //文件大小
        String fileSize = FileUtil.FormetFileSize(fileInfo.getFileSize());
        tvFileSize.setText(fileSize);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isDragging= true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isDragging= false;
                if (mediaPlayer!=null && isPrepared) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    play();
                }
            }
        });

        RxView.clicks(ivPause)
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
                        toggle();
                    }
                });

        RxView.clicks(ivPlay)
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
                        toggle();
                    }
                });
    }

    private void injectThis(){
        Activity activity = getActivity();
        if (activity instanceof FileOpenActivity) {
            ((FileOpenActivity) activity).getActivityComponent().inject(this);
        }
    }

    private void initPlayer() {
        if (mediaPlayer==null) {
            mediaPlayer = new MediaPlayer();
        }

        try {
            mediaPlayer.reset();
            isPrepared = false;
            mediaPlayer.setDataSource(fileInfo.getFileLocalPath());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    isPrepared = true;
                    updateProgressUi();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    stop();
                    return false;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toggle() {
        if (mediaPlayer!=null) {
            if (mediaPlayer.isPlaying()) {
                pause();
            } else {
                play();
            }
        } else {
            initPlayer();

            play();
        }
    }

    public void play() {
        if (null == mediaPlayer) {
            initPlayer();
        }

        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                while (!isPrepared) {
                    Log.i(TAG, "call: ");
                }
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Boolean flag) {
                        if (mediaPlayer!=null) {
                            mediaPlayer.start();
                            updateProgressUi();
                            startProgressExecutorService();
                        }
                    }
                });
    }

    public void pause() {
        if (mediaPlayer!=null) {
            mediaPlayer.pause();
            updateProgressUi();
        }
        cancelProgressExecutorService();
    }

    public void stop() {
        Log.d("luxy", "AudioPlayer stop");
        cancelProgressExecutorService();
        if (mediaPlayer != null) {
            updateProgressUi();
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Observable.just(1)
                .delay(200, TimeUnit.MILLISECONDS)
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
//                        isPrepared = false;
                        seekBar.setProgress(0);
                        tvAudioPlayed.setText("00:00");
                        ivPlay.setVisibility(View.GONE);
                        ivPause.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void updateProgressUi() {
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
                        if (!isDragging && mediaPlayer!=null) {
                            if (mediaPlayer.isPlaying()) {
                                ivPlay.setVisibility(View.VISIBLE);
                                ivPause.setVisibility(View.GONE);
                            } else {
                                ivPlay.setVisibility(View.GONE);
                                ivPause.setVisibility(View.VISIBLE);
                            }

                            try {
                                if (isPrepared) {
                                    int position = mediaPlayer.getCurrentPosition();
                                    int duration = mediaPlayer.getDuration();

                                    if (duration < 1000) {
                                        position = 1000;
                                        duration = 1000;
                                    }

                                    if (duration > 0) {
                                        if (!generateTime(duration).equals(tvAudioLength.getText().toString())) {
                                            tvAudioLength.setText(generateTime(duration));
                                            seekBar.setMax(duration);
                                        }

                                        tvAudioPlayed.setText(generateTime(position));
                                        seekBar.setProgress(position);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            ivPlay.setVisibility(View.GONE);
                            ivPause.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void startProgressExecutorService() {
        cancelProgressExecutorService();
        progressExecutorService = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder()
                .setDaemon(true).setNameFormat("progress-executor-service-pool-%d").build());
        progressExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    mHandler.obtainMessage();
                    mHandler.sendEmptyMessage(1);
                } catch (Exception e) {

                }
            }
        }, 200, 1000, TimeUnit.MILLISECONDS);
    }

    private void cancelProgressExecutorService() {
        if (null != progressExecutorService) {
            progressExecutorService.shutdown();
            progressExecutorService = null;
        }
    }

    /**
     * 时长格式化显示
     */
    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }
}
