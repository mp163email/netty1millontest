package 百万连接;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * des: 一个Server只有1个ConnectionHandler对象
 * created by miapoeng on 2019/11/15 14:35
 */

/***为啥要加Sharable的注解呢? 不加就会报错***/
@ChannelHandler.Sharable
public class ConnectionHandler extends ChannelInboundHandlerAdapter {

    private AtomicInteger nConnection = new AtomicInteger();

    /**
     * 构造方法里创建一个定时线程，每2秒打印一下有多少个链接链进来了
     */
    public ConnectionHandler () {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("connections:" + nConnection.get());
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        nConnection.incrementAndGet();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        nConnection.decrementAndGet();
    }
}
