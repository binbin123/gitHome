package com.letv.accountLogin.netty;

import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import com.letv.dmr.DmrInterfaceManage;
import com.letv.upnpControl.dlna.jni_interface;
import com.letv.upnpControl.tools.Engine;
import com.letv.upnpControl.tools.LetvLog;
import android.content.Context;

public class ImageReceiver {
	private static ClientBootstrap bootstrap = null;
	private static ChannelFuture future = null;
	private static Context mContext;
	private static String fileName;
	public static String url;
	public static final String TAG = ImageReceiver.class.getSimpleName();
	
	public static void sendOnline(String from, Context context,String ip) {
		LetvLog.d(TAG, "--send image online--");
		mContext = context;
	
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = pipeline();

				pipeline.addLast("decoder", new HttpResponseDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
				pipeline.addLast("handler", new FileRecieveHandler());

				return pipeline;
			}
		});
		bootstrap.setOption("receiveBufferSize", 1048576);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		future = bootstrap.connect(new InetSocketAddress(ip,
				8080));
		// Wait until the connection is made successfully.
		future.awaitUninterruptibly(3000);
		if (future.isSuccess()) {
			LetvLog.d(TAG, "--send online success--");
			 future.awaitUninterruptibly().getChannel();
		} else {
			LetvLog.d(TAG, "--send online failed--");
			future = bootstrap.connect(new InetSocketAddress(ip,
					8080));
			// Wait until the connection is made successfully.
			future.awaitUninterruptibly(3000);
			if(future.isSuccess()){
				future.awaitUninterruptibly().getChannel();
			}else{
				bootstrap.releaseExternalResources();
				return;
			}
		}
	
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);

		// 先要发送一个文件服务上线通知
		response.addHeader("online", "true");
		response.addHeader("from", from);

		setContentLength(response, 0);
		future.getChannel().write(response);
	}

	static class FileRecieveHandler extends SimpleChannelUpstreamHandler {

		private volatile boolean readingChunks;
		private FileOutputStream fOutputStream = null;

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			/*
			 * 按照channle的顺序进行处理
			 * server先发送HttpResponse过来，所以这里先对HttpResponse进行处理，进行文件判断之类
			 * 之后，server发送的都是ChunkedFile了。
			 */

			if (e.getMessage() instanceof HttpResponse) {
				DefaultHttpResponse httpResponse = (DefaultHttpResponse) e
						.getMessage();
				fileName = httpResponse.getHeader("fileName");
				readingChunks = httpResponse.isChunked();
				DmrInterfaceManage.getInstance().setAccountLoginReceivingPicture();
				LetvLog.d(TAG, "--received file success--");
				
			} else {
				HttpChunk httpChunk = (HttpChunk) e.getMessage();
				if (!httpChunk.isLast()) {
					ChannelBuffer buffer = httpChunk.getContent();
					
						if (fOutputStream == null) {
							url = Engine.getInstance().getFilePath() + fileName;
							LetvLog.d(TAG, "--file URL =" + url + "--");
							try{
							fOutputStream = mContext.openFileOutput(
									fileName, Context.MODE_PRIVATE);
							}catch(FileNotFoundException e1){
								e1.printStackTrace();
							}
						}
						while (buffer.readable()) {
							byte[] dst = new byte[buffer.readableBytes()];
							buffer.readBytes(dst);
							fOutputStream.write(dst);
						}
				} else {
					readingChunks = false;
				}
				fOutputStream.flush();
				
			}
			if (!readingChunks) {
				fOutputStream.close();
				fOutputStream = null;
				LetvLog.d(TAG, "--send to dmr --");
				if(jni_interface.TvGetUpnpDeviceStatus() == false){
					File file = new File(url);  
					file.delete();
					return;
				}
				DmrInterfaceManage.getInstance().setUrl(url, 1,0);
			}
		}
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			LetvLog.d(TAG, "Exception occure--"+e.getCause());
			System.err.print(e.getCause());
		}
	}
}
