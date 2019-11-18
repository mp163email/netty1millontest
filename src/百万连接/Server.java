package 百万连接;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * des: 服务器端
 * 1.处理逻辑不用IO线程，用自定义线程池
 * 2.Linux不要受单个进程最大文件句柄数限制
 * 3.Linux不要受所有进程最大文件句柄数限制
 * created by miapoeng on 2019/11/15 10:06
 */
public class Server {
    public static void main(String[] args) throws Exception{
        new Server().start(Constant.BEGIN_PORT, Constant.N_PORT);
    }

    public void start (int beginPort, int nPort) throws InterruptedException{
        System.out.println("server starting ........");

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);//这些TCP参数是做啥的

        bootstrap.childHandler(new ConnectionHandler());

        for (int i = 0; i < nPort; i++) {
            int port = beginPort + i;
            ChannelFuture channelFuture = bootstrap.bind(port);
            channelFuture.addListener((ChannelFuture cf) -> {
                if (cf.isSuccess()) {
                    Channel channel = cf.sync().channel();
                    InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
                    System.out.println("bind success.  IP: " + socketAddress.getHostName() + ",  PORT=" + socketAddress.getPort());
                }
            });

        }

        System.out.println("server  started !!");
    }
}
