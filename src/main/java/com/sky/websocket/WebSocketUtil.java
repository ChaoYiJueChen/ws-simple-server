package com.sky.websocket;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.util.Map;

public class WebSocketUtil {
    private static Logger logger = LoggerFactory.getLogger(WebSocketUtil.class);

    /**
     * 根据macId获取Session 会话
     *
     * @param macId
     * @return
     */
    public static Session getSession(String macId) {
        return WebSocketServer.serverMap.get(macId);
    }


    /**
     * 根据Session获取macId 会话
     *
     * @param session
     * @return
     */
    public static String getMacId(Session session) {
        for (Map.Entry<String, Session> entry : WebSocketServer.serverMap.entrySet()) {
            Session sessionValue = entry.getValue();
            if (null != sessionValue && sessionValue.equals(session)) {
                return entry.getKey();
            }
        }
        return "";
    }

    /* 服务器端信息发送的方法
     * @param macId
     * @param message
     */
    public static void sendMessage(String macId, String message) throws Exception {
        Session session = getSession(macId);
        if (null != session) {
            WebSocketServer.messageMap.remove(macId);  //发送消息之前应该把已经存在的消息清空。
            synchronized (session) {
                session.getBasicRemote().sendText(message);
            }
            logger.info("已经成功向医保客户端---" + macId + "---发送的消息：" + message);
        }

    }


}