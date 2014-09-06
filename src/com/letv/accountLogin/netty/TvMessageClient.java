package com.letv.accountLogin.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import com.letv.accountLogin.protobuf.ControlDataProto.Parcel;
import com.letv.upnpControl.tools.Constants;
import com.letv.upnpControl.tools.Engine;
import com.letv.upnpControl.tools.LetvLog;

/**
 * 
 * @author Liang Tian <tianliang@letv.com> Li Pengfei<lipengfei@letv.com>
 */
public class TvMessageClient {
	private static final String TAG = TvMessageClient.class.getCanonicalName();
	private static Context mContext = null;
	private static ClientBootstrap bootstrap;
	private static ChannelFactory factory;
	private static String mSignature;
	private static int count;
	private static String host;
	private static int port;
	private static String mFrom;
	private static String mToken;
	private static String mMsgInfo;
	public static boolean stopPing = false;
	private static PostmanClientHandler handler;
	private static HandlerThread mThread;
	private static final int MSG_ONLINE = 1000;
	private static final int MSG_OFFLINE = 2000;
	private static final int MSG_SEND_DATA = 3000;
	private static final int MSG_LOOP_SEND_PING = 4000;
	private static HandlerThread mStatusThread;
	private static StatusHandler mStatusHandler;
	
	static class StatusHandler extends Handler {
		public StatusHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ONLINE:
				synchronized (this) {
					stopPing = false;
					LetvLog.d(TAG, "--netty send online--");
					handler.onLine();
					break;
				}
			case MSG_OFFLINE:
				synchronized (this) {
					if (stopPing)
						break;
					stopPing = true;
					LetvLog.d(TAG, "--netty send offline--");
					break;
				}
			case MSG_SEND_DATA:
				synchronized (this) {
					LetvLog.d(TAG, "--netty send data--");
					Parcel.Builder builder = Parcel.newBuilder();
					builder.setType(Parcel.DataType.KEY_STRIKE);
					builder.setFrom(mFrom);
					builder.setTo(Engine.getTo());
					builder.setMsgInfo(mMsgInfo);
					builder.setToken(mToken);
					LetvLog.d(TAG, "To phone " + Engine.getTo());
					handler.sendParcel(builder.build());
					break;
				}
			}
		}
	}

	private static NettyHandler mHandler;

	static class NettyHandler extends Handler {
		public NettyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOOP_SEND_PING:
				while (true) {
					LetvLog.d(TAG, "In send PING loop!");
					if (stopPing) {
						LetvLog.d(TAG, "stop send PING!");
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException iee) {
						iee.printStackTrace();
					}
					handler.ping();
					try {
						/*
						 * <libao fix bug for :client connect server abormally
						 * probability begin
						 */
						Thread.sleep(120000L);
						/* libao end > */
					} catch (InterruptedException iee) {
						iee.printStackTrace();
					}
					LetvLog.d(TAG, "After send PING delay!");
				}
				break;
			}
		}
	}

	private static ChannelFuture connectFuture;
	private static Channel channel;

	public static void sendDataByNetty(int dataType, final String from,
			String token, String msgInfo, Context context, String signature) {
		LetvLog.d(TAG, "Send netty started dataType =" + dataType + "from = "
				+ from + "token=" + token + "msgInfo = " + msgInfo
				+ "signature=" + signature);
		host = SystemProperties.get("persist.letv.tsiUrl");
		port = 80;
		mContext = context;
		mSignature = signature;
		mToken = token;
		mMsgInfo = msgInfo;
		mFrom = from;
		if (mThread == null) {
			mThread = new HandlerThread("NettyHandler");
			mThread.start();
		}
		if (mHandler == null) {
			mHandler = new NettyHandler(mThread.getLooper());
		}
		if (mStatusThread == null) {
			mStatusThread = new HandlerThread("StatusHandler");
			mStatusThread.start();
		}
		if (mStatusHandler == null) {
			mStatusHandler = new StatusHandler(mStatusThread.getLooper());
		}
		if (dataType == Constants.ONLINE_DATA_TYPE) {
			LetvLog.d(TAG, "build connect!");
			if (factory != null) {
				factory.shutdown();
				factory = null;
			}
			factory = new NioClientSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool());
			if (bootstrap != null) {
				bootstrap.shutdown();
				bootstrap = null;
			}
			bootstrap = new ClientBootstrap(factory);
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() {
					return Channels.pipeline(
							new ProtobufVarint32FrameDecoder(),
							new ProtobufDecoder(Parcel.getDefaultInstance()),
							new ProtobufVarint32LengthFieldPrepender(),
							new ProtobufEncoder(), new PostmanClientHandler(
									from), new EventClientHandler());
				}
			});
			bootstrap.setOption("tcpNoDelay", true);
			bootstrap.setOption("keepAlive", true);
			if (connectFuture != null) {
				connectFuture.cancel();
				connectFuture = null;
			}
			LetvLog.e(TAG, "build connect!");
			if (host == null || host.length() == 0 || host.equals("")) {
				LetvLog.d(TAG, "use host connect for host is not null!");
				connectFuture = bootstrap.connect(new InetSocketAddress(
						Constants.DEFAULT_HOST, port));
			} else {
				LetvLog.d(TAG, "use default connect for host is null!");
				connectFuture = bootstrap.connect(new InetSocketAddress(host,
						port));
			}
			LetvLog.d(TAG, "after connect !");
			connectFuture.awaitUninterruptibly(3000);
			if (connectFuture.isSuccess()) {
				LetvLog.d(TAG, "connect build sucdessful!");
				channel = connectFuture.awaitUninterruptibly().getChannel();
			} else {
				LetvLog.d(TAG, "can't build connect! try once again!");
				if (host == null || host.length() == 0 || host.equals("")) {
					connectFuture = bootstrap.connect(new InetSocketAddress(
							Constants.DEFAULT_HOST, port));
				} else {
					connectFuture = bootstrap.connect(new InetSocketAddress(
							host, port));
				}
				connectFuture.awaitUninterruptibly(3000);
				if (connectFuture.isSuccess()) {
					channel = connectFuture.awaitUninterruptibly().getChannel();
				} else {
					LetvLog.d(TAG,
							"build connect failed try to release the resource!");
					bootstrap.releaseExternalResources();
					return;
				}
			}

		}

		// 给手机发送状态消息时走此通道

		if (factory == null) {
			LetvLog.d(TAG, "---factory=null---");
			factory = new NioClientSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool());
		}
		if (bootstrap == null) {
			LetvLog.d(TAG, "---bootstrap=null---");
			bootstrap = new ClientBootstrap(factory);
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() {
					return Channels.pipeline(
							new ProtobufVarint32FrameDecoder(),
							new ProtobufDecoder(Parcel.getDefaultInstance()),
							new ProtobufVarint32LengthFieldPrepender(),
							new ProtobufEncoder(), new PostmanClientHandler(
									from), new EventClientHandler());
				}
			});
			bootstrap.setOption("tcpNoDelay", true);
			bootstrap.setOption("keepAlive", true);
		}
		// Make a new connection.
		if (connectFuture == null) {
			LetvLog.d(TAG, "---connectFuture=null---");
			if (host == null || host.length() == 0 || host.equals("")) {
				connectFuture = bootstrap.connect(new InetSocketAddress(
						Constants.DEFAULT_HOST, port));
				// connectFuture = bootstrap.connect(new InetSocketAddress(
				// "220.181.153.205", port));
			} else {
				connectFuture = bootstrap.connect(new InetSocketAddress(host,
						port));
			}
		}
		// Wait until the connection is made successfully.
		if (channel == null) {
			LetvLog.d(TAG, "---channel=null---");
			channel = connectFuture.awaitUninterruptibly().getChannel();
		}
		// Get the handler instance to initiate the request.
		handler = channel.getPipeline().get(PostmanClientHandler.class);

		switch (dataType) {
		case Constants.ONLINE_DATA_TYPE:
			mStatusHandler.sendEmptyMessage(MSG_ONLINE);
			break;
		case Constants.OFFLINE_DATA_TYPE:
			mStatusHandler.sendEmptyMessage(MSG_OFFLINE);
			break;
		case Constants.SEND_DATA_TYPE:
			mStatusHandler.sendEmptyMessage(MSG_SEND_DATA);
			break;
		}
	}

	static class PostmanClientHandler extends SimpleChannelUpstreamHandler {

		private final BlockingQueue<Parcel> answer = new LinkedBlockingQueue<Parcel>();
		private volatile Channel channel;
		private String clientId;

		public PostmanClientHandler(String clientId) {
			this.clientId = clientId;
		}

		public void sendParcel(Parcel parcel) {
			channel.write(parcel);
		}

		public void ping() {
			count++;
			LetvLog.d(TAG, "Send PING " + count + " times!");
			Parcel.Builder builder = Parcel.newBuilder();
			builder.setType(Parcel.DataType.PING);
			builder.setFrom(clientId);
			/*
			 * <libao fix bug for :client connect server abormally probability
			 * begin
			 */
			ChannelFuture cf = channel.write(builder.build());
			try {
				cf.sync();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (cf.isSuccess())
				LetvLog.e(TAG, "#####ping success");
			else
				LetvLog.e(TAG, "#####ping fail");
			/* libao end > */

		}

		public Parcel onLine() {
			LetvLog.d(TAG, "Send online data!");
			Parcel.Builder builder = Parcel.newBuilder();
			builder.setType(Parcel.DataType.ONLINE);
			builder.setFrom(clientId);
			builder.setToken(mToken);
			builder.setMsgInfo(mMsgInfo);
			builder.setSignature(mSignature);
			channel.write(builder.build());

			Parcel result = null;
			/*
			 * <libao fix bug for :client connect server abormally probability
			 * begin
			 */
			/*
			 * boolean interrupted = false; for (;;) { try { result =
			 * answer.take(); break; } catch (InterruptedException e) {
			 * interrupted = true; } } if (interrupted) {
			 * Thread.currentThread().interrupt(); }
			 */
			/* libao end > */
			return result;
		}

		public Parcel offline() {
			Parcel.Builder builder = Parcel.newBuilder();
			builder.setType(Parcel.DataType.OFFLINE);
			builder.setFrom(clientId);
			builder.setSignature(mSignature);
			channel.write(builder.build());

			Parcel result = null;
			boolean interrupted = false;
			try {
				result = answer.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
			return result;
		}

		@Override
		public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
				throws Exception {
			if (e instanceof ChannelStateEvent) {
				System.out.println(e.toString());
			}
			super.handleUpstream(ctx, e);
		}

		@Override
		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
				throws Exception {
			channel = e.getChannel();
			super.channelOpen(ctx, e);
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
			Parcel parcel = (Parcel) e.getMessage();
			LetvLog.e(TAG, "###############messageReceived");
			switch (parcel.getType()) {
			case ONLINE_ACK:
				LetvLog.d(TAG, "Received online ack!");
				if (parcel.getErrorCode() != null
						&& !"".equals(parcel.getErrorCode())) {
					LetvLog.d(TAG, "ErrorCode--" + parcel.getErrorCode()
							+ "--");
					if (parcel.getMsgInfo() != null
							&& !"".equals(parcel.getMsgInfo())) {
						LetvLog.d(TAG, "MsgInfo--" + parcel.getMsgInfo()
								+ "--");
					}
					Parcel p = onLine();
					while (p.getErrorCode() != null
							&& !"".equals(p.getErrorCode())) {
						try {
							Thread.sleep(1000);
						} catch (Exception e1) {
						}
						p = onLine();
					}
				}
				boolean offered = answer.offer(parcel);
				assert offered;
				mHandler.sendEmptyMessage(MSG_LOOP_SEND_PING);
				break;
			case KEY_STRIKE:
				LetvLog.d(TAG, "Received KEY_STRIKE message!");
				OperationCtrlData.operate(mContext, parcel);
				break;
			case OFFLINE_ACK:
				if (parcel.getErrorCode() == null) {
					if (mHandler != null) {
						mHandler.removeMessages(MSG_LOOP_SEND_PING);
						mHandler = null;
					}
					if (mStatusHandler != null) {
						mStatusHandler.removeMessages(MSG_ONLINE);
						mStatusHandler.removeMessages(MSG_OFFLINE);
						mStatusHandler.removeMessages(MSG_SEND_DATA);
						mStatusHandler = null;
					}
					if (mThread != null) {
						mThread.getLooper().quit();
						try {
							mThread.join(500);
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
						mThread = null;
					}
					if (mStatusThread != null) {
						LetvLog.d(TAG, "mStatusThread quit");
						mStatusThread.getLooper().quit();
						try {
							mStatusThread.join(500);
						} catch (InterruptedException se) {
							se.printStackTrace();
						}
						mStatusThread = null;
					}
				}
				break;
			default:
				ctx.sendUpstream(e);
				break;
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			LetvLog.d(TAG, "NettyException" + e.getCause());
			e.getCause().printStackTrace();
			e.getChannel().close();
		}
	}

	static class EventClientHandler extends SimpleChannelUpstreamHandler {

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
			Parcel parcel = (Parcel) e.getMessage();
			if (parcel.getType().equals(Parcel.DataType.MSG_INFO)) {
				System.out.println("" + System.currentTimeMillis() + " "
						+ parcel.getType() + "=>" + parcel.getMsgInfo());
			} else {
				System.out.println("" + System.currentTimeMillis() + " "
						+ parcel.getType() + "=>" + parcel.getKeyCode());
			}

		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			System.err.println("!!!!Exception" + e.getCause());
			e.getCause().printStackTrace();
			e.getChannel().close();
		}
	}
}
