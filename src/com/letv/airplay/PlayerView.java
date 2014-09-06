package com.letv.airplay;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import com.letv.smartControl.R;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * 播放页面（自定义控件）
 * 
 * @author 韦念欣
 * 
 */
public class PlayerView extends RelativeLayout {
	private String TAG = PlayerView.class.getSimpleName();
	private Context context;

	// 音乐播放界面控件
	private AnimationDrawable diskAnimation;
	private RelativeLayout musicLayout;
	private FrameLayout outLayout;
	private RelativeLayout inLayout;
	private ImageView musicLogo;
	private ImageView musicReflect;
	//private ImageView musicLogoDefault;
	private TextView musicText;

	// 视频播放界面控件
	private RelativeLayout videoLayout;
	private SurfaceView surfaceView;
	private RelativeLayout soundLayout;
	private ImageView soundIcon;
	private TextView soundText;
	private VolumeProgressBar volumeSeekBar;
	private ImageView centerButton;
	private RelativeLayout controlLayout;
	private TextView currentTimeText;
	private TextView totalTimeText;
	private SeekBar controlSeekBar;
	private ProgressBar centerProgressBar;
	private ImageView progressImage;

	private String singer;
	private String title;
	private byte[] logo;
	private long lastOperateSoundTime;
	private long lastOperateControlTime;
	private int totalTime;
	private OnSeekBarChangeListener listener;

	public Handler handler;
	private int state;
	private boolean canShowVolume = false;

	private static final int INIT = 10240;
	private static final int TYPE_MUSIC_NO_PIC = INIT + 1;
	private static final int TYPE_MUSIC_WITH_PIC = INIT + 2;
	private static final int TYPE_VIDEO = INIT + 3;
	private static final int TYPE_VIDEO_MUSIC = INIT + 4;
	private static final int TYPE_ERROR = INIT + 5;
	private static final int TYPE_PURE_BG = INIT + 100;

	private static final int SET_MUSIC_INFO = INIT + 6;
	private static final int SET_SOUND_INFO = INIT + 7;
	private static final int SET_CENTER_BUTTON = INIT + 8;
	private static final int SET_CENTER_PROGRESSBAR = INIT + 9;
	private static final int SET_VIDEO_TOTAL_TIME = INIT + 10;
	private static final int SET_VIDEO_CURRENT_TIME = INIT + 11;
	private static final int SET_VIDEO_BUFFERE = INIT + 12;
	private static final int SET_CONTROL_VISIBILITY = INIT + 13;
	private static final int SET_CONTROL_IMAGE = INIT + 14;
	private static final int START_DISK_ANIMATION = INIT + 15;
	private static final int STOP_DISK_ANIMATION = INIT + 16;

	public static final int PROGRESS_TYPE_PLAY = 0;
	public static final int PROGRESS_TYPE_PAUSE = 1;
	public static final int PROGRESS_TYPE_SPEED = 2;
	public static final int PROGRESS_TYPE_REVERSE = 3;

	public PlayerView(Context context) {
		super(context);
		handler = new EventHandler(context.getMainLooper());
		this.context = context;
		init();
	}

	/**
	 * 设置为无背景显示效果
	 */
	void switchToPureBG() {
		handler.sendMessageDelayed(handler.obtainMessage(TYPE_PURE_BG), 1000);
	}

	public void removePureBG() {
		LetvLog.d(TAG, "handleMessage removePureBG");
		handler.removeMessages(TYPE_PURE_BG);
	}

	/**
	 * 设置为无音乐LOGO显示效果
	 */
	public void switchToPlayingNoPic() {
		handler.sendEmptyMessage(TYPE_MUSIC_NO_PIC);
	}

	/**
	 * 设置为有音乐LOGO显示效果
	 */
	public void switchToPlayingWithPic() {
		handler.sendEmptyMessage(TYPE_MUSIC_WITH_PIC);
	}

	/**
	 * 设置为播放视频的显示效果
	 */
	public void switchToPlayingVideo() {
		handler.sendEmptyMessage(TYPE_VIDEO);
	}

	/**
	 * 设置为播放视频音乐的显示效果(使用视频界面来播放音乐)
	 */
	public void switchToPlayingVideoMusic() {
		handler.sendEmptyMessage(TYPE_VIDEO_MUSIC);
	}

	/**
	 * 设置为播放错误显示效果
	 */
	public void switchToPlayingError() {
		handler.sendEmptyMessage(TYPE_ERROR);
	}

	/**
	 * 设置音乐歌手
	 * 
	 * @param singer
	 */
	public void setMusicSinger(String singer) {
		this.singer = singer;
		handler.sendEmptyMessage(SET_MUSIC_INFO);
	}

	/**
	 * 设置音乐标题
	 * 
	 * @param title
	 */
	public void setMusicTitle(String title) {
		this.title = title;
		handler.sendEmptyMessage(SET_MUSIC_INFO);
	}

	/**
	 * 设置音乐专辑封面图片
	 * 
	 * @param logo
	 */
	public void setMusicLogo(byte[] logo) {
		this.logo = logo;
		LetvLog.d(TAG, "setMusicLogo handleMessage removePureBG");
		handler.removeMessages(TYPE_PURE_BG);
		handler.sendEmptyMessage(TYPE_MUSIC_WITH_PIC);
	}

	/**
	 * 获取SurfaceView
	 * 
	 * @return
	 */
	public SurfaceView getSurfaceView() {
		return surfaceView;
	}

	/**
	 * 设置音量(0~15)
	 * 
	 * @param sound
	 *            音量范围：0到15
	 */
	public void setSound(int sound) {
		if (sound < 0) {
			sound = 0;
		} else if (sound > 15) {
			sound = 15;
		}
		handler.sendMessage(handler.obtainMessage(SET_SOUND_INFO, sound));
	}

	/**
	 * 设置ProgressBar前面的标志
	 * 
	 * @param type
	 *            0播放 1暂停 2快进 3快退
	 */
	public void setProgressBarImage(int type) {
		handler.sendMessage(handler.obtainMessage(SET_CONTROL_IMAGE, type));
	}

	/**
	 * 设置中间按钮是否显示
	 * 
	 * @param isShow
	 */
	public void setCenterButtonShow(boolean isShow) {
		handler.sendMessage(handler.obtainMessage(SET_CENTER_BUTTON, isShow));
	}

	/**
	 * 设置中间ProgressBar是否显示
	 * 
	 * @param isShow
	 */
	public void setCenterProgressBarShow(boolean isShow) {
		LetvLog.e(TAG, "onInfo " + "setCenterProgressBarShow " + isShow);
		handler.sendMessage(handler.obtainMessage(SET_CENTER_PROGRESSBAR,
				isShow));
	}

	/**
	 * 设置视频总时间长度
	 * 
	 * @param titleTime
	 *            (传入seekbar时将其转换为100)
	 */
	public void setTotalTime(int totalTime) {
		totalTime /= 1000;
		int minute = totalTime / 60;
		int hour = minute / 60;
		int second = totalTime % 60;
		minute %= 60;

		this.totalTime = totalTime;
		String text = String.format("%02d:%02d:%02d", hour, minute, second);
		handler.sendMessage(handler.obtainMessage(SET_VIDEO_TOTAL_TIME,
				totalTime, 0, text));
	}

	/**
	 * 设置视频播放的位置
	 * 
	 * @param currentTime
	 */
	public void setCurrentTime(int currentTime) {
		currentTime /= 1000;
		int minute = currentTime / 60;
		int hour = minute / 60;
		int second = currentTime % 60;
		minute %= 60;

		String text = String.format("%02d:%02d:%02d", hour, minute, second);
		handler.sendMessage(handler.obtainMessage(SET_VIDEO_CURRENT_TIME,
				currentTime, 0, text));
	}

	/**
	 * 设置视频缓冲进度(长度100)
	 * 
	 * @param progress
	 */
	public void setControlBuffereProgress(int progress) {
		handler.sendMessage(handler.obtainMessage(SET_VIDEO_BUFFERE, progress));
	}

	/**
	 * 设置视频控制条是否显示
	 * 
	 * @param visibility
	 */
	public void setControlSeekBarVisibility(int visibility) {
		handler.sendMessage(handler.obtainMessage(SET_CONTROL_VISIBILITY,
				visibility));
	}

	/**
	 * 设置视频控制条显示8秒钟
	 */
	public void ShowControlSeekBar8s() {
		setControlSeekBarVisibility(View.VISIBLE);
		lastOperateControlTime = System.currentTimeMillis();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (System.currentTimeMillis() - lastOperateControlTime > 7000) {
					setControlSeekBarVisibility(View.INVISIBLE);
				}
			}
		}, 8000);
	}

	/**
	 * 开始播放光盘动画
	 */
	public void startDiskAnimatinon() {
		handler.sendEmptyMessage(START_DISK_ANIMATION);
	}

	/**
	 * 停止播放光盘动画
	 */
	public void stopDiskAnimation() {
		handler.sendEmptyMessage(STOP_DISK_ANIMATION);
	}

	/**
	 * 消息处理Handler
	 */
	private class EventHandler extends Handler {

		public EventHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			LetvLog.d(TAG, "handleMessage msg.what: " + msg.what);
			// Log.d(TAG, "handleMessage currentThread: "
			// +Thread.currentThread().getId());
			super.handleMessage(msg);
			switch (msg.what) {
			case TYPE_PURE_BG:
				switchToPureHandle();
				break;
			case SET_VIDEO_CURRENT_TIME:
				setCurrentTimeHandle(msg.arg1, (String) msg.obj);
				break;
			case SET_VIDEO_BUFFERE:
				setControlBuffereProgressHandle((Integer) msg.obj);
				break;
			case TYPE_MUSIC_NO_PIC:
				switchToPlayingNoPicHandle();
				break;
			case TYPE_MUSIC_WITH_PIC:
				switchToPlayingWithPicHandle();
				break;
			case TYPE_VIDEO:
				switchToPlayingVideoHandle();
				break;
			case TYPE_VIDEO_MUSIC:
				switchToPlayingVideoMusicHandle();
				break;
			case TYPE_ERROR:
				switchToPlayingErrorHandle();
				break;
			case SET_MUSIC_INFO:
				setMusicInfoHandle();
				break;
			case SET_SOUND_INFO:
				setSoundInfoHandle((Integer) msg.obj);
				break;
			case SET_CENTER_BUTTON:
				setCenterButtonHandle((Boolean) msg.obj);
				break;
			case SET_CENTER_PROGRESSBAR:
				setCenterProgressBarHandle((Boolean) msg.obj);
				break;
			case SET_VIDEO_TOTAL_TIME:
				setTotalTimeHandle(msg.arg1, (String) msg.obj);
				break;
			case SET_CONTROL_VISIBILITY:
				setControlSeekBarVisibilityHandle((Integer) msg.obj);
				break;
			case SET_CONTROL_IMAGE:
				setProgressBarImageHandle((Integer) msg.obj);
				break;
			case START_DISK_ANIMATION:
				startDiskAnimatinonHandle();
				break;
			case STOP_DISK_ANIMATION:
				stopDiskAnimationHandle();
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 初始化操作
	 */
	private void init() {

		// Log.d(TAG, "playerview init currentThread: "
		// +Thread.currentThread().getId());
		View view;
		
		if (LetvUtils.isLetvUi3Version()) {
			view = View.inflate(context, R.layout.player_view_ui3, this);
		} else {
			view = View.inflate(context, R.layout.player_view, this);
		}

		// 获取音乐界面控件
		musicLayout = (RelativeLayout) view
				.findViewById(R.id.player_view_music);
		outLayout = (FrameLayout) view
				.findViewById(R.id.player_view_music_layout_out);
		inLayout = (RelativeLayout) view
				.findViewById(R.id.player_view_music_layout_in);
		musicLogo = (ImageView) view.findViewById(R.id.player_view_music_logo);
		musicReflect = (ImageView) view
				.findViewById(R.id.player_view_music_reflect);
		//musicLogoDefault = (ImageView) view
		//		.findViewById(R.id.player_view_music_logo_default);
		musicText = (TextView) view.findViewById(R.id.player_view_music_text);

		// 获取音乐界面光盘动画
		ImageView imageView = (ImageView) view
				.findViewById(R.id.player_view_music_image_disk);
		diskAnimation = (AnimationDrawable) imageView.getBackground();

		// 获取视频界面控件
		videoLayout = (RelativeLayout) view
				.findViewById(R.id.player_view_video);
		surfaceView = (SurfaceView) view
				.findViewById(R.id.player_view_video_surfaceview);
		soundLayout = (RelativeLayout) view
				.findViewById(R.id.player_view_sound);
		soundIcon = (ImageView) view
				.findViewById(R.id.player_view_video_sound_icon);
		soundText = (TextView) view
				.findViewById(R.id.player_view_video_sound_text);
		volumeSeekBar = (VolumeProgressBar) view
				.findViewById(R.id.player_view_video_sound_volume);
		centerButton = (ImageView) view
				.findViewById(R.id.player_view_video_center_btn);
		centerProgressBar = (ProgressBar) view
				.findViewById(R.id.player_view_video_center_progressbar);
		controlLayout = (RelativeLayout) view
				.findViewById(R.id.player_view_video_ctrl);
		
		if (LetvUtils.isLetvUi3Version()) {
			
		} else {		
		currentTimeText = (TextView) view
				.findViewById(R.id.player_view_video_ctrl_now_time);
		}
		
		totalTimeText = (TextView) view
					.findViewById(R.id.player_view_video_ctrl_total_time);		
		
		
		controlSeekBar = (SeekBar) view
				.findViewById(R.id.player_view_video_ctrl_seekBar);
		
		if (LetvUtils.isLetvUi3Version()) {
			controlSeekBar.setThumb(getThumbDrawable("00:00:00"));
		}
		
		progressImage = (ImageView) view
				.findViewById(R.id.player_view_video_ctrl_image);
		controlSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						if (listener != null) {
							listener.onStopTrackingTouch(seekBar);
						}
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						if (listener != null) {
							listener.onStartTrackingTouch(seekBar);
						}
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (listener != null) {
							listener.onProgressChanged(seekBar, progress,
									fromUser);
						}
					}
				});

		// 初始化状态为：无图片音乐播放界面
		// switchToPlayingNoPic();
		// switchToPureBG();
	}

	private void switchToPureHandle() {
		if (TYPE_ERROR == state) {
			removeAllViews();
			init();
		}

		state = TYPE_PURE_BG;
		logo = null;
		stopDiskAnimationHandle();

		musicLayout.setVisibility(View.VISIBLE);
		videoLayout.setVisibility(View.INVISIBLE);
		surfaceView.setVisibility(View.GONE);
		// soundLayout.setVisibility(View.INVISIBLE);
		//musicLogoDefault.setVisibility(View.VISIBLE);
		inLayout.setVisibility(View.INVISIBLE);
		setMusicInfoHandle();
		startDiskAnimatinonHandle();
	}

	/**
	 * 设置为无音乐LOGO显示效果
	 */
	private void switchToPlayingNoPicHandle() {
		if (TYPE_ERROR == state) {
			removeAllViews();
			init();
		}

		state = TYPE_MUSIC_NO_PIC;
		logo = null;
		stopDiskAnimationHandle();
		musicLayout.setVisibility(View.VISIBLE);
		videoLayout.setVisibility(View.INVISIBLE);
		surfaceView.setVisibility(View.GONE);
		// soundLayout.setVisibility(View.INVISIBLE);
		//musicLogoDefault.setVisibility(View.VISIBLE);
		inLayout.setVisibility(View.INVISIBLE);
		setMusicInfoHandle();
		startDiskAnimatinonHandle();
	}

	/**
	 * 设置为有音乐LOGO显示效果
	 */
	private void switchToPlayingWithPicHandle() {
		if (TYPE_ERROR == state) {
			removeAllViews();
			init();
		}

		state = TYPE_MUSIC_WITH_PIC;
		stopDiskAnimationHandle();
		musicLayout.setVisibility(View.VISIBLE);
		videoLayout.setVisibility(View.INVISIBLE);
		surfaceView.setVisibility(View.GONE);
		inLayout.setVisibility(View.VISIBLE);
		// soundLayout.setVisibility(View.INVISIBLE);
		//musicLogoDefault.setVisibility(View.INVISIBLE);
		setMusicInfoHandle();
		startDiskAnimatinonHandle();

	}

	/**
	 * 设置为播放视频的显示效果
	 */
	private void switchToPlayingVideoHandle() {
		if (TYPE_ERROR == state) {
			removeAllViews();
			init();
		}

		state = TYPE_VIDEO;
		stopDiskAnimationHandle();
		videoLayout.setVisibility(View.VISIBLE);
		musicLayout.setVisibility(View.INVISIBLE);
		surfaceView.setVisibility(View.VISIBLE);
		controlLayout.setVisibility(View.VISIBLE);
		// soundLayout.setVisibility(View.INVISIBLE);
		centerButton.setVisibility(View.INVISIBLE);
	}

	/**
	 * 设置为播放视频音乐的显示效果(使用视频界面来播放音乐)
	 */
	private void switchToPlayingVideoMusicHandle() {
		if (TYPE_ERROR == state) {
			removeAllViews();
			init();
		}

		state = TYPE_VIDEO_MUSIC;
		stopDiskAnimationHandle();
		videoLayout.setVisibility(View.VISIBLE);
		surfaceView.setVisibility(View.GONE);
		musicLayout.setVisibility(View.VISIBLE);
		//musicLogoDefault.setVisibility(View.VISIBLE);
		inLayout.setVisibility(View.INVISIBLE);
		controlLayout.setVisibility(View.VISIBLE);
		// soundLayout.setVisibility(View.INVISIBLE);
		centerButton.setVisibility(View.INVISIBLE);
		setMusicInfoHandle();
		startDiskAnimatinonHandle();
	}

	/**
	 * 设置为播放错误显示效果
	 */
	private void switchToPlayingErrorHandle() {
		state = TYPE_ERROR;
		stopDiskAnimationHandle();
		removeAllViews();
		System.gc();
		View.inflate(context, R.layout.playing_backgroud_error, this);
	}

	/**
	 * 设置音乐信息
	 */
	private void setMusicInfoHandle() {
		setMusicText();
		if (state == TYPE_MUSIC_WITH_PIC) {
			setMusicLogo();
		}
	}

	/**
	 * 设置音乐文字信息（歌手、歌名）
	 */
	private void setMusicText() {
		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(this.singer)) {
			sb.append(this.singer).append("  ");
		}
		if (!TextUtils.isEmpty(this.title)) {
			sb.append(this.title);
		}
		if (musicText != null) {
			musicText.setText(sb.toString());
		}
	}

	/**
	 * 设置音乐专辑图片LOGO
	 */
	private void setMusicLogo() {
		// Log.d(TAG, "setMusicLogo currentThread: "
		// +Thread.currentThread().getId());
		if (logo == null || outLayout == null || inLayout == null
				|| musicLogo == null || musicReflect == null) {
			return;
		}

		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 加载音乐LOG
		Bitmap bitmap = BitmapFactory
				.decodeByteArray(logo, 0, logo.length, opt);
		if (bitmap == null) {
			return;
		}

		// 保持比例自动适应宽高
		ViewGroup.LayoutParams outParams = outLayout.getLayoutParams();
		int outWidth = outParams.width;
		int outHeight = outParams.height;

		int picWidth = bitmap.getWidth();
		int picHeight = bitmap.getHeight();

		if (picWidth > picHeight) {
			float scale = (float) picWidth / outWidth;
			picWidth = outWidth;
			picHeight = Math.round(picHeight / scale);
		} else if (picWidth < picHeight) {
			float scale = (float) picHeight / outHeight;
			picHeight = outHeight;
			picWidth = Math.round(picWidth / scale);
		} else {
			picWidth = outWidth;
			picHeight = outHeight;
		}

		ViewGroup.LayoutParams inParams = inLayout.getLayoutParams();
		inParams.width = picWidth;
		inParams.height = picHeight;
		inLayout.setLayoutParams(inParams);

		ViewGroup.LayoutParams logoParams = musicLogo.getLayoutParams();
		logoParams.width = picWidth - 6;
		logoParams.height = picHeight - 6;
		musicLogo.setLayoutParams(logoParams);
		SoftReference<Bitmap> sf = new SoftReference<Bitmap>(bitmap);
		musicLogo.setImageBitmap(sf.get());

		// 设置音乐LOGO白光
		Bitmap bp = readBitMap(context,R.drawable.player_audio_push_cover_reflect);
		WeakReference<Bitmap> wr = new WeakReference<Bitmap>(bp);
		SoftReference<Drawable> srf = new SoftReference<Drawable>(new BitmapDrawable(wr.get()));

		ViewGroup.LayoutParams reflectParams = musicReflect.getLayoutParams();
		reflectParams.width = logoParams.width;
		reflectParams.height = Math.round(logoParams.height * 0.75f);
		musicReflect.setLayoutParams(reflectParams);
		musicReflect.setBackgroundDrawable(srf.get());
		System.gc();
	}

	public static Bitmap readBitMap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		// opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;

		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	/**
	 * 设置音量值和图片
	 * yh
	 */
	private void setSoundInfoHandle(int sound) {

		if (soundLayout == null || volumeSeekBar == null || soundText == null
				|| soundIcon == null) {
			System.out.println("addd==null");
			return;
		}
		// if (!canShowVolume) {
		// System.out.println("canShowVolume");
		// soundLayout.setVisibility(View.INVISIBLE);
		// return;
		// }

		soundLayout.setVisibility(View.VISIBLE);
		soundLayout.bringToFront();

		volumeSeekBar.setProgress(sound);
		soundText.setText(sound + "");

		if (sound == 0) {
			soundIcon.setBackgroundResource(R.drawable.play_sound_off_ico);
		} else {
			soundIcon.setBackgroundResource(R.drawable.play_sound_on_ico);
		}

		// 5秒后隐藏音量布局
		lastOperateSoundTime = System.currentTimeMillis();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (System.currentTimeMillis() - lastOperateSoundTime > 4000) {
					soundLayout.setVisibility(View.INVISIBLE);
				}
			}
		}, 5000);
	}

	/**
	 * 设置中间按钮是否显示
	 * 
	 * @param isShow
	 */
	private void setCenterButtonHandle(boolean isShow) {
		if (centerButton == null)
			return;

		if (isShow) {
			centerButton.setVisibility(View.VISIBLE);
		} else {
			centerButton.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 设置中间ProgressBar是否显示
	 * 
	 * @param isShow
	 */
	private void setCenterProgressBarHandle(boolean isShow) {
		// Log.d(TAG, "setCenterProgressBarHandle currentThread: "
		// +Thread.currentThread().getId());

		if (centerProgressBar == null)
			return;

		if (isShow) {
			centerProgressBar.setVisibility(View.VISIBLE);
		} else {
			centerProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 设置视频总时间长度
	 * 
	 * @param totalTime
	 *            (传入seekbar时将其转换为100，与缓冲进度条统一)
	 * @param text
	 */
	private void setTotalTimeHandle(int totalTime, String text) {
		if (totalTimeText == null || controlSeekBar == null)
			return;

		totalTimeText.setText(text);
		controlSeekBar.setMax(100);

	}

	/**
	 * 设置当前播放的视频位置
	 * 
	 * @param currentTime
	 * @param text
	 */
	private void setCurrentTimeHandle(int currentTime, String text) {
		if (controlSeekBar == null)
			return;
		
		if (LetvUtils.isLetvUi3Version()) {
			controlSeekBar.setThumb(getThumbDrawable(text));
		} else {
			if(currentTimeText == null)
				return;
			currentTimeText.setText(text);
			
		}
		controlSeekBar.setProgress(Math.round(100f * currentTime / totalTime));
	}

	private Drawable getThumbDrawable(String text) {
		int fontSize = (int) this.context.getResources().getDimension(
				R.dimen.thumb_position_font_size);
		int fontX = (int) this.context.getResources().getDimension(
				R.dimen.thumb_position_font_x);
		int fontY = (int) this.context.getResources().getDimension(
				R.dimen.thumb_position_font_y);
		Bitmap bitmap = BitmapFactory.decodeResource(this.context.getResources(),
				R.drawable.playbar_time_box)
				.copy(Bitmap.Config.ARGB_8888, true);
		Paint paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(this.context.getResources().getColor(android.R.color.white));
		paint.setTextSize(fontSize);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(text, fontX, fontY, paint);
		Drawable newThumb = new BitmapDrawable(this.context.getResources(), bitmap);
		return newThumb;
	}
	
	/**
	 * 设置视频缓冲进度
	 * 
	 * @param progress
	 */
	private void setControlBuffereProgressHandle(int progress) {
		if (controlSeekBar != null) {
			controlSeekBar.setSecondaryProgress(progress);
		}
	}

	/**
	 * 设置视频控制条是否显示
	 * 
	 * @param visibility
	 */
	private void setControlSeekBarVisibilityHandle(int visibility) {
		if (controlLayout != null) {
			controlLayout.setVisibility(visibility);
		}
	}

	/**
	 * 设置ProgressBar前面的标志
	 * 
	 * @param type
	 *            0播放 1暂停 2快进 3快退
	 */
	private void setProgressBarImageHandle(int type) {
		switch (type) {
		case 0:
			progressImage.setBackgroundResource(R.drawable.ic_playbar_button_play);
			break;
		case 1:
			progressImage.setBackgroundResource(R.drawable.ic_playbar_button_pause);
			break;
		case 2:
			progressImage.setBackgroundResource(R.drawable.ic_playbar_button_fast_3);
			break;
		case 3:
			progressImage.setBackgroundResource(R.drawable.ic_playbar_button_retreat_3);
			break;
		default:
			progressImage.setBackgroundResource(R.drawable.ic_playbar_button_play);
			break;
		}
	}

	/**
	 * 开始播放光盘动画
	 */
	private void startDiskAnimatinonHandle() {
		// Log.d(TAG, "startDiskAnimatinonHandle currentThread: "
		// +Thread.currentThread().getId());
		if (diskAnimation != null && !diskAnimation.isRunning()) {
			diskAnimation.start();
		}
	}

	/**
	 * 停止播放光盘动画
	 */
	private void stopDiskAnimationHandle() {
		if (diskAnimation != null && diskAnimation.isRunning()) {
			diskAnimation.stop();
		}
	}

	/**
	 * SeekBar Listener
	 */
	public interface OnSeekBarChangeListener {
		public void onStopTrackingTouch(SeekBar seekBar);

		public void onStartTrackingTouch(SeekBar seekBar);

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser);
	}

	/**
	 * 设置Listener
	 * 
	 * @param listener
	 */
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
		this.listener = listener;
	}
}
