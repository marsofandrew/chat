package org.marsofandrew.chat.ui;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * This server is based on netty telnet server example with more detailed implementation.
 */
@Slf4j
public class TelnetServer {

    private final int port;

    public TelnetServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        log.info("Server started");
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            TelnetServerInitializer initializer = new TelnetServerInitializer();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(initializer);
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

        new TelnetServer(8080).run();
    }
}