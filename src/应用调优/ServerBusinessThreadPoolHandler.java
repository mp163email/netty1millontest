package 应用调优;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * des: 使用线程池，处理业务逻辑。使处理逻辑线程与IO线程分开
 * created by miapoeng on 2019/11/18 17:59
 */
public class ServerBusinessThreadPoolHandler extends ServerBusinessHandler {

    public static final ServerBusinessThreadPoolHandler INSTANCE = new ServerBusinessThreadPoolHandler();

    private static ExecutorService threadPool = Executors.newFixedThreadPool(24);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ByteBuf data = Unpooled.directBuffer();
        data.writeBytes(msg);
        threadPool.submit(() -> {
            Object result = getResult(data);
            ctx.channel().writeAndFlush(result);
        });
    }
}
