package com.raymond.netty;

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

    public static void main(String[] args){
        ByteBuf delimiter = Unpooled.copiedBuffer("\t".getBytes());

        EventLoopGroup group = new NioEventLoopGroup();

        final int[] counter = {0};
        String msg = "Hello fucking Server, i am fucking client...., count:" + counter[0] + "\t";
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(100, delimiter));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ByteBuf message;
                                message = Unpooled.buffer();
                                message.writeBytes(msg.getBytes("UTF-8"));
                                ctx.writeAndFlush(message);
                                System.out.println("client send end...");

                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                counter[0]++;
                                String msg1 = "Hello fucking sever, i am fucking client...., count:" + counter[0] + "\t";
                                String body = (String) msg;
                                System.out.println("client receive message : " + body);
                                System.out.println("send to server again");

                                ByteBuf message;
                                message = Unpooled.buffer();
                                message.writeBytes(msg1.getBytes("UTF-8"));
                                ctx.writeAndFlush(message);
                                System.out.println("client send end...");
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                super.exceptionCaught(ctx, cause);
                            }
                        });
                        //ch.pipeline().addLast(new StringEncoder());
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
