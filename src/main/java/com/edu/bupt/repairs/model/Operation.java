package com.edu.bupt.repairs.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class Operation {
    private BigInteger id;

    private BigInteger operator;

    private BigInteger orderId;

    private String info;

    private Timestamp lastUpdateTime;
}
