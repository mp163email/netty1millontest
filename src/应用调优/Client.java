package 应用调优;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

import java.util.concurrent.ExecutionException;

/**
 * des: QPS统计放在了客户端中
 *
 * 客户端启动后，会记录一个启动时间戳
 * 当前时间-启动时间戳=启动了多长时间
 * 这段时间处理完成的请求数 / 启动了多长时间（秒） = 每秒能处理多少个请求（QPS）
 *
 * 用一个变量记录所有请求响应的时间 / 总的请求数 = 平均每个请求的响应时间
 *
 * 客户端主程序启动后，创建了一个用于统计的线程：每2秒统计并输出一次QPS和平均响应时间
 * 客户端联通服务器后，每个Channel都每1秒向服务器发送一个long型的时间戳。 用的是自己Channel上下文里的ctx.executor().scheduleAtFixedRate
 * created by miapoeng on 2019/11/18 15:46
 */
public class Client {
    public static void main(String[] args) {
        new Client().start();
    }

    public void start () {
        EventLoopGroup worker = new NioEventLoopGroup();

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new FixedLengthFrameDecoder(Long.BYTES));
                pipeline.addLast(ClientBussnessHandler.INSTANCE);
            }
        });

        for (int i = 0; i < 1000; i++) {
            try {
                bootstrap.connect(Constant.HOST, Constant.PORT).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
