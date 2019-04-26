package com.maxent.proxy;

import com.maxent.proxy.detector.ProtocolType;
import com.maxent.proxy.detector.ProxyDetector;
import com.maxent.proxy.detector.ProxyDetectorFactory;
import com.maxent.proxy.detector.ProxyInfo;
import com.maxent.proxy.scanner.ProxyScanner;
import com.maxent.proxy.task.TaskScheduler;
import com.maxent.proxy.util.IPFilterUtils;
import com.maxent.proxy.util.IPPoolUtils;
import com.maxent.proxy.util.IPSegment;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Proxy scanner application!
 */
public class App {

    public static void launchProxyScannner(){
        ProxyScanner.start();
    }




    public static void main(String[] args) {
        ProxyScanner.start();

//        TaskScheduler scheduler = new TaskScheduler("InlandSocksProxyScheduler", "Socks代理验证",
//                ProtocolType.HTTP, new HashMap<>());
//        ProxyDetector proxyDetector = ProxyDetectorFactory.createProxyDetector(ProtocolType.HTTP, scheduler);
//        proxyDetector.reset();
//        proxyDetector.detect("121.33.226.167", 3128);

//        Set<String> ipSegments = read("data/InlandIPSegments.txt");
//        Set<String> ips = read("data/test.txt");
//
//        int count = 0;
//        List<IPSegment> ipSegmentList = IPPoolUtils.getIPSegments(ipSegments);
//        while (!ipSegmentList.isEmpty()) {
//            Iterator<IPSegment> iterator = ipSegmentList.iterator();
//            while (iterator.hasNext()) {
//                IPSegment ipSegment = iterator.next();
//                if (ipSegment.hasNextIP()) {
//                    String proxyIP = ipSegment.getNextStringIP();
//                    if (!IPFilterUtils.getInstance().needFilter(proxyIP)) {
//                        if (ips.contains(proxyIP)) {
//                            System.out.println(proxyIP);
//                            count++;
//                        }
//                    }
//                } else {
//                    iterator.remove();
//                }
//            }
//        }
//
//        System.out.println("The number of the IPs has been included in our IP segments: " + count);

//        App app = new App();
//        try {
//            app.testProxy();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    public void testProxy() throws Exception {
        final String host = "42.159.247.105";
        final int port = 80;

        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();

                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpContentDecompressor());
                        p.addLast(new HttpObjectAggregator(10_485_760));
                        p.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(final ChannelHandlerContext ctx) throws Exception {
                                HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/client_ip.php");
                                request.headers().set("Host", host + ":" + port);

                                ctx.writeAndFlush(request);

                                System.out.println("!sent");
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println("!answer");
                                if (msg instanceof FullHttpResponse) {
                                    FullHttpResponse httpResp = (FullHttpResponse) msg;


                                    ByteBuf content = httpResp.content();
                                    String strContent = content.toString(Charset.forName("UTF-8"));
                                    System.out.println("body: " + strContent);

                                    return;
                                }

                                super.channelRead(ctx, msg);
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace(System.err);
                                ctx.close();
                            }
                        });

                        p.addFirst(new Socks5ProxyHandler(new InetSocketAddress("182.254.226.161", 1080)));
                    }
                });

        b.connect(host, port).awaitUninterruptibly();
        System.out.println("!connected");

    }

    public static Set<String> read(String ipSegFilePath) {
        Set<String> ipSegments = new HashSet<>();
        File file = FileUtils.getFile(ipSegFilePath);
        if (file == null) {
            return null;
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            List<String> ipSegList = IOUtils.readLines(br);
            if (ipSegList != null && !ipSegList.isEmpty()) {
                for (String ipSeg : ipSegList) {
                    ipSegments.add(ipSeg);
                }
                br.close();
            } else {
                br.close();
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        return ipSegments;
    }
}
