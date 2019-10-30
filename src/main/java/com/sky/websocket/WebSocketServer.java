package com.sky.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@ServerEndpoint(value = "/websocket/server/{macId}")
public class WebSocketServer {

    private static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    // 已经建立链接的对象缓存起来
    public static ConcurrentMap<String, Session> serverMap = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, String> messageMap = new ConcurrentHashMap<>();
    // 当前session
    private Session currentSession;

    @OnOpen
    public void onOpen(Session session, @PathParam("macId") String macId) {
        session.setMaxIdleTimeout(60000); //60才超时
        logger.info("客户端已经建立连接，客户端macId是：" + macId);
        this.currentSession = session;
        serverMap.put(macId, session);//建立链接时，缓存对象
}

    @OnClose
    public void onClose(Session session, CloseReason reason) throws Exception {
        if (!serverMap.containsValue(session)) return;
        Iterator<String> keys = serverMap.keySet().iterator();
        String macId = null;
        while (keys.hasNext()) {
            macId = keys.next();
            if (serverMap.get(macId) == session) {
                serverMap.remove(macId, session);//关闭链接时，删除缓存对象
            }
        }
        this.currentSession = null;
        session.close();
        logger.info("客户端macId：" + macId + "已经断开连接，断开连接原因：" + reason.toString());
    }

    @OnMessage
    public void onMessage(String json) throws Exception {
        String macId = WebSocketUtil.getMacId(this.currentSession);
        if (json.equals("ok")) {
            logger.info("收到客户端：" + macId + "的心跳检测：" + json);
            WebSocketUtil.sendMessage(macId, "ok");
            return;
        }
        logger.info("收到客户端：" + macId + "的业务数据：" + json);
        messageMap.put(macId, json);
    }


    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }
}
