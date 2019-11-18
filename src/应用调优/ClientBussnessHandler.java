package 应用调优;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * des:
 * created by miapoeng on 2019/11/18 15:56
 */
@ChannelHandler.Sharable
public class ClientBussnessHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static final ClientBussnessHandler INSTANCE = new ClientBussnessHandler();

    private static AtomicLong beginTime = new AtomicLong(0);
    private static AtomicLong totalResponseTime = new AtomicLong(0);
    private static AtomicInteger totalRequest = new AtomicInteger(0);

    public static final Thread THREAD = new Thread(() -> {
        while (true) {
            long duration = (System.currentTimeMillis() - beginTime.get()) / 1000;
            if (duration > 0) {
                //qps-每秒能处理多少个请求=总的请求量/总的客户端运行时间（秒）
                //平均每个请求的响应时间= 总的响应时间 / 总的请求数
                System.out.println("总请求数=" + totalRequest.get() + ", 总运行时间（秒）=" + duration + ", qps:" + totalRequest.get() / duration + ", average response time = " + ((float)totalResponseTime.get()) / totalRequest.get());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        //请求耗时 = 当前时间 - 客户端收到服务器返回的自己先前发送的时间戳
        //累加请求耗时
        totalResponseTime.addAndGet(System.currentTimeMillis() - msg.readLong());
        //累加请求数量
        totalRequest.incrementAndGet();
        //放到这里不太合适，应该放到客户端启动完成后
        if (beginTime.compareAndSet(0, System.currentTimeMillis())) {
            THREAD.start();
        }
    }

    /**
     * 连接建立的时候
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.executor().scheduleAtFixedRate(() -> {
            ByteBuf byteBuf = ctx.alloc().ioBuffer();
            byteBuf.writeLong(System.currentTimeMillis());
            ctx.channel().writeAndFlush(byteBuf);
        }, 0, 1, TimeUnit.SECONDS);
    }
}
