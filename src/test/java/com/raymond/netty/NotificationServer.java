package com.raymond.netty;

import com.raymond.callmonitoring.utils.NettyDirectMemReporter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class NotificationServer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServer.class);
    private static ByteBuf delimiter = Unpooled.copiedBuffer("\t".getBytes());
    //private static ByteBuf serverResponse = Unpooled.copiedBuffer(" fucking server\t".getBytes());
    private static NettyDirectMemReporter reporter = new NettyDirectMemReporter();

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new SecondChannelInitializer());

        try {
            ChannelFuture future = bootstrap.bind(8899).sync();
            reporter.startReport();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    private static class FirstChannelInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1000, delimiter));
            ch.pipeline().addLast(new StringDecoder());
            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    String body = (String) msg;
                    ByteBuf message;
                    message = Unpooled.directBuffer();
                    message.writeBytes((body + "\t").getBytes("UTF-8"));
                    System.out.println(ctx.channel().isWritable());
                    ctx.writeAndFlush(message);
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    super.exceptionCaught(ctx, cause);
                }
            });
            //ch.pipeline().addLast(new StringEncoder());
        }
    }

    private static class SecondChannelInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch) throws Exception {
             ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                 @Override
                 public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                     ByteBuf byteBuf = (ByteBuf) msg;
                     byte[] bytes = new byte[byteBuf.readableBytes()];
                     byteBuf.readBytes(bytes);
                     String body = new String(bytes, Charset.forName("UTF-8"));
                     ReferenceCountUtil.release(byteBuf);
                     //System.out.println();

                     //Thread.currentThread().join(300);

//                     if (!ctx.channel().isWritable()) {
//                         System.out.println("channel writable is false....");
//                         return;
//                     }
                     ByteBuf message = Unpooled.directBuffer();
                     message.writeBytes((body + "\t").getBytes("UTF-8"));
                     ctx.writeAndFlush(message);
                 }

                 @Override
                 public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                     System.out.println(reporter.getDirectMem());;
                     super.exceptionCaught(ctx, cause);
                 }
             });
        }
    }
}
