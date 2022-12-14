package Netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        //创建两个线程组：BossGroup:处理连接请求和WorkerGroup：真正与客户端业务处理，两个都是无限循环
        EventLoopGroup BossGroup = new NioEventLoopGroup(1);
        EventLoopGroup WorkerGroup = new NioEventLoopGroup();

        try {
            //创建服务器端的启动对象，配置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            //使用链式编程来进行设置
            bootstrap.group(BossGroup, WorkerGroup)//设置两个线程组
                    .channel(NioServerSocketChannel.class)//使用NIOSocketChannel作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128)//设置线程队列得到连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true)//设置保持活动连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {//创建一个通道初始化对象(匿名对象)
                        //给pipline设置处理器
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });//给我们EventGroup对应的管道设置处理器

            System.out.println("服务器is ready!");

            //绑定一个端口并同步,生成了一个ChannelFuture对象
            //启动服务器(并绑定端口)
            ChannelFuture channelFuture = bootstrap.bind(6668).sync();

            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        }finally {
            BossGroup.shutdownGracefully();
            WorkerGroup.shutdownGracefully();
        }
    }
}
