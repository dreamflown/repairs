package com.edu.bupt.repairs.model.message;


import com.edu.bupt.repairs.model.Order;

import java.math.BigInteger;

public class AuditResultMsg extends ToLeaderMsg<String> {

    BigInteger from;

    BigInteger to;

    Order order;

    String auditMsg;

    public void send(BigInteger from, BigInteger to, Order order, String content) {
        this.from = from;
        this.to = to;
        this.auditMsg = auditMsg;
        this.content = content;

        // todo websocket 发送消息
    }
}
