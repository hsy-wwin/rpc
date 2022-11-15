package Netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Promise;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    public Promise<Object> promise;

    @Override
    //当通道就绪就会触发该方法
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("clinet" + ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello,Server",CharsetUtil.UTF_8));
    }

    //当通道有读取事件时会触发
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        promise.setSuccess(msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("服务器回复的消息:" + byteBuf.toString(CharsetUtil.UTF_8));
        System.out.println("服务器的地址:" + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
