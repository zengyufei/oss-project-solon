package com.zyf.controller.websocket;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.net.annotation.ServerEndpoint;
import org.noear.solon.net.websocket.WebSocket;
import org.noear.solon.net.websocket.listener.SimpleWebSocketListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@ServerEndpoint("/ws/demo")
public class WebSocketDemo extends SimpleWebSocketListener {

    private static ConcurrentHashMap<String, WebSocket> idWbs = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<WebSocket, String> wbIds = new ConcurrentHashMap<>();

    static {
        // 定时任务每5秒检查是否有超时的连接
        new Thread(() -> {
            while (true) {
                try {
                    log.trace("存活websocket数量: {}", wbIds.size());
                    for (WebSocket webSocket : wbIds.keySet()) {
                        if (!webSocket.isValid()) {
                            log.trace("移除失效websocket!");
                            final String id = wbIds.get(webSocket);
                            wbIds.remove(webSocket);
                            idWbs.remove(id);
                        }
                    }
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ignored) {
                } finally {
                    log.trace("每5秒检查websocket");
                }
            }
        }).start();
    }

    @Override
    public void onOpen(WebSocket socket) {
        String id = socket.param("id");
        idWbs.put(id, socket);
        wbIds.put(socket, id);
    }

    @Override
    public void onMessage(WebSocket socket, String text) throws IOException {
        socket.send("server 我收到了：" + text);
    }

    @Override
    public void onMessage(WebSocket socket, ByteBuffer binary) throws IOException {

    }

    @Override
    public void onClose(WebSocket socket) {
        if (wbIds.containsKey(socket)) {
            final String id = wbIds.get(socket);
            wbIds.remove(socket);
            idWbs.remove(id);
        }
    }

    @Override
    public void onError(WebSocket socket, Throwable error) {

    }

    public void sendMessage(String message) {
        for (WebSocket webSocket : wbIds.keySet()) {
            webSocket.send(message);
        }
    }

    public void sendMessage(String id, String message) {
        if (idWbs.containsKey(id)) {
            final WebSocket webSocket = idWbs.get(id);
            webSocket.send(message);
        }
    }

}
