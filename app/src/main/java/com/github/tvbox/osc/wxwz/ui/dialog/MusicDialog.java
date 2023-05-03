package com.github.tvbox.osc.wxwz.ui.dialog;

import static com.github.tvbox.osc.wxwz.util.DownloadDriveUtils.thread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.DriveFolderFile;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.ui.activity.DriveActivity;
import com.github.tvbox.osc.ui.dialog.BaseDialog;
import com.github.tvbox.osc.util.StorageDriveType;
import com.github.tvbox.osc.viewmodel.drive.AbstractDriveViewModel;
import com.github.tvbox.osc.wxwz.entity.LrcEntry;
import com.github.tvbox.osc.wxwz.util.DownloadDriveUtils;
import com.github.tvbox.osc.wxwz.util.LrcUtils;
import com.github.tvbox.osc.wxwz.util.okhttp.BasicAuthInterceptor;
import com.github.tvbox.osc.wxwz.util.FileUtils;
import com.github.tvbox.osc.wxwz.util.okhttp.WebDav;
import com.github.tvbox.osc.wxwz.util.okhttp.entity.DownloadInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MusicDialog extends BaseDialog {
    private ImageView mMusicImage;
    private TextView mMusicName;
    private TextView mMusicSinger;
    private TextView mMusicAlbumName;
    private SeekBar mMusicProgress;
    private TextView mMusicTimeLeft;
    private TextView mMusicTimeRight;
    private TextView mMusicLrc;
    private LinearLayout mMusicPause;
    private LinearLayout mMusicPrev;
    private LinearLayout mMusicExit;
    private LinearLayout mMusicFfwd;

    private boolean isPrepare = false;
    MediaPlayer mediaPlayer = new MediaPlayer();
    String title = "";
    String album = "";
    String artist = "";
    String duration = "";
    String fileName = "";
    String fileExt = "";
    Bitmap songalbum = null;
    String songPath = "";
    String songNewPath = "";
    private int playpos = -1;
    private ImageView mPlayPauseImg;
    private AbstractDriveViewModel viewModel = null;
    DriveFolderFile selectedItem = null;
    private List<DriveFolderFile> list = new ArrayList<>();
    private boolean error = false;
    public Thread thread;
    private boolean loadFinish = false;
    private int progress = 0;
    public DownloadInfo downloadInfo;
    private boolean isLyric = false;
    private List<LrcEntry> mainList = new ArrayList<>();
    private int currentLyricIndex = 0;
    private String songUrl = "";

    public MusicDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_music);
        setCanceledOnTouchOutside(false);
        initView();
        initEvent();
    }

    private void initEvent() {
        mMusicPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mMusicProgress.removeCallbacks(mUpdateProgress);
                    mPlayPauseImg.setBackgroundResource(R.drawable.v_play);
                } else {
                    mediaPlayer.start();
                    mMusicProgress.postDelayed(mUpdateProgress, 10);
                    mPlayPauseImg.setBackgroundResource(R.drawable.v_pause);
                }
            }
        });

        mMusicLrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mMusicExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mMusicProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mMusicPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prev();
            }
        });
        mMusicFfwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPrepare = false;
                mMusicProgress.removeCallbacks(mUpdateProgress);
                mMusicProgress.setProgress(0);
                mMusicTimeLeft.setText("00:00");
                mPlayPauseImg.setBackgroundResource(R.drawable.v_play);
                if (!error) {
                    next();
                }


            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                error = true;
                return true;
            }
        });

    }

    private void initView() {
        mMusicImage = findViewById(R.id.musicImage);
        mMusicName = findViewById(R.id.music_name);
        mMusicSinger = findViewById(R.id.music_singer);
        mMusicAlbumName = findViewById(R.id.music_album_name);
        mMusicProgress = findViewById(R.id.music_progress);
        mMusicTimeLeft = findViewById(R.id.music_time_left);
        mMusicTimeRight = findViewById(R.id.music_time_right);
        mMusicPause = findViewById(R.id.music_pause);
        mMusicPrev = findViewById(R.id.music_prev);
        mMusicExit = findViewById(R.id.music_exit);
        mMusicFfwd = findViewById(R.id.music_ffwd);
        mPlayPauseImg = findViewById(R.id.play_pauseImg);
        mMusicLrc = findViewById(R.id.music_lrc);
        mMusicLrc.setSelected(true);
    }

    public void playSong(Context context, String songPath, AbstractDriveViewModel viewModel, DriveFolderFile selectedItem) {

        songUrl = songPath;
        currentLyricIndex = 0;
        if (this.viewModel == null) {
            this.viewModel = viewModel;
        }
        if (this.selectedItem == null) {
            this.selectedItem = selectedItem;
        }

        if (playpos == -1) {
            playpos = DriveActivity.filePostion;
        }

        list = DriveActivity.driveFolderFileList;

        mediaPlayer.reset();
        mediaPlayer.setLooping(false);

        if(songPath.startsWith("http")){
            if (FileUtils.isFileExists(FileUtils.getFileName(songPath),"")){
                songPath = FileUtils.getMusicCacheFile(FileUtils.getFileName(songPath),"");
            }else if (FileUtils.isFileExists(FileUtils.getFileName(songPath),"/tvbox/Download")){
                songPath = FileUtils.getMusicCacheFile(FileUtils.getFileName(songPath),"/tvbox/Download");
                Log.e("wxwz","歌曲地址:" + songPath);
            }
        }
        if (songPath.startsWith("http")) {
            fileName = FileUtils.getFileName(songPath);
            fileExt = FileUtils.getFileExt(fileName);
            String path = songPath;
            thread = new Thread() {
                public void run() {
                    DriveFolderFile currentDrive = viewModel.getCurrentDrive();
                    JsonObject config = currentDrive.getConfig();
                    WebDav webDav = new WebDav();
                    if (config.has("username") && config.has("password")) {
                        webDav.init(config.get("username").getAsString(), config.get("password").getAsString());
                    }
                    //判断文件是否存在
                    try {

                        downloadInfo = webDav.getWebDavFile(path);
                        DownloadInfo lrcinfo = webDav.getWebDavFile(FileUtils.removeExt(path) + ".lrc");

                        Log.e("wxwz","当前文件大小:" + FileUtils.getFileSize(downloadInfo.getFileSize()));

                        InputStream is = downloadInfo.getFile();
                        if (lrcinfo.getFileSize()!=0){
                            //mMusicLrc.setText("正在下载歌词...");
                            String lrcPath = FileUtils.removeExt(fileName) + ".lrc";
                            InputStream lrc = lrcinfo.getFile();
                            downloadFile(lrc,lrcPath);
                            isLyric = true;
                            getLrc(new File(lrcPath));
                        }

                        songNewPath = download(is);

                        if (!songNewPath.equals("")){
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(songNewPath);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            Message m = Message.obtain();
                            m.arg1 = 1;
                            //发送类似请求码,判断是哪个线程
                            h.sendMessage(m);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();

                        Message m = Message.obtain();
                        //发送类似请求码,判断是哪个线程
                        m.arg1 = 0;
                        m.obj = "播放出现错误!";
                        h.sendMessage(m);
                    }
                }
            };
            thread.start();


            try {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(songPath);
                //[3]准备播放
                mediaPlayer.prepareAsync(); //异步
                //[4]设置一个准备完成的一个监听
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    //当这个方法执行说明我们要播放的数据一定缓冲好了
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //[4]播放音乐
                        isPrepare = true;
                        mediaPlayer.start();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Message m = Message.obtain();
                //发送类似请求码,判断是哪个线程
                m.arg1 = 0;
                m.obj = "播放出现错误!";
                h.sendMessage(m);

            }
        } else {
            try {
                mediaPlayer.setDataSource(songPath);
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message m = Message.obtain();
                //发送类似请求码,判断是哪个线程
                m.arg1 = 0;
                m.obj = "播放出现错误!";
                h.sendMessage(m);
            }
        }
        getSongInfo(context, songPath);

    }

    public void getSongInfo(Context context, String path) {
        getLrc(new File(FileUtils.removeExt(path) + ".lrc"));
        if (path.startsWith("http")) {
            //title = mediaPlayer.getTrackInfo().toString();
            title = "正在缓存歌曲....";
            artist = "";
            album = "";
            mMusicLrc.setText("");
            songalbum = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_default_album);
            loadFinish = false;
            mMusicProgress.setProgress(0);
            mMusicProgress.setSecondaryProgress(0);
        } else {
            loadFinish = true;
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(path);

                title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); // 播放时长单位为毫秒
                byte[] pic = mmr.getEmbeddedPicture();  // 图片，可以通过BitmapFactory.decodeByteArray转换为bitmap图
                //Log.d("pic", "path" + pic);


                if (pic != null) {
                    songalbum = BitmapFactory.decodeByteArray(pic, 0, pic.length);
                    mmr.release();
                } else {
                    songalbum = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_default_album);
                }
                error = false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        mMusicName.setText(title);
        mMusicSinger.setText(artist);
        mMusicAlbumName.setText(album);
        mMusicImage.setImageBitmap(songalbum);

        mPlayPauseImg.setBackgroundResource(mediaPlayer.isPlaying() ? R.drawable.v_pause : R.drawable.v_play);
        mMusicTimeRight.setText(toTime(mediaPlayer.getDuration()));
        if (mMusicProgress != null) {
            mMusicProgress.setMax((int) mediaPlayer.getDuration());
            if (mMusicProgress != null) {
                mMusicProgress.removeCallbacks(mUpdateProgress);
            }

            mMusicProgress.postDelayed(mUpdateProgress, 10);
        }
    }

    public Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {

            int delay = 250; //not sure why this delay was so high before
            if (loadFinish){
                long position = mediaPlayer.getCurrentPosition();
                if (mMusicProgress != null) {
                    mMusicProgress.setProgress((int) position);
                    mMusicTimeLeft.setText(toTime((int) position));
                    if (isLyric){
                        if (currentLyricIndex == 0 || position >= mainList.get(currentLyricIndex).getTime()) {
                            mMusicLrc.setText(mainList.get(currentLyricIndex).getText());
                            if (currentLyricIndex<mainList.size() -1){
                                currentLyricIndex++;
                            }

                        }
                    }else {
                        mMusicLrc.setText("无歌词");
                    }
                }
            }else {
                if (mMusicProgress != null){
                    mMusicProgress.setSecondaryProgress(progress);
                    //Log.e("wxwz","进度:" + progress);
                }
            }


            mMusicProgress.postDelayed(mUpdateProgress, delay); //delay

        }
    };

    public static String toTime(int time) {

        time /= 1000;
        int minute = time / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

    Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 0) {
                if (thread != null) {
                    thread.interrupt();
                    thread = null;
                }

                mMusicName.setText((CharSequence) msg.obj);
            } else if (msg.arg1 == 1) {
                if (thread != null) {
                    thread.interrupt();
                }

                getSongInfo(getContext(), songNewPath);
            }

        }
    };

    public String download(InputStream inputStream) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(root + "/tvbox/.cache/Music");
        if (!file.exists())
            file.mkdirs();
        File filename = new File(file, fileName);

        loadFinish = false;
        try {
            // 1K的数据缓冲
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            int hasRead = 0;//已经读取了多少
            long total = downloadInfo.getFileSize();

            // 输出的文件流
            OutputStream os = new FileOutputStream(filename);
            mMusicProgress.postDelayed(mUpdateProgress,10);
            mMusicProgress.setMax(100);
            // 开始读取
            while ((len = inputStream.read(bs)) != -1) {
                os.write(bs, 0, len);
                hasRead += len;
                progress = (int) (hasRead * 1.0f / total * 100);


            }
            // 完毕，关闭所有链接
            mMusicProgress.setMax(0);
            progress = 100;
            mMusicProgress.setSecondaryProgress(100);
            mMusicProgress.removeCallbacks(mUpdateProgress);
            os.close();
            inputStream.close();
            loadFinish = true;
            return filename.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            mMusicProgress.removeCallbacks(mUpdateProgress);
            return "";
        }
    }

    public String downloadFile(InputStream inputStream,String savePath) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();

        File file = new File(root + "/tvbox/.cache/Music");
        if (!file.exists())
            file.mkdirs();
        File filename = new File(file, savePath);

        try {
            // 1K的数据缓冲
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            int hasRead = 0;//已经读取了多少
            //long total = downloadInfo.getFileSize();

            // 输出的文件流
            OutputStream os = new FileOutputStream(filename);
            // 开始读取
            while ((len = inputStream.read(bs)) != -1) {
                os.write(bs, 0, len);
                hasRead += len;
                //progress = (int) (hasRead * 1.0f / total * 100);


            }
            // 完毕，关闭所有链接
            //mMusicProgress.setMax(0);
            //progress = 100;
           // mMusicProgress.setSecondaryProgress(100);
            //mMusicProgress.removeCallbacks(mUpdateProgress);
            os.close();
            inputStream.close();
            //loadFinish = true;
            return filename.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
           // mMusicProgress.removeCallbacks(mUpdateProgress);
            return "";
        }
    }

    public void prev() {
        if (list.size() != 0) {
            DriveFolderFile currentDrive = viewModel.getCurrentDrive();
            if (playpos == 0) {
                playpos = list.size() - 1;
            } else {
                playpos--;
            }
            getSongPath();
            //判断该文件是否是音频文件
            if (StorageDriveType.isMusicType(FileUtils.getFileExt(songPath))) {
                playSong(getContext(), songPath, viewModel, selectedItem);
            } else {
                prev();
            }
        }
    }

    public void next() {
        if (list.size() != 0) {
            if (list.size() - 1 == playpos) {
                playpos = 0;

            } else {
                playpos++;
            }
            //判断该文件是否是音频文件
            getSongPath();
            if (StorageDriveType.isMusicType(FileUtils.getFileExt(songPath))) {
                playSong(getContext(), songPath, viewModel, selectedItem);
            } else {
                next();
            }

        }

    }

    public void getSongPath() {
        DriveFolderFile currentDrive = viewModel.getCurrentDrive();
        //获取音频路径
        if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV) {
            JsonObject config = currentDrive.getConfig();
            String targetPath = selectedItem.getAccessingPathStr() + list.get(playpos).name;
            songPath = config.get("url").getAsString() + targetPath;
        } else {
            songPath = getWebDavFileUrl(viewModel.getCurrentDrive().name + selectedItem.getAccessingPathStr() + list.get(playpos).name);
        }
        Log.d("wxwz", "歌曲:" + songPath);
    }

    private void getLrc(File file){
        if (!mainList.isEmpty()||mainList.size()!=0){
            mainList.clear();
        }

        if (file.exists()){
            mainList = LrcUtils.parseLrc(file);
        }

        if (mainList.isEmpty()||mainList.size()==0){
            isLyric = false;

        }else {
            isLyric = true;
        }
    }

    private String getWebDavFileUrl(String fileUrl) {

        VodInfo vodInfo = new VodInfo();
        vodInfo.name = "存储";
        vodInfo.playFlag = "drive";
        DriveFolderFile currentDrive = viewModel.getCurrentDrive();
        if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV) {
            String credentialStr = currentDrive.getWebDAVBase64Credential();
            if (credentialStr != null) {
                JsonObject playerConfig = new JsonObject();
                JsonArray headers = new JsonArray();
                JsonElement authorization = JsonParser.parseString(
                        "{ \"name\": \"authorization\", \"value\": \"Basic " + credentialStr + "\" }");
                headers.add(authorization);
                playerConfig.add("headers", headers);
                vodInfo.playerCfg = playerConfig.toString();
                fileUrl = vodInfo.playerCfg;
            }
        }


        return fileUrl;
    }


    @Override
    public void dismiss() {

        isPrepare = false;
        mMusicProgress.removeCallbacks(mUpdateProgress);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }

        if (!loadFinish&&songUrl.startsWith("http")){
            FileUtils.delFile(new File(DownloadDriveUtils.root + "/tvbox/.cache/Music",FileUtils.getFileName(songUrl)));
        }
        super.dismiss();
    }

}
