package Netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ExecutionException;

public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            //创建客户端启动对象，注意客户端使用的不是ServerBootStrap而是BootStrap
            Bootstrap bootstrap = new Bootstrap();

            Promise<Object> promise = new DefaultPromise<>(group.next());

            NettyClientHandler handler = new NettyClientHandler();

            handler.promise = promise;
            //设置相关参数
            bootstrap
                    .group(group)//设置线程组
                    .channel(NioSocketChannel.class)//设置客户端通道的实现类
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(handler);
                        }
                    });

            System.out.println("客户端OK");
            //启动客户端去连接服务器端
            //关于channel要分析，要分析netty的异步模型
            ChannelFuture channelfulture = bootstrap.connect("127.0.0.1", 6668).sync();
            Object o = handler.promise.get();
            System.out.println(o + "    promise   this test");
            channelfulture.channel().closeFuture().sync();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
