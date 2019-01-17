package io.gazetteer.tileserver;

import io.gazetteer.mbtiles.XYZ;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.NOT_MODIFIED;

@Sharable
public class TileServerHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TileServerHandler.class);

    private static final AsciiString TYPE = AsciiString.cached("text/plain");

    private static final AsciiString ENCODING = AsciiString.cached("gzip");

    private static final long STARTUP_TIME = System.currentTimeMillis();

    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final String DATE_GMT_TIMEZONE = "GMT";

    private static final AsciiString MAX_AGE = AsciiString.cached("public, max-age=0, no-transform"); // disable cache for now

    private static final String ROOT_URI = "/";

    private static final String ROOT_CONTENT = "Gazetteer Tile Server v0.1";

    private final TileServerConfig config;

    public TileServerHandler(TileServerConfig config) {
        this.config = config;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpRequest req) throws ParseException {
        if (!req.decoderResult().isSuccess()) {
            sendError(ctx);
            return;
        }

        String ifModifiedSince = req.headers().get(IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            long downloadTime = dateFormatter.parse(ifModifiedSince).getTime();
            if (STARTUP_TIME < downloadTime) {
                sendNotModified(ctx);
                return;
            }
        }

        if (ROOT_URI.equals(req.uri())) {
            sendRoot(ctx, ROOT_CONTENT);
            return;
        }

        Matcher tileMatcher = config.tileUri.matcher(req.uri());
        if (tileMatcher.matches()) {
            int z = Integer.parseInt(tileMatcher.group(1));
            int x = Integer.parseInt(tileMatcher.group(2));
            int y = Integer.parseInt(tileMatcher.group(3));
            sendTile(ctx, z, x, y);
            return;
        }

        sendError(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.info("Exception caught", cause);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
        setDateHeader(response);
        ctx.channel().write(response).addListener(ChannelFutureListener.CLOSE);
    }


    private void sendRoot(ChannelHandlerContext ctx, String content) {
        ByteBuf data = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(data));
        setDateHeader(response);
        response.headers().set(CONTENT_TYPE, TYPE);
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendTile(ChannelHandlerContext ctx, int z, int x, int y) {
        XYZ coord = new XYZ(x, y, z);
        config.dataSource.getTile(coord).thenAccept(tile -> {
            if (tile != null) {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(tile.getBytes()));
                setDateHeader(response);
                response.headers().set(CONTENT_TYPE, config.dataSource.getMimeType());
                response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
                response.headers().set(CONTENT_ENCODING, ENCODING);
                response.headers().set(CACHE_CONTROL, MAX_AGE);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        });
    }

    private void sendError(ChannelHandlerContext ctx) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
        setDateHeader(response);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED);
        setDateHeader(response);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(DATE_GMT_TIMEZONE));
        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));
    }


}