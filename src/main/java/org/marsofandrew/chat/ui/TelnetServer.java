package org.marsofandrew.chat.ui;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.marsofandrew.chat.core.TopicService;

/**
 * This server is based on netty telnet server example with more detailed implementation.
 */
@Slf4j
@RequiredArgsConstructor
public class TelnetServer {

    private static final int DEFAULT_CLIENTS_LIMIT = 10;
    private final int port;

    private final int messageLimit;

    private final int clientsPerChanelLimit;


    public void run() throws Exception {
        log.info("Server started");
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try  {
            TopicService<String> topicService =
                    new TopicService<>(messageLimit, clientsPerChanelLimit, clientsPerChanelLimit);
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>(){

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(new DelimiterBasedFrameDecoder(
                                            8192, Delimiters.lineDelimiter()),
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new TelnetServerHandler(topicService));
                        }
                    });
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            }));

            b.bind(port).sync().channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2){
            System.err.println("Need 2 parameters port and message limit");
            System.exit(1);
        }
        int port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e){
            System.err.println("incorrect format of port number");
            System.exit(1);
        }

        int messageLimit = -1;
        try {
            messageLimit = Integer.parseInt(args[1]);
        } catch (NumberFormatException e){
            System.err.println("incorrect format of messageLimit number");
            System.exit(1);
        }

        new TelnetServer(port, messageLimit, DEFAULT_CLIENTS_LIMIT).run();
    }
}