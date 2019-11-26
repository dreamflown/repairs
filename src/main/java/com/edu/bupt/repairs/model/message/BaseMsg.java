package com.edu.bupt.repairs.model.message;

import java.math.BigInteger;

public interface BaseMsg<T> {
    void send(BigInteger from, BigInteger to, T content);
}
