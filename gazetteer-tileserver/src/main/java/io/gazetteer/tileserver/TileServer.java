package io.gazetteer.tileserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(description = "Start a tile server")
public class TileServer implements Runnable {

    public final TileServerConfig config;

    public TileServer() {
        this.config = TileServerConfig.fromPGTiles();
    }

    public static void main(String[] args) {
        CommandLine.run(new TileServer(), args);
    }

    @Override
    public void run() {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            if (config.sslContext != null) {
                                p.addLast(config.sslContext.newHandler(ch.alloc()));
                            }
                            p.addLast(new HttpServerCodec());
                            p.addLast(new HttpObjectAggregator(512 * 1024));
                            p.addLast(new CorsHandler(
                                    CorsConfigBuilder.forOrigin("*")
                                            .allowedRequestMethods(HttpMethod.POST)
                                            .build())
                            );
                            p.addLast(new HttpServerExpectContinueHandler());
                            p.addLast(new TileServerHandler(config));
                        }
                    });
            Channel ch = b.bind(config.host, config.port).sync().channel();
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
