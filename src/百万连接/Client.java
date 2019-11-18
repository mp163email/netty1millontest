package 百万连接;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * des: 客户端可以不停的去链接某个端口（这里是不停的链接100个端口）
 * created by miapoeng on 2019/11/15 10:06
 */
public class Client {

    private static final String SERVER_HOST = "192.168.26.100";

    public static void main(String[] args) throws Exception {
        new Client().start(Constant.BEGIN_PORT, Constant.N_PORT);
    }

    public void start (final int beginPort, int nPort) throws Exception {
        System.out.println("client starting .....");

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {

            }
        });

        int index = 0;
        int port;
        while (index < nPort) {
            port = beginPort + index;
            try {
                ChannelFuture channelFuture = bootstrap.connect(SERVER_HOST, port);
                channelFuture.addListener((ChannelFuture future) -> {
                    if (!future.isSuccess()) {
                        System.out.println("connected faild , exit !!");
                        System.exit(0);
                    }
                });
                channelFuture.get();
            } catch (Exception e) {
                System.out.println(e);
            }
            index++;
            if (++index == nPort) {
                index = 0;
            }
        }
    }
}
