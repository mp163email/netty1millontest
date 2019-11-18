package 应用调优;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * des: 方案一：自定义线程池，处理自己想要处理的逻辑
 * 优点：可控，比如下面的代码，只是将后两行代码提交到线程池进行异步处理
 * created by miapoeng on 2019/11/18 14:57
 */
public class Server {

    private static final AtomicInteger totalClient = new AtomicInteger(0);

    public static void main(String[] args) {
        new Server().start();
    }

    public void start () {
        EventLoopGroup bossEventLoop = new NioEventLoopGroup();

        // ***这里有8个处理客户端的线程，所以如果每个客户端每秒向服务器发1个包，而服务器每1秒钟处理1个包。则不论有多少个客户端，服务器每秒处理的请求数大约都会等于8****
        EventLoopGroup workerEventLoop = new NioEventLoopGroup();//默认是cup核数*2=8

        EventLoopGroup businessEventLoop = new NioEventLoopGroup(1000);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossEventLoop, workerEventLoop);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new FixedLengthFrameDecoder(Long.BYTES));
//                pipeline.addLast(ServerBusinessHandler.INSTANCE);//没有调优的：完全使用IO线程处理逻辑
                pipeline.addLast(businessEventLoop, ServerBusinessHandler.INSTANCE);//调优方式二：在netty启动的时候指定线程池处理逻辑
//                pipeline.addLast(ServerBusinessThreadPoolHandler.INSTANCE);//调优方式一：自定义线程池处理逻辑
            }
        });
        bootstrap.bind(Constant.PORT).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("服务器绑定端口成功");
                }
            }
        });
    }
}
