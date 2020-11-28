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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NotificationServer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServer.class);
    private static ByteBuf delimiter = Unpooled.copiedBuffer("\t".getBytes());
    private static NettyDirectMemReporter reporter = new NettyDirectMemReporter();

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new CallMonitoringChannelInitializer());

        try {
            logger.info("start.....");
            ChannelFuture future = bootstrap.bind(8899).sync();
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

    private static class CallMonitoringChannelInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(100, delimiter));
            ch.pipeline().addLast(new StringDecoder());
            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                    logger.info("channel active");
                    //TODO: do validation based on JWT?
                }

                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    Long queueId = Long.valueOf(msg.toString());
                    logger.info("connection created for userId:{}", queueId);
                    if (queueId == null) {
                        //TODO: double check
                        ctx.channel().close();
                        return;
                    }

                    ActorSystem actorSystem = AkkaActorSystem.getInstance().getActorSystem();
                    List<Long> queueIds = new ArrayList<>();
                    queueIds.add(queueId);
                    CallSubscription callSubscription = new CallSubscription();
                    callSubscription.setQueueIdList(queueIds);
                    String uniqueChannelId = Utils.getUniqueChannelId(ctx.channel());
                    actorSystem.actorOf(Props.create(CallSubscriptionActor.class, ctx.channel(), callSubscription), uniqueChannelId);
                }


                @Override
                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                    ActorSystem actorSystem = AkkaActorSystem.getInstance().getActorSystem();
                    actorSystem.actorSelection(Utils.getActorPath(Utils.getUniqueChannelId(ctx.channel()))).tell(PoisonPill.getInstance(), ActorRef.noSender());
                    super.channelInactive(ctx);
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    super.exceptionCaught(ctx, cause);
                }
            });
        }
    }
}
