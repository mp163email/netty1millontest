package 应用调优;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ThreadLocalRandom;

/**
 * des:
 * created by miapoeng on 2019/11/18 15:14
 */
@ChannelHandler.Sharable
public class ServerBusinessHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static final ServerBusinessHandler INSTANCE = new ServerBusinessHandler();


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ByteBuf data = Unpooled.directBuffer();
        data.writeBytes(msg);
        Object result = getResult(data);
        ctx.channel().writeAndFlush(result);
    }

    /**
     * 模拟一些延时的逻辑
     * 按一些几率延时
     * @param data
     * @return
     */
    public Object getResult (ByteBuf data) {
        //90%=1ms
        //95%=10ms
        //99%=100ms
        //99.9%=1000ms

        int level = ThreadLocalRandom.current().nextInt(1, 1000);
        int time;
        if (level <= 900) {
            time = 1;
        } else if (level <= 950) {
            time = 10;
        } else if (level <= 990) {
            time = 100;
        } else {
            time = 1000;
        }
        try {
            Thread.currentThread().sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return data;
    }
}
