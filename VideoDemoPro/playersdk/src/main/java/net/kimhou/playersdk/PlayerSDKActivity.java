package net.kimhou.playersdk;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.VideoSurfaceView;
import com.google.android.exoplayer.util.PlayerControl;

import util.PlayerConst;
import util.VPUtil;


public class PlayerSDKActivity extends ActionBarActivity implements View.OnClickListener, SurfaceHolder.Callback, ExoPlayer.Listener, MediaCodecVideoTrackRenderer.EventListener{

    private VideoSurfaceView surfaceView;
    private ExoPlayer player;
    private MediaController mediaController;
    private MediaCodecVideoTrackRenderer videoRenderer;
    private MediaCodecAudioTrackRenderer audioTrackRenderer;
    private long position = 0;//记录播放位置
    private Boolean isPlayWhenReady = true;//是否在缓冲好时自动播放
    private Boolean hasRenderToSurface = false;//是否已经渲染到surface
    private PlayerControl playerControl;
    private String url;
    private String TAG = "[PlayerSDKActivityTAG]";

    private ImageButton replayButton;
    private ProgressBar loadingIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_player_sdk);
        loadingIcon = (ProgressBar)findViewById(R.id.loadingIcon);

        //接收参数
        Intent intent = getIntent();
        url = intent.getStringExtra(PlayerConst.VIDEO_URL);
        if(url == null || url.length() == 0){
            VPUtil.showMessage(this, "video url is null!!!");
            return;
        }

        //初始化surface
        surfaceView = (VideoSurfaceView)findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);
        //绑定点击事件, 控制显示隐藏controller
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mediaController.isShowing()) {
                        mediaController.hide();
                    } else if(hasRenderToSurface) {
                        mediaController.show(0);
                    }
                }
                return true;
            }
        });

        //创建controller
        mediaController = new MediaController(this);
        mediaController.setAnchorView(surfaceView);
        mediaController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        replayButton = (ImageButton)findViewById(R.id.replayButton);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replayButton.setVisibility(View.INVISIBLE);
                preparePlayer();
                isPlayWhenReady = true;
                play(isPlayWhenReady);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");

        //设置为横屏
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();

        replayButton.setVisibility(View.INVISIBLE);
        preparePlayer();
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");
        releasePlayer();
        isPlayWhenReady = false;
    }

    /**
     * 创建ExoPlayer
     */
    private void preparePlayer(){
        loadingIcon.setVisibility(View.VISIBLE);
        if(url == null || url.length() == 0){
            VPUtil.showMessage(this, "video url is null!!!");
            return;
        }

        if(player == null) {
            player = ExoPlayer.Factory.newInstance(2, 1000, 5000);
            player.addListener(PlayerSDKActivity.this);
            player.seekTo(position);
            Log.d(TAG, "seekTo:" + position);
            playerControl = new PlayerControl(player);
            mediaController.setMediaPlayer(playerControl);
            mediaController.setEnabled(true);
        }
        buildRenders();
    }

    /**
     * 释放player
     */
    private void releasePlayer(){
        hasRenderToSurface = false;
        mediaController.hide();
        if (player != null) {
            position = player.getCurrentPosition();
            player.release();
            player = null;
            playerControl = null;
        }
        videoRenderer = null;
    }

    /**
     * 创建videoRender和audioRender
     */
    private void buildRenders(){
        FrameworkSampleSource sampleSource = new FrameworkSampleSource(PlayerSDKActivity.this, Uri.parse(url), null, 2);
        videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource,
                MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT,
                0,
                new Handler(getMainLooper()),
                PlayerSDKActivity.this, 50);
        audioTrackRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

        player.prepare(videoRenderer, audioTrackRenderer);
        player.sendMessage(audioTrackRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0f);
    }

    /**
     * 尝试播放视频
     * @param isPlayWhenReady
     */
    private void play(Boolean isPlayWhenReady){
        Surface surface = surfaceView.getHolder().getSurface();
        if(surface == null || !surface.isValid()){
            Log.d(TAG, "surface not ready");
            return;
        }
        hasRenderToSurface = false;
        player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
        player.setPlayWhenReady(isPlayWhenReady);
    }

    /**
     * 重新播放
     */
    private void showReplay(){
        loadingIcon.setVisibility(View.INVISIBLE);
        mediaController.hide();
        replayButton.setVisibility(View.VISIBLE);
        releasePlayer();
        position = 0;
    }

    public void onClick(View v){
        Log.d(TAG, "onClick");

    }

    // ExoPlayer.Listener implementation

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        // Do nothing.
        Log.d(TAG, "onPlayerStateChanged, " + playWhenReady + ", " + playbackState);

        isPlayWhenReady = playWhenReady;
        //渲染到surface loading状态
        if(isPlayWhenReady && !hasRenderToSurface){
            loadingIcon.setVisibility(View.VISIBLE);
        }
        switch (playbackState){
            case ExoPlayer.STATE_ENDED:
                loadingIcon.setVisibility(View.INVISIBLE);
                player.seekTo(0);
                showReplay();
                break;
            case ExoPlayer.STATE_READY:
                if(!isPlayWhenReady){
                    loadingIcon.setVisibility(View.INVISIBLE);
                    mediaController.show(0);
                }
                if(isPlayWhenReady && hasRenderToSurface){
                    loadingIcon.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                loadingIcon.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        // Do nothing.
        Log.d(TAG, "onPlayWhenReadyCommitted");
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        Log.d(TAG, "onPlayerError" + e.getMessage());
        VPUtil.showMessage(this, "发生错误, 请检验网络或者文件是否可用. error:" + e.getMessage());

        showReplay();
    }

    // MediaCodecVideoTrackRenderer.Listener

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {
        Log.d(TAG, "onVideoSizeChanged");
        surfaceView.setVideoWidthHeightRatio(height == 0 ? 1 : (pixelWidthHeightRatio * width) / height);
    }

    @Override
    public void onDrawnToSurface(Surface surface) {
        Log.d(TAG, "onDrawnToSurface");
        loadingIcon.setVisibility(View.INVISIBLE);
        hasRenderToSurface = true;
        player.sendMessage(audioTrackRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f);
    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {
        Log.d(TAG, "Dropped frames: " + count);

    }

    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
        Log.d(TAG, "onDecoderInitializationError " + e.getMessage());
        VPUtil.showMessage(this, "decoder initialization error " + e.getMessage());

        showReplay();
    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
        Log.d(TAG, "onCryptoError" + e.getMessage());
        VPUtil.showMessage(this, "onCryptoError " + e.getMessage());

        showReplay();
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

        play(isPlayWhenReady);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        hasRenderToSurface = false;
    }
}
