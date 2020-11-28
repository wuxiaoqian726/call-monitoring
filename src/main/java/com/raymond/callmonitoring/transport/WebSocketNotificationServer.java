package com.raymond.callmonitoring.transport;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import com.raymond.callmonitoring.AkkaActorSystem;
import com.raymond.callmonitoring.actor.CallSubscriptionActor;
import com.raymond.callmonitoring.model.CallSubscription;
import com.raymond.callmonitoring.utils.NettyDirectMemReporter;
import com.raymond.callmonitoring.utils.Utils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WebSocketNotificationServer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationServer.class);
    private static ByteBuf delimiter = Unpooled.copiedBuffer("\t".getBytes());
    private static NettyDirectMemReporter reporter = new NettyDirectMemReporter();

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new WebSocketServerInitializer());

        try {
            logger.info("start.....");
            ChannelFuture future = bootstrap.bind(8080).sync();
            reporter.startReport();
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
