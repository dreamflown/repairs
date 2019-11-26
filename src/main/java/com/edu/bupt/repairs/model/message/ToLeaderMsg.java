package com.edu.bupt.repairs.model.message;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class ToLeaderMsg<T> implements BaseMsg<T> {

    long id;

    BigInteger from;

    BigInteger to;

    T content;

    @Override
    public void send(BigInteger from, BigInteger to, T content) {
        this.id = new Date().getTime();
        this.from = from;
        this.to = to;
        this.content = content;

        // todo websocker 发送消息
    }


}
