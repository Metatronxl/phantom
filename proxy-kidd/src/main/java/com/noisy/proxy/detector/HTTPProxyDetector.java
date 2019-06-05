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
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lei.x on 5/24/19.
 */
public class HTTPProxyDetector extends AbstractProxyDetector {
    private static final Logger log = LoggerFactory.getLogger(HTTPProxyDetector.class);
    //    线程池核心数 Config the thread pool size by the multiple of the CPU cores, must small than 20
    private final static int THREAD_POOL_SIZE_CORES_MULTIPLE = 2;
    private final static int nThreads = THREAD_POOL_SIZE_CORES_MULTIPLE
            * Runtime.getRuntime().availableProcessors();

    private final ProxyInfoDao proxyInfoDao = new ProxyInfoDaoFilempl();

    private EventLoopGroup workerGroup;
    private Calendar startTime = Calendar.getInstance();
    private final AtomicLong taskCounter = new AtomicLong(0);
    private final AtomicLong totalTasks = new AtomicLong(0);
    private final AtomicLong proxyIPNum = new AtomicLong(0);
    // The TCP connection limits of the system, must small than 500000
    private final int tcpConnLimits = 2000;
    private final Semaphore semaphore = new Semaphore(tcpConnLimits);
    private final Semaphore canStartNextSchSem = new Semaphore(1);
    private Bootstrap bootstrap;
    private String outputTmpFilePath;
    private File outputTmpFile;
    private TaskScheduler scheduler;

    public HTTPProxyDetector(TaskScheduler scheduler) {
        this.scheduler = scheduler;
        workerGroup = new NioEventLoopGroup(nThreads);

        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.AUTO_CLOSE, true);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, false);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1024);
        bootstrap.option(ChannelOption.SO_RCVBUF, 4096);
        bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout());


        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            public void initChannel(NioSocketChannel ch) throws Exception {
                // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                ch.pipeline().addLast(new HttpClientCodec());
                ch.pipeline().addLast(new ReadTimeoutHandler(getTimeout(), TimeUnit.MILLISECONDS));

                ch.pipeline().addLast(new HttpClientOutboundHandler());
                ch.pipeline().addLast(new WriteTimeoutHandler(getTimeout(), TimeUnit.MILLISECONDS));
                // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
//                ch.pipeline().addLast(new HttpRequestEncoder());

                ch.pipeline().addLast(new HTTPProxyRespHandler());
            }
        });

        outputTmpFilePath = getOutputTmpFilePath(scheduler.getName());
        outputTmpFile = new File(outputTmpFilePath);
        if (!outputTmpFile.getParentFile().exists()) {
            outputTmpFile.getParentFile().mkdirs();
        }
    }

    public void reset() {
        try {
            canStartNextSchSem.acquire();
        } catch (InterruptedException e) {
            log.error("An exception occurred when resetting the HTTP proxy detector, ex: {}", e);
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
    public void detect(String ip, int port) {
        try {
            semaphore.acquire();
            log.info("semaphore acquire:{}", semaphore.toString());
        } catch (InterruptedException e) {
            log.warn("An exception occurred when creating a detection task, ex: {}", e);
        }

        // Start the client.
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

    private synchronized void finish(ChannelHandlerContext ctx) throws InterruptedException {
        // 避免重复调用
        if (ctx.channel().hasAttr(AttributeKey.valueOf("finished"))) {
            return;
        }

        semaphore.release();
        log.info("semaphore release:{}", semaphore.toString());
        ctx.channel().attr(AttributeKey.valueOf("finished")).set(true);

        long leftTasks = taskCounter.decrementAndGet();
        if (leftTasks < 1) {
            log.info("Total tasks: {}, Left tasks: {}", totalTasks, taskCounter);
            canStartNextSchSem.release();

            long consumedTime = System.currentTimeMillis() - startTime.getTimeInMillis();
//            EmailReporter.getInstance().report(scheduler.getScanTarget(),
//                    startTime.getTime(), totalTasks.get() / scheduler.getScanPortNum(),
//                    scheduler.getScanPortNum(), proxyIPNum.get(), consumedTime);

            log.info("Finished one scanning schedule, total tasks: {}, consumed time: {},file address:{}",
                    totalTasks, consumedTime, outputTmpFile.getPath());
        }
    }

    private class HTTPProxyRespHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String responseText;
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;
                ByteBuf buf = content.content();
                responseText = buf.toString(io.netty.util.CharsetUtil.UTF_8);
                buf.release();

                String proxyIP = (String) ctx.channel().attr(AttributeKey.valueOf("ip")).get();
                System.out.println(responseText.trim());
                ProxyType proxyType = checkProxyType(proxyIP, responseText.trim());
                if (proxyType != null) {
                    ProxyInfo proxyInfo;
                    Integer port = (Integer) ctx.channel().attr(AttributeKey.valueOf("port")).get();
                    proxyInfo = new ProxyInfo(proxyIP,
                            ProtocolType.HTTP.getType(), port, proxyType.getType(), System.currentTimeMillis());
                    log.info("FIND PROXY:" + proxyInfo.toString());
                    proxyIPNum.incrementAndGet();
                    //TODO 添加GEO数据库
//                    proxyInfo.setLocation(IPLocationUtils.getLocation(proxyInfo.getIp()));
                    proxyInfoDao.append(proxyInfo, outputTmpFile);
                }
            }

            finish(ctx);
            ctx.channel().close();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // Send HTTP request
            URI uri = new URI(getProxyCheckerURL());
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
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            finish(ctx);
            ctx.channel().close();
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
