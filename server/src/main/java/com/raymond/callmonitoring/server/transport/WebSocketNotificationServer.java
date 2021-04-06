package com.raymond.callmonitoring.server.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketNotificationServer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationServer.class);
    public static final int DEFAULT_SO_SNDBUF = 8192;
    public static final int DEFAULT_SO_RCVBUF = 8192;

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_REUSEADDR,true)
                .childOption(ChannelOption.TCP_NODELAY,true)
                .childOption(ChannelOption.SO_RCVBUF,DEFAULT_SO_RCVBUF)
                .childOption(ChannelOption.SO_SNDBUF,DEFAULT_SO_SNDBUF)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new WebSocketServerInitializer());

        try {
            logger.info("start.....");
            ChannelFuture future = bootstrap.bind(8080).sync();
            future.channel().closeFuture().sync();
            logger.info("finished to start....");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
