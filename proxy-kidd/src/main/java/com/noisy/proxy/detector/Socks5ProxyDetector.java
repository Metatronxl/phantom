package com.noisy.proxy.detector;

import com.noisy.proxy.TaskScheduler;
import com.noisy.proxy.dao.ProxyInfoDao;
import com.noisy.proxy.dao.ProxyInfoDaoFilempl;
import com.noisy.proxy.entity.ProtocolType;
import com.noisy.proxy.entity.ProxyInfo;
import com.noisy.proxy.entity.ProxyType;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lei.x on 5/24/19.
 */

@Slf4j
public class Socks5ProxyDetector extends AbstractProxyDetector {


    private final static int THREAD_POOL_SIZE_CORES_MULTIPLE = 2;
    private final static int nThreads = THREAD_POOL_SIZE_CORES_MULTIPLE * Runtime.getRuntime().availableProcessors();

    private final ProxyInfoDao proxyInfoDao = new ProxyInfoDaoFilempl();
    private EventLoopGroup workerGroup;
    private Calendar startTime = Calendar.getInstance();
    private final AtomicLong taskCounter = new AtomicLong(0);
    private final AtomicLong totalTasks = new AtomicLong(0);
    private final AtomicLong proxyIPNum = new AtomicLong(0);
    private final int tcpConnLimits = 2000;
    private final Semaphore semaphore = new Semaphore(tcpConnLimits);
    private final Semaphore canStartNextSchSem = new Semaphore(1);
    private Bootstrap bootstrap;
    private String outputTmpFilePath;
    private File outputTmpFile;
    private TaskScheduler scheduler;
    private volatile boolean isReported;

    public Socks5ProxyDetector(TaskScheduler scheduler) {
        this.scheduler = scheduler;

        workerGroup = new NioEventLoopGroup(nThreads);

        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, false);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1024);
        bootstrap.option(ChannelOption.SO_RCVBUF, 4096);
        bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout());

        outputTmpFilePath = getOutputTmpFilePath(scheduler.getName());
        outputTmpFile = new File(outputTmpFilePath);
        if (!outputTmpFile.getParentFile().exists()) {
            outputTmpFile.getParentFile().mkdirs();
        }

        isReported = false;
    }

    public void reset() {
        try {
            canStartNextSchSem.acquire();
        } catch (InterruptedException e) {
            log.error("An exception occurred when resetting the Socks4 proxy detector, ex: {}", e);
        }
        startTime.setTimeInMillis(System.currentTimeMillis());
        totalTasks.set(taskCounter.get());
        proxyIPNum.set(0);
        outputTmpFilePath = getOutputTmpFilePath(scheduler.getName());
        outputTmpFile = new File(outputTmpFilePath);
        if (!outputTmpFile.getParentFile().exists()) {
            outputTmpFile.getParentFile().mkdirs();
        }
    }

    @Override
    public void detect(final String ip, final int port) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            log.warn("An exception occurred when creating a detection task, ex: {}", e);
        }

        if (Epoll.isAvailable()) {
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.config().setOption(EpollChannelOption.SO_REUSEPORT, true);
                    ChannelPipeline p = ch.pipeline();

                    p.addLast(new HttpClientCodec());
                    p.addLast(new HttpContentDecompressor());
                    p.addLast(new HttpObjectAggregator(10_485_760));
                    p.addLast(new WriteTimeoutHandler(getTimeout(), TimeUnit.MILLISECONDS));
                    p.addLast(new ReadTimeoutHandler(getTimeout(), TimeUnit.MILLISECONDS));
                    p.addLast(new HttpClientOutboundHandler());
                    p.addLast(new SocksProxyRespHandler());
                    p.addFirst(new Socks5ProxyHandler(new InetSocketAddress(ip, port)));
                }
            });
        } else {
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();

                    p.addLast(new HttpClientCodec());
                    p.addLast(new HttpContentDecompressor());
                    p.addLast(new HttpObjectAggregator(10_485_760));
                    p.addLast(new WriteTimeoutHandler(getTimeout(), TimeUnit.MILLISECONDS));
                    p.addLast(new ReadTimeoutHandler(getTimeout(), TimeUnit.MILLISECONDS));
                    p.addLast(new HttpClientOutboundHandler());
                    p.addLast(new SocksProxyRespHandler());
                    p.addFirst(new Socks5ProxyHandler(new InetSocketAddress(ip, port)));
                }
            });
        }

        // Connecting the proxy
        try {
            ChannelFuture f = bootstrap.connect(ip, port);
            f.channel().attr(AttributeKey.valueOf("ip")).set(ip);
            f.channel().attr(AttributeKey.valueOf("port")).set(port);

            long leftTasks = taskCounter.get();
            long tmpTotalTasks = totalTasks.incrementAndGet();
//            if (((tmpTotalTasks == scheduler.getTotalTasks()) && (leftTasks % 100) == 0)
//                    || ((tmpTotalTasks % 1000000) == 0)) {
//                log.info("Created tasks: {}, Left tasks: {}", tmpTotalTasks, leftTasks);
//            }
        } catch (Exception e) {
            log.error("An exception occurred when connecting to host {}:{}, ex: {}", ip, port, e);
        }
    }

    private synchronized void finish(ChannelHandlerContext ctx) {
        if (ctx.channel().hasAttr(AttributeKey.valueOf("finished"))) {
            return;
        }

        semaphore.release();
        ctx.channel().attr(AttributeKey.valueOf("finished")).set(true);

        long leftTasks = taskCounter.decrementAndGet();
        if (leftTasks < 1) {
            log.info("Total tasks: {}, Left tasks: {}", totalTasks, taskCounter);
            canStartNextSchSem.release();

            long consumedTime = System.currentTimeMillis() - startTime.getTimeInMillis();
            String distFilePath = retrieveOutputFilePath(outputTmpFilePath);
            try {
                if (outputTmpFile.exists())
                    FileUtils.moveFile(outputTmpFile, new File(distFilePath));
            } catch (IOException e) {
                log.error("Failed to move the temporary file {} to the dist file {}. Exception: {}",
                        outputTmpFile.getPath(), distFilePath, e);
            }
            log.info("Finished one scanning schedule, total tasks: {}, consumed time: {}",
                    totalTasks, consumedTime);
        }
    }

    private class SocksProxyRespHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnknownHostException {
            String responseText;
            if (msg instanceof FullHttpResponse) {
                FullHttpResponse httpResp = (FullHttpResponse) msg;


                ByteBuf content = httpResp.content();
                responseText = content.toString(Charset.forName("UTF-8"));
            } else {
                responseText = msg.toString();
            }
            String proxyIP = (String) ctx.channel().attr(AttributeKey.valueOf("ip")).get();

            ProxyType proxyType = checkProxyType(proxyIP, responseText.trim());
            if (proxyType != null) {
                ProxyInfo proxyInfo;
                Integer port = (Integer) ctx.channel().attr(AttributeKey.valueOf("port")).get();
                proxyInfo = new ProxyInfo(proxyIP,
                        ProtocolType.SOCKS_V5.getType(), port, proxyType.getType(), System.currentTimeMillis());
                proxyIPNum.incrementAndGet();
                log.info("FIND PROXY:" + proxyInfo.toString());
//                proxyInfo.setLocation(IPLocationUtils.getLocation(proxyInfo.getIp()));
                proxyInfoDao.append(proxyInfo, outputTmpFile);
            }

            finish(ctx);
            ctx.channel().close();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // Send HTTP request
            URI uri = null;
            try {
                uri = new URI(getProxyCheckerURL());
            } catch (URISyntaxException e) {
                log.error(e.getMessage());
            }
            int port = uri.getPort();
            String host = uri.getHost() + ":" + String.valueOf(port);

            HttpRequest request = new DefaultHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
            request.headers().set(HttpHeaderNames.HOST, host);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            request.headers().set(HttpHeaderNames.PROXY_CONNECTION, HttpHeaderValues.KEEP_ALIVE);

            ctx.channel().writeAndFlush(request);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            finish(ctx);
            ctx.fireChannelInactive();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.channel().close();
            finish(ctx);
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            taskCounter.incrementAndGet();
            ctx.fireChannelRegistered();
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            finish(ctx);
            ctx.fireChannelUnregistered();
        }
    }

    private class HttpClientOutboundHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            finish(ctx);
            ctx.close(promise);
        }
    }
}
