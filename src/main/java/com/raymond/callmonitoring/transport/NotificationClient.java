package com.raymond.callmonitoring.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationClient {
    private static final Logger logger = LoggerFactory.getLogger(NotificationClient.class);
    private final String queueId;

    public NotificationClient(String queueId) {
        this.queueId = queueId;
    }

    public void start() {
        ByteBuf delimiter = Unpooled.copiedBuffer("\t".getBytes());
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(800, delimiter));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ByteBuf message = Unpooled.buffer();
                                message.writeBytes((queueId + "\t").getBytes("UTF-8"));
                                ctx.writeAndFlush(message);
                                logger.info("client send end...");
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                String body = (String) msg;
                                logger.info("client receive message : " + body);
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                super.exceptionCaught(ctx, cause);
                            }
                        });
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect("localhost",8899).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }


    }

}
