package com.letv.dmr.upnp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Xml;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.StateVariable;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.QueryListener;
import org.cybergarage.upnp.device.InvalidDescriptionException;
import org.cybergarage.util.Debug;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import com.letv.dmr.MediaplayerBase;
import com.letv.upnpControl.dlna.jni_interface;
import com.letv.upnpControl.tools.FileDownLoad;
import com.letv.upnpControl.tools.LetvLog;

public class MediaRendererDevice extends Device implements ActionListener,
		QueryListener {
	public static final String TAG = MediaRendererDevice.class.getSimpleName();
	private String CurrentURI = null;

	private String InstanceId = "0";

	private String mCurPlaySpeed = "1";

	private String mCurPlayState = "NO_MEDIA_PRESENT";

	private int mCurPosition = 0;

	private String mFileName = null;

	public String mMuteState = "0";

	public String mVolume = "0";

	private String mRealTime = "00:00:00";

	private int mTotalDur = 0;

	private String mTotalDuration = "00:00:00";

	public String mVideoURL = "";

	private String netMediaType = null;

	private String playingMediaType = null;

	private HandlerThread mHandlerThread;

	private MediaStatusHandler mMediaStatushandler;

	private boolean bPwAuthValid = false;

	public boolean isUTP = false;

	public static final int SETURL = 0;

	public static final int GETTRANSPORT = 1;

	public static final int GETPOSITION = 2;

	private int mIosPlay = 0;
	
	private String mStartPostion = "0";
	
	public MediaRendererDevice() {

	}

	public MediaRendererDevice(String paramString)
			throws InvalidDescriptionException {
		super(paramString);
		setActionListener(this);
		setQueryListener(this);
		mHandlerThread = new HandlerThread("MediaStatusHandler");
		mHandlerThread.start();
		mMediaStatushandler = new MediaStatusHandler(mHandlerThread.getLooper());
	}

	private String checkMediaTypeOfNetUri(String paramString) {
		if (paramString == null)
			return null;
		if (paramString.toLowerCase().startsWith("http-get:*:video"))
			return "video/*";
		if (paramString.toLowerCase().startsWith("http-get:*:audio"))
			return "audio/*";
		if (paramString.toLowerCase().startsWith("http-get:*:image"))
			return "image/*";
		return "";
	}

	private String getFileType(String paramString) {
		URL localURL;
		try {
			localURL = new URL(paramString);
			HTTPRequest localHTTPRequest = new HTTPRequest();
			localHTTPRequest.setMethod("HEAD");
			localHTTPRequest.setURI(paramString);
			localHTTPRequest.setUserAgent("LETV-Intel-DMR");
			localHTTPRequest.setAccept("*/*");
			localHTTPRequest.setReferer("");
			localHTTPRequest.setProxyConnection("Keep-Alive");
			String host = localURL.getHost();
			int port = localURL.getPort();
			Debug.d("MediaRendererDevice", "localURL.getPort=" + port);
			if (port == -1) {
				port = 80;
			}
			HTTPResponse localHTTPResponse = localHTTPRequest.post(host, port,
					true);
			int j = 1 + localHTTPResponse.getContentType().indexOf("/");
			String fileType = localHTTPResponse.getContentType()
					.substring(0, j) + "*";
			Debug.d("MediaRendererDevice", "mediaType=" + fileType);
			return fileType;
		} catch (Exception localException) {
			Debug.d("MediaRendererDevice", "Exception ");
			return null;
		}

	}

	private String getMediaTypeByMetadata(String paramString) {
		String str = "";
		String class_str = "";
		XmlPullParser localXmlPullParser = Xml.newPullParser();
		Debug.d("MediaRendererDevice",
				"getMediaTypeByMetadata begin paramString = " + paramString);
		try {
			localXmlPullParser.setInput(new StringReader(paramString));
			int eventType = localXmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_TAG:
					Debug.d("MediaRendererDevice",
							"getMediaTypeByMetadata START_TAG get name = "
									+ localXmlPullParser.getName());
					if ("res".equals(localXmlPullParser.getName())) {
						int k = localXmlPullParser.getAttributeCount();
						for (int l = 0; l < k; ++l) {

							if ("protocolInfo".equals(localXmlPullParser
									.getAttributeName(l)))
								str = checkMediaTypeOfNetUri(localXmlPullParser
										.getAttributeValue(l));
						}
					} else if ("title".equals(localXmlPullParser.getName())) {
						this.mFileName = localXmlPullParser.nextText();
						Debug.d("MediaRendererDevice",
								"getMediaTypeByMetadata title str = "
										+ this.mFileName);
					} else if ("class".equals(localXmlPullParser.getName())) {
						class_str = localXmlPullParser.nextText();
						Debug.d("MediaRendererDevice",
								"getMediaTypeByMetadata class str = "
										+ class_str);
						if ("object.item.videoItem.movie".equals(class_str))
							class_str = "video/*";
					} else if ("utp".equals(localXmlPullParser.getName())) {

						isUTP = true;

					} else if ("ios_play".equals(localXmlPullParser.getName())) {
						mIosPlay = 1;
						Debug.d("MediaRendererDevice",
								"getMediaTypeByMetadata mIosPlay = "
										+ this.mIosPlay);
					}else if ("start".equals(localXmlPullParser.getName())) {
						mStartPostion = localXmlPullParser.nextText();
						Debug.d("MediaRendererDevice",
								"getMediaTypeByMetadata mStartPostion = "
										+ this.mStartPostion);
					}
					break;

				case XmlPullParser.TEXT:

					break;
				}
				eventType = localXmlPullParser.next();
			}
		} catch (Exception localException1) {
			Debug.d("MediaRendererDevice", "getMediaTypeByMetadata Exception ");
		}
		return str == "" ? class_str : str;
	}

	private void notifyChangeStatvar(String paramString1, String paramString2,
			String paramString3) {
		Debug.d("MediaRendererDevice", "######notifyChangeStatvar");
		if (getService(paramString1) == null)
			return;
		if (!getService(paramString1).hasStateVariable(paramString2)) {
			StateVariable localStateVariable = new StateVariable();
			localStateVariable.setName(paramString2);
			getService(paramString1).addStateVariable(localStateVariable);
		}
		setStateVariable(paramString1, paramString2, paramString3);
		getService(paramString1).notify(
				getService(paramString1).getStateVariable("LastChange"));
	}

	public boolean actionControlReceivedByUDP(int type, String... params) {
		LetvLog.w(TAG,"actionControlReceivedByUDP type = " + type);
		switch (type) {
		case SETURL:
			if (params.length == 2) {
				isUTP = false;
				this.CurrentURI = params[0];
				String urlMetaData = params[1];
				this.netMediaType = null;
				mFileName = "";
				if (urlMetaData != null) {
					this.netMediaType = getMediaTypeByMetadata(urlMetaData);
					Debug.d("MediaRendererDevice", "netMediaType = "
							+ this.netMediaType);
				}
				this.mVideoURL = this.CurrentURI;
				this.mCurPlayState = "PLAYING";
				this.playingMediaType = null;
				if (this.CurrentURI != null) {
					Debug.d("MediaRendererDevice", "######Play######");
					LetvLog.w("MediaRendererDevice", "CurrentURI="
							+ this.CurrentURI);
					this.playingMediaType = DesUtils
							.CheckMediaType(this.CurrentURI);
					if ((!this.playingMediaType.equals("audio/*"))
							&& (!this.playingMediaType.equals("image/*"))
							&& (!this.playingMediaType.equals("video/*"))) {
						if (this.netMediaType != null)
							this.playingMediaType = this.netMediaType;
					}

					if ((!this.playingMediaType.equals("audio/*"))
							&& (!this.playingMediaType.equals("image/*"))
							&& (!this.playingMediaType.equals("video/*"))) {
						this.playingMediaType = getFileType(this.CurrentURI);
						Debug.d("MediaRendererDevice", "playingMediaType2222="
								+ this.playingMediaType);
					}
					Debug.d("MediaRendererDevice", "playingMediaType="
							+ this.playingMediaType);

					if ("image/*".equals(this.playingMediaType)) {

						Intent localIntent8 = new Intent(
								"com.letv.UPNP_PLAY_ACTION");
						localIntent8.putExtra("media_type",
								this.playingMediaType);
						localIntent8.putExtra("media_uri", this.CurrentURI);
						localIntent8.putExtra("file_name", this.mFileName);
						localIntent8.putExtra("download_type", 1);
						notifyDMR(localIntent8);
						Debug.d("MediaRendererDevice",
								"######sendBroadcast(UPNP_PLAY_ACTION: image)######");
						return true;
					}
					if ("video/*".equals(this.playingMediaType)) {
						Intent localIntent7 = new Intent(
								"com.letv.UPNP_PLAY_ACTION");
						localIntent7.putExtra("media_type",
								this.playingMediaType);
						localIntent7.putExtra("media_uri", this.CurrentURI);
						localIntent7.putExtra("file_name", this.mFileName);
						notifyDMR(localIntent7);
						Debug.d("MediaRendererDevice",
								"######sendBroadcast(UPNP_PLAY_ACTION: video)######");
						return true;
					}
					if ("audio/*".equals(this.playingMediaType)) {
						Intent localIntent6 = new Intent();
						localIntent6.setAction("com.letv.UPNP_PLAY_ACTION");
						localIntent6.putExtra("media_type",
								this.playingMediaType);
						localIntent6.putExtra("media_uri", this.CurrentURI);
						localIntent6.putExtra("file_name", this.mFileName);
						notifyDMR(localIntent6);
						Debug.d("MediaRendererDevice",
								"######sendBroadcast(UPNP_PLAY_ACTION:audio)######");
						return true;
					}

					return false;
				}
				return true;
			}
			break;

		}
		return false;
	}

	void doActionReceived(String receive_data) {
		try {
			JSONObject jo = new JSONObject(receive_data);

			if (jo.has("ACTION")) {

				String control_string = jo.getString("ACTION");
				Debug.d(TAG, "receive ACTION = " + control_string);

				if (control_string != null) {

					if (control_string.equals("play_url")) {
						String playUrl = jo.getString("VALUE");
						jni_interface.TvSendPlayUrl("", playUrl);

					} else if (control_string.equals("recommended_video")) {
						String recommendedInfo = jo.getString("VALUE");
						jni_interface.TvSendRecommendedVideo("",
								recommendedInfo);
					} else if (control_string.equals("install_package")) {
						String installPackage = jo.getString("VALUE");
						jni_interface.TvInstallPackage("", installPackage);

					} else if (control_string.equals("inputText")) {
						String inputValue = jo.getString("VALUE");
						jni_interface.TvSendInputValueAction("", inputValue);

					} else if (control_string.equals("move_cursor")) {
						try {
							String mouseValue = jo.getString("VALUE");
							JSONObject mouseJo = new JSONObject(mouseValue);
							String x = mouseJo.getString("XPosition");
							String y = mouseJo.getString("YPosition");
							jni_interface.TvSendMouseActionByUdp(
									Integer.valueOf(x), Integer.valueOf(y));
						} catch (JSONException e) {
							Debug.d(TAG, "creat JSONObject error");
							e.printStackTrace();
						}

					} else if (control_string.equals("SendBroadcastIntent")) {
						String intentUri = jo.getString("VALUE");
						try {
							Intent intent = Intent.parseUri(intentUri, 0);
							if (DMRService.mContext != null) {
								DMRService.mContext.sendBroadcast(intent);
							}
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}

					} else if (control_string.equals("SendActivityIntent")) {
						String intentUri = jo.getString("VALUE");
						try {
							Intent intent = Intent.parseUri(intentUri, 0);
							if (DMRService.mContext != null) {
								int flag = intent.getFlags();
								intent.setFlags(flag | Intent.FLAG_ACTIVITY_NEW_TASK);
								Debug.d(TAG, "SendActivityIntent intent = " + intent.getClass().getSimpleName());
								DMRService.mContext.startActivity(intent);
							}
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}

					} else if (control_string.equals("DownLoadFile")) {
						String info = jo.getString("VALUE");
						try {
							JSONObject Jo = new JSONObject(info);
							String mimeType = Jo.getString("MIME");
							String url = Jo.getString("URL");
							String filename = url.substring(url
									.lastIndexOf("/") + 1);
							try {
								new FileDownLoad().executeOnExecutor(
										AsyncTask.THREAD_POOL_EXECUTOR, url,
										URLDecoder.decode(filename, "utf-8"),
										mimeType);
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					} else {
						String action = jo.getString("ACTION");
						jni_interface.TvSendCtrAction(action);
					}
				}
			}
		} catch (JSONException e) {
			Debug.d(TAG, "creat JSONObject error");
			e.printStackTrace();
		}

	}

	public Bundle getPlayInfo() {

		
		Bundle bundle = new Bundle();
		mCurPosition = MediaplayerBase.getInstance()
				.mediaPlayerPositionGet();
		Debug.e("MediaRendererDevice", "mCurPosition =" + mCurPosition);
		mRealTime = DesUtils.timeFormatToString(this.mCurPosition);
		mTotalDur = MediaplayerBase.getInstance()
				.mediaPlayerDurationGet();
		mTotalDuration = DesUtils
				.timeFormatToString(this.mTotalDur);
		bundle.putString("volume", mVolume);
		bundle.putString("mute", mMuteState);
		bundle.putString("state", mCurPlayState);
		bundle.putString("position", mRealTime);
		bundle.putString("totalTime", mTotalDuration);

		return bundle;

	}

	public boolean actionControlReceived(Action paramAction) {

		String serviceType = paramAction.getService().getServiceType();
		LetvLog.w("MediaRendererDevice", "service type =" + serviceType
				+ "action = " + paramAction.getName());
		if (serviceType
				.equals("urn:schemas-upnp-org:service:ConnectionManager:1")) {
			if (paramAction.getName().equals("GetProtocolInfo")) {
				paramAction.setArgumentValue("Source", "");
				paramAction
						.setArgumentValue(
								"Sink",
								"http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_MED;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_LRG;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_TS_HD_NA_ISO;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_TS_SD_NA_ISO;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG1;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mp4:DLNA.ORG_PN=AVC_MP4_MP_SD_AAC_MULT5;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mp4:DLNA.ORG_PN=AVC_MP4_MP_SD_AC3;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_TS_HD_NA;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=AVC_TS_MP_HD_AC3_T;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_MP3;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_BASE;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_BASE;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_FULL;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_FULL;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/3gpp:DLNA.ORG_PN=MPEG4_P2_3GPP_SP_L0B_AAC;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/3gpp:DLNA.ORG_PN=MPEG4_P2_3GPP_SP_L0B_AMR;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMABASE;DLNA.ORG_OP=01,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAFULL;DLNA.ORG_OP=01,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAPRO;DLNA.ORG_OP=01,http-get:*:audio/mp4:DLNA.ORG_PN=AAC_ISO_320;DLNA.ORG_OP=01,http-get:*:audio/3gpp:DLNA.ORG_PN=AAC_ISO_320;DLNA.ORG_OP=01,http-get:*:audio/mp4:DLNPN=AAC_ISO;DLNA.ORG_OP=01,http-get:*:audio/mp4:DLNA.ORG_PN=AAC_MULT5_ISO;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=44100;channels=2:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:image/jpeg:*,http-get:*:video/avi:*,http-get:*:video/divx:*,http-get:*:video/x-matroska:*,http-get:*:video/mpeg:*,http-get:*:video/mp4:*,http-get:*:video/x-ms-wmv:*,http-get:*:video/x-msvideo:*,http-get:*:video/x-flv:*,http-get:*:video/x-tivo-mpeg:*,http-get:*:video/quicktime:*,http-get:*:audio/mp4:*,http-get:*:audio/x-wav:*,http-get:*:audio/x-flac:*,http-get:*:application/ogg:*");

				return true;
			}
			if (paramAction.getName().equals("GetCurrentConnectionIDs")) {
				paramAction.setArgumentValue("ConnectionIDs", "0");

				return true;
			}
			if (paramAction.getName().equals("GetCurrentConnectionInfo")) {
				paramAction.setArgumentValue("RcsID", "0");
				paramAction.setArgumentValue("AVTransportID", "0");
				paramAction.setArgumentValue("ProtocolInfo", ":::");
				paramAction.setArgumentValue("PeerConnectionManager", "");
				paramAction.setArgumentValue("PeerConnectionID", "-1");
				paramAction.setArgumentValue("Direction", "Input");
				paramAction.setArgumentValue("Status", "Unknown");

				return true;
			}
		} else if (serviceType
				.equals("urn:schemas-upnp-org:service:AVTransport:1")) {
			if ("GetDeviceCapabilities".equals(paramAction.getName())) {
				paramAction.setArgumentValue("PlayMedia", "NETWORK");
				paramAction.setArgumentValue("RecMedia", "NOT_IMPLEMENTED");
				paramAction.setArgumentValue("RecQualityModes",
						"NOT_IMPLEMENTED");

				return true;
			}
			
			if (paramAction.getName().equals("SendMessage")) {
				String value = paramAction.getArgumentValue("Message");

				Debug.d("MediaRendererDevice", "SendMessage  = " + value);
				doActionReceived(value);
				return true;
			}
			if (paramAction.getName().equals("InstallApk")) {
				String filename = paramAction.getArgumentValue("filename");
				String filecontent = paramAction
						.getArgumentValue("filecontent");
				Debug.d("MediaRendererDevice", ">>>>>InstallApk " + filename);
				Intent InstalApkIntent = new Intent();
				InstalApkIntent.setAction("InstallApk");
				InstalApkIntent.putExtra("filename", filename);
				InstalApkIntent.putExtra("filecontent", filecontent);
				notifyDMR(InstalApkIntent);
				return true;
			}

			if (paramAction.getName().equals("GetTransportInfo")) {
				Debug.d("MediaRendererDevice",
						">>>>>GetTransportInfo,  mCurPlayState= "
								+ this.mCurPlayState);
				paramAction.setArgumentValue("CurrentTransportState",
						mCurPlayState);
				paramAction.setArgumentValue("CurrentTransportStatus", "OK");
				paramAction.setArgumentValue("CurrentSpeed", mCurPlaySpeed);

				return true;
			}
			if (paramAction.getName().equals("GetTransportSettings")) {
				paramAction.setArgumentValue("PlayMode", "NORMAL");
				paramAction.setArgumentValue("RecQualityMode", "0:BASIC");

				return true;
			}
			if (paramAction.getName().equals("GetMediaInfo")) {
				paramAction.setArgumentValue("NrTracks", "0");
				paramAction.setArgumentValue("MediaDuration", mTotalDuration);
				paramAction.setArgumentValue("CurrentURI", "");
				paramAction.setArgumentValue("CurrentURIMetaData", "");
				paramAction.setArgumentValue("NextURI", "");
				paramAction.setArgumentValue("NextURIMetaData", "");
				paramAction.setArgumentValue("PlayMedium", "UNKNOWN");
				paramAction.setArgumentValue("RecordMedium", "UNKNOWN");
				paramAction.setArgumentValue("WriteStatus", "UNKNOWN");

				return true;
			}
			if (paramAction.getName().equals("SetAVTransportURI")) {
				paramAction.getArgumentIntegerValue("InstanceID");
				this.CurrentURI = paramAction.getArgumentValue("CurrentURI");
				// paramAction.getArgument("CurrentURI").getRelatedStateVariable().setValue(this.CurrentURI);
				paramAction.setValue("CurrentURI", this.CurrentURI);
				Debug.d("MediaRendererDevice", "SetAVTransportURI CurrentURI="
						+ this.CurrentURI);
				String urlMetaData = paramAction
						.getArgumentValue("CurrentURIMetaData");
				paramAction.setValue("CurrentURIMetaData", urlMetaData);
				// paramAction.getArgument("CurrentURIMetaData").getRelatedStateVariable().setValue(urlMetaData);
				Debug.d("MediaRendererDevice",
						"SetAVTransportURI CurrentURIMetaData=" + urlMetaData);
				int isPlay = paramAction.getArgumentIntegerValue("IsPlay");
				this.netMediaType = null;
				mFileName = "";
				isUTP = false;
				mIosPlay = 0;
				mStartPostion = "0";
				/*
				 * if (urlMetaData != null && urlMetaData.equals("")) {
				 * setStateVariable(paramAction, "CurrentTrackDuration", "");
				 * return false; }
				 */
				if (urlMetaData != null) {
					this.netMediaType = getMediaTypeByMetadata(urlMetaData);
					Debug.d("MediaRendererDevice", "netMediaType = "
							+ this.netMediaType);
				}
				this.mVideoURL = this.CurrentURI;
				Debug.d("MediaRendererDevice", "isPlay=" + isPlay);
				if (isPlay == 1) {
					mIosPlay = 0;
					this.mCurPlayState = "PLAYING";
					this.playingMediaType = null;
					if (this.CurrentURI != null) {
						Debug.d("MediaRendererDevice", "######Play######");
						Debug.d("MediaRendererDevice", "CurrentURI="
								+ this.CurrentURI);
						this.playingMediaType = DesUtils
								.CheckMediaType(this.CurrentURI);
						if ((!this.playingMediaType.equals("audio/*"))
								&& (!this.playingMediaType.equals("image/*"))
								&& (!this.playingMediaType.equals("video/*"))) {
							if (this.netMediaType != null)
								this.playingMediaType = this.netMediaType;
						}

						if ((!this.playingMediaType.equals("audio/*"))
								&& (!this.playingMediaType.equals("image/*"))
								&& (!this.playingMediaType.equals("video/*"))) {
							this.playingMediaType = getFileType(this.CurrentURI);
							Debug.d("MediaRendererDevice",
									"playingMediaType2222="
											+ this.playingMediaType);
						}
						Debug.d("MediaRendererDevice", "playingMediaType="
								+ this.playingMediaType);

						if ("image/*".equals(this.playingMediaType)) {
							setStateVariable(paramAction, "TransportState",
									"PLAYING");
							Intent localIntent8 = new Intent(
									"com.letv.UPNP_PLAY_ACTION");
							localIntent8.putExtra("media_type",
									this.playingMediaType);
							localIntent8.putExtra("download_type", 1);
							localIntent8.putExtra("media_uri", this.CurrentURI);
							localIntent8.putExtra("file_name", this.mFileName);
							notifyDMR(localIntent8);
							Debug.d("MediaRendererDevice",
									"######sendBroadcast(UPNP_PLAY_ACTION: image)######");
							return true;
						}
						if ("video/*".equals(this.playingMediaType)) {
							setStateVariable(paramAction, "TransportState",
									"PLAYING");
							Intent localIntent7 = new Intent(
									"com.letv.UPNP_PLAY_ACTION");
							localIntent7.putExtra("media_type",
									this.playingMediaType);
							localIntent7.putExtra("media_uri", this.CurrentURI);
							localIntent7.putExtra("file_name", this.mFileName);
							notifyDMR(localIntent7);
							Debug.d("MediaRendererDevice",
									"######sendBroadcast(UPNP_PLAY_ACTION: video)######");
							setStateVariable(paramAction, "TransportState",
									"PLAYING");
							return true;
						}
						if ("audio/*".equals(this.playingMediaType)) {
							Intent localIntent6 = new Intent();
							localIntent6.setAction("com.letv.UPNP_PLAY_ACTION");
							localIntent6.putExtra("media_type",
									this.playingMediaType);
							localIntent6.putExtra("media_uri", this.CurrentURI);
							localIntent6.putExtra("file_name", this.mFileName);
							notifyDMR(localIntent6);
							Debug.d("MediaRendererDevice",
									"######sendBroadcast(UPNP_PLAY_ACTION:audio)######");
							setStateVariable(paramAction, "TransportState",
									"PLAYING");
							return true;
						}
					}
					return false;
				}
				return true;
			}
			if (paramAction.getName().equals("Play")) {

				Debug.d("MediaRendererDevice", " Play");
				String speed = paramAction.getArgumentValue("Speed");
				this.InstanceId = paramAction.getArgumentValue("InstanceID");
				// paramAction.getArgument("Speed").getRelatedStateVariable().setValue(speed);
				paramAction.setValue("Speed", speed);
				this.mCurPlayState = "PLAYING";
				this.playingMediaType = null;
				// this.mFileName = null;
				if (this.CurrentURI != null) {
					Debug.d("MediaRendererDevice", "######Play######");
					Debug.d("MediaRendererDevice", "CurrentURI="
							+ this.CurrentURI);
					this.playingMediaType = DesUtils
							.CheckMediaType(this.CurrentURI);

					if ((!this.playingMediaType.equals("audio/*"))
							&& (!this.playingMediaType.equals("image/*"))
							&& (!this.playingMediaType.equals("video/*"))) {
						if (this.netMediaType != null)
							this.playingMediaType = this.netMediaType;
					}

					if ((!this.playingMediaType.equals("audio/*"))
							&& (!this.playingMediaType.equals("image/*"))
							&& (!this.playingMediaType.equals("video/*"))) {
						this.playingMediaType = getFileType(this.CurrentURI);
						Debug.d("MediaRendererDevice", "playingMediaType2222="
								+ this.playingMediaType);
					}
					Debug.d("MediaRendererDevice", "playingMediaType="
							+ this.playingMediaType);

					if ("image/*".equals(this.playingMediaType)) {
						setStateVariable(paramAction, "TransportState",
								"PLAYING");
						Intent localIntent8 = new Intent(
								"com.letv.UPNP_PLAY_ACTION");
						localIntent8.putExtra("media_type",
								this.playingMediaType);
						localIntent8.putExtra("media_uri", this.CurrentURI);
						localIntent8.putExtra("file_name", this.mFileName);
						localIntent8.putExtra("download_type", 1);
						notifyDMR(localIntent8);
						Debug.d("MediaRendererDevice",
								"######sendBroadcast(UPNP_PLAY_ACTION: image)######");
						return true;
					}
					if ("video/*".equals(this.playingMediaType)) {
						setStateVariable(paramAction, "TransportState",
								"PLAYING");
						Intent localIntent7 = new Intent(
								"com.letv.UPNP_PLAY_ACTION");
						localIntent7.putExtra("media_type",
								this.playingMediaType);
						localIntent7.putExtra("media_uri", this.CurrentURI);
						localIntent7.putExtra("file_name", this.mFileName);
						localIntent7.putExtra("start_position", this.mStartPostion);
						notifyDMR(localIntent7);
						Debug.d("MediaRendererDevice",
								"######sendBroadcast(UPNP_PLAY_ACTION: video)######");
						setStateVariable(paramAction, "TransportState",
								"PLAYING");
						return true;
					}
					if ("audio/*".equals(this.playingMediaType)) {
						Intent localIntent6 = new Intent();
						localIntent6.setAction("com.letv.UPNP_PLAY_ACTION");
						localIntent6.putExtra("media_type",
								this.playingMediaType);
						localIntent6.putExtra("media_uri", this.CurrentURI);
						localIntent6.putExtra("file_name", this.mFileName);
						localIntent6.putExtra("start_position", this.mStartPostion);
						notifyDMR(localIntent6);
						Debug.d("MediaRendererDevice",
								"######sendBroadcast(UPNP_PLAY_ACTION:audio)######");
						setStateVariable(paramAction, "TransportState",
								"PLAYING");
						return true;
					}
				}
				Debug.d("MediaRendererDevice", "######Invalid Play######");
				return false;
			}
			if (paramAction.getName().equals("Stop")) {
				if (this.CurrentURI != null) {
					Debug.d("MediaRendererDevice", "######Stop ######");
					Debug.d("MediaRendererDevice", "playingMediaType="
							+ this.playingMediaType);
					this.mCurPlayState = "STOPPED";
					Intent localIntent5 = new Intent(
							"com.letv.UPNP_STOP_ACTION");
					localIntent5.putExtra("media_type", this.playingMediaType);
					notifyDMR(localIntent5);
					Debug.d("MediaRendererDevice",
							"######sendBroadcast(intent)######");
					this.mRealTime = "00:00:00";
					this.mTotalDuration = "00:00:00";
					mMuteState = "0";
					mCurPosition = 0;
					setStateVariable(paramAction, "TransportState", "STOPPED");
					return true;
				}
				Debug.d("MediaRendererDevice", "######Invalid Stop######");
				return false;
			}
			if (paramAction.getName().equals("Pause")) {
				this.mCurPlayState = "PLAYBACK_PAUSED";
				if (this.CurrentURI != null) {
					Debug.d("MediaRendererDevice", "######Pause ###### ");
					Intent localIntent4 = new Intent(
							"com.letv.UPNP_PAUSE_ACTION");
					localIntent4.putExtra("media_type", this.playingMediaType);
					notifyDMR(localIntent4);
					return true;
				}
				Debug.d("MediaRendererDevice", "######Invalid pause######");
				return false;
			}
			if (paramAction.getName().equals("GetPositionInfo")) {

				this.mCurPosition = MediaplayerBase.getInstance()
						.mediaPlayerPositionGet();
				Debug.e("MediaRendererDevice", "mCurPosition =" + mCurPosition);
				this.mRealTime = DesUtils.timeFormatToString(this.mCurPosition);
				Debug.d("MediaRendererDevice", "DMC mRealTime=" + mRealTime);
				int i = paramAction.getArgumentIntegerValue("InstanceID");
				Debug.d("MediaRendererDevice", "DMC says InstanceID=" + i);
				// paramAction.getArgument("Track").getRelatedStateVariable().getValue();
				// paramAction.getArgument("Track").setValue("1");
				paramAction.getValue("Track");
				paramAction.setArgumentValue("Track", "1");
				// String TrackDuration =
				// paramAction.getArgument("TrackDuration").getRelatedStateVariable().getValue();
				String TrackDuration = paramAction.getValue("TrackDuration");
				if ((TrackDuration != null) && (TrackDuration.equals(""))) {
					this.mTotalDur = MediaplayerBase.getInstance()
							.mediaPlayerDurationGet();
					this.mTotalDuration = DesUtils
							.timeFormatToString(this.mTotalDur);
					Debug.d("MediaRendererDevice", "DMC mTotalDuration="
							+ mTotalDuration);
					// paramAction.getArgument("TrackDuration").setValue(this.mTotalDuration);
					paramAction.setArgumentValue("TrackDuration",
							this.mTotalDuration);
				} else {
					Debug.d("MediaRendererDevice", "TrackDuration="
							+ TrackDuration);
					// paramAction.getArgument("TrackDuration").setValue(TrackDuration);
					paramAction
							.setArgumentValue("TrackDuration", TrackDuration);
				}
				String str4 = paramAction.getValue("TrackMetaData");
				if (str4 != null && str4.length() == 0) {
					str4 = "NOT_IMPLEMENTED";
				}
				paramAction.setArgumentValue("TrackMetaData", str4);
				paramAction.setArgumentValue("TrackURI", this.CurrentURI);
				paramAction.setArgumentValue("RelTime", mRealTime);
				paramAction.setArgumentValue("AbsTime", "00:00:00");
				paramAction.setArgumentValue("AbsCount", "2147483647");
				paramAction.setArgumentValue("RelCount", "2147483647");

				return true;
			}
			if ("Seek".equals(paramAction.getName())) {
				paramAction.getArgumentIntegerValue("InstanceID");
				paramAction.getArgumentValue("Unit");
				String rel_time = paramAction.getArgumentValue("Target");
				Debug.d("MediaRendererDevice", "######SEEK###### to time : "
						+ rel_time);
				Intent localIntent3 = new Intent("com.letv.UPNP_PLAY_SEEK");
				localIntent3.putExtra("media_type", this.playingMediaType);
				localIntent3.putExtra("REL_TIME", rel_time);
				notifyDMR(localIntent3);
			}
		} else if (serviceType
				.equals("urn:schemas-upnp-org:service:RenderingControl:1")) {
			if (paramAction.getName().equals("GetVolume")) {
				// paramAction.getArgument("CurrentVolume").setValue(JniInterface.getInstance().mediaPlayerVolumeGet());
				// paramAction.getArgument("CurrentVolume").setValue("50");
				paramAction.setArgumentValue("CurrentVolume", "50");
				return true;
			}
			if (paramAction.getName().equals("GetMute")) {
				/*
				 * boolean isMute =
				 * JniInterface.getInstance().mediaPlayerIsMute(); if(isMute ==
				 * true){ this.mMuteState = "1"; }else { this.mMuteState = "0";
				 * }
				 */
				paramAction.setArgumentValue("CurrentMute", mMuteState);
				// paramAction.getArgument("CurrentMute").setValue(this.mMuteState);
				return true;
			}
			if (paramAction.getName().equals("SetVolume")) {

				Debug.d("MediaRendererDevice", "######SetVolume ###### ");
				Intent localIntent2 = new Intent();
				localIntent2.setAction("com.letv.UPNP_SETVOLUME_ACTION");
				localIntent2.putExtra("DesiredVolume",
						paramAction.getArgumentIntegerValue("DesiredVolume"));
				localIntent2.putExtra("media_type", "audio/*");
				notifyDMR(localIntent2);
				return true;
			}
			if (paramAction.getName().equals("SetMute")) {
				Debug.d("MediaRendererDevice", "######SetMute ###### ");
				// this.mMuteState =
				// paramAction.getArgument("DesiredMute").getValue();
				this.mMuteState = paramAction.getValue("DesiredMute");
				Debug.d("MediaRendererDevice", "mMuteState = "
						+ this.mMuteState);
				Intent localIntent1 = new Intent();
				localIntent1.setAction("com.letv.UPNP_SETMUTE_ACTION");
				if (this.mMuteState.equals("0")) {
					localIntent1.putExtra("DesiredMute", false);
				} else {
					localIntent1.putExtra("DesiredMute", true);
				}
				localIntent1.putExtra("media_type", this.playingMediaType);
				notifyDMR(localIntent1);

				/*
				 * Debug.d("MediaRendererDevice", "######SetMute ###### ");
				 * this.mMuteState =
				 * paramAction.getArgument("DesiredMute").getValue();
				 * Debug.d("MediaRendererDevice", "mMuteState = " +
				 * this.mMuteState); boolean isMute = false; if
				 * (!this.mMuteState.equals("0")){ isMute = true; }
				 * 
				 * JniInterface.getInstance().mediaPlayerMuteSet(isMute);
				 */
				return true;
			}
		}
		return false;
	}

	class MediaStatusHandler extends Handler {
		public MediaStatusHandler() {

		}

		public MediaStatusHandler(Looper looper) {
			super(looper);

		}

		@Override
		public void handleMessage(Message msg) {
			Intent paramIntent = (Intent) msg.obj;
			String str = paramIntent.getAction();
			if (str.equals("com.letv.dlna.PLAY_PLAYING")) {
				Debug.d("MediaRendererDevice", ">>>>>>>>playing");
				mCurPlayState = "PLAYING";
				notifyChangeStatvar(
						"urn:schemas-upnp-org:service:AVTransport:1",
						"TransportState", "PLAYING");
				return;
			}
			if (str.equals("com.letv.dlna.PLAY_PAUSED")) {
				Debug.d("MediaRendererDevice", ">>>>>>>>pause");
				mCurPlayState = "PAUSED_PLAYBACK";
				notifyChangeStatvar(
						"urn:schemas-upnp-org:service:AVTransport:1",
						"TransportState", "PAUSED_PLAYBACK");
				return;
			}
			if (str.equals("com.letv.dlna.PLAY_STOPPED")) {
				Debug.d("MediaRendererDevice", ">>>>>>>>stop");
				mCurPlayState = "STOPPED";
				notifyChangeStatvar(
						"urn:schemas-upnp-org:service:AVTransport:1",
						"TransportState", "STOPPED");
				mRealTime = "00:00:00";
				mTotalDuration = "00:00:00";
				mMuteState = "0";
				mCurPosition = 0;
				notifyChangeStatvar(
						"urn:schemas-upnp-org:service:RenderingControl:1",
						"Mute", mMuteState);
				return;
			}
			if (str.equals("com.letv.dlna.PLAY_SETVOLUME")) {

				int i = paramIntent.getIntExtra("VOLUME", 50);
				Debug.d("MediaRendererDevice", ">>>>>>>>volume " + i);
				notifyChangeStatvar(
						"urn:schemas-upnp-org:service:RenderingControl:1",
						"Volume", "" + i);
				mVolume = "" + i;
				return;
			}
			if (str.equals("com.letv.dlna.PLAY_SETMUTE")) {

				int i = paramIntent.getIntExtra("MUTE", 0);

				Debug.d("MediaRendererDevice", ">>>>>>>>mute " + i);
				notifyChangeStatvar(
						"urn:schemas-upnp-org:service:RenderingControl:1",
						"Mute", "" + i);
				mMuteState = "" + i;
				return;
			}
			if (str.equals("com.letv.dlna.PLAY_SPEED")) {
				String str2 = paramIntent.getStringExtra("SPEED");
				Debug.d("MediaRendererDevice", ">>>>>>>>SPEED " + str2);
				mCurPlaySpeed = str2;
			}

		}
	}

	public void stopMediaStateThread() {
		if (mHandlerThread != null) {
			mHandlerThread.getLooper().quit();
		}
	}

	public void handleMessage(Intent paramIntent) {
		if (mMediaStatushandler != null) {
			Message message = Message.obtain();
			message.obj = paramIntent;
			mMediaStatushandler.sendMessage(message);
		}

	}

	public void notifyDMR(Intent paramIntent) {
	}

	public String getTransportState() {
		return mCurPlayState;
	}

	public boolean queryControlReceived(StateVariable paramStateVariable) {
		Debug.d("MediaRendererDevice", "######queryControlReceived");
		return false;
	}

	public void setStateVariable(String paramString1, String paramString2,
			String paramString3) {
		Debug.d("MediaRendererDevice", "######setStateVariable");
		if (getService(paramString1) == null
				|| getService(paramString1).getStateVariable(paramString2) == null
				|| getService(paramString1).getStateVariable("LastChange") == null)
			return;
		getService(paramString1).getStateVariable(paramString2).setValue(
				paramString3);
		if ("Volume".equals(paramString2)) {
			getService(paramString1).getStateVariable("LastChange").setValue(
					"<Event><InstanceID val=\"" + this.InstanceId + "\"><"
							+ paramString2 + " channel=\"Master\"  val=\""
							+ paramString3 + "\" /></InstanceID></Event>");
			return;
		}
		getService(paramString1).getStateVariable("LastChange").setValue(
				"<Event><InstanceID val=\"" + this.InstanceId + "\"><"
						+ paramString2 + " val=\"" + paramString3
						+ "\" /></InstanceID></Event>");
	}

	public void setStateVariable(Action paramAction, String paramString1,
			String paramString2) {
		StateVariable sv = paramAction.getService().getStateVariable(
				paramString1);
		if (sv != null)
			sv.setValue(paramString2);
	}
}
