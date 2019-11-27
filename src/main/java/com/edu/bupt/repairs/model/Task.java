package com.edu.bupt.repairs.model;

import lombok.Data;

import java.math.BigInteger;

@Data
public class Task {
    private BigInteger maintainId;

    private BigInteger serverId;

    public void setMaintainerId(BigInteger maintainerId){this.maintainId=maintainerId;}

    public void setServerId(BigInteger serverId){this.serverId=serverId;}

    public Task() {
        super();
    }

}
