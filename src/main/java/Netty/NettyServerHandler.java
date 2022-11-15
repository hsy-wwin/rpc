package Netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

//我们自定义一个handler需要继续netty规定好的某个HandlerAdapter(规范)
//这时我们制定的一个handler,才能称为一个handler

public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    //这里我们可以读取客户端发送的消息
    /*
    1.ChannelHandlerContext ctx:上下文对象,含有管道pipline,通道channel,地址
    2.Object msg默认是客户端发送的消息，默认Object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server ctx=" +ctx);
        //将msg转成byteBuf
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("客户端发送消息是" + byteBuf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址是："+ ctx.channel().remoteAddress());
    }

    //数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //writeAndFlush是write方法+Flush方法，将数据写入到缓存并刷新
        //一般来讲我们对发送的数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello客户端", CharsetUtil.UTF_8));
    }

    //处理异常,一般是需要关闭通道
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
