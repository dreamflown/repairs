package com.edu.bupt.repairs.model;

import lombok.Data;

@Data
public class Order {
    private String id;
    private  String address;
    private Integer type;
    private  Boolean serverTakeOrderResult;
    private Boolean workerTakeOrderResult;
    private Boolean serverApprovalPayResult;



    public Order(String userid, String address, Integer type) {
        this.id = userid;
        this.address = address;
        this.type = type;
    }
    public void setServerApprovalPayResult(Boolean serverApprovalPayResult){
        this.serverApprovalPayResult=serverApprovalPayResult;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public Integer getType() { return type; }

    public void setType(Integer type) {
        this.type= type;
    }

    public Boolean getServerTakeOrderResult() {
        return serverTakeOrderResult;
    }

    public void setServerTakeOrderResult(Boolean serverTakeOrderResult) {
        this.serverTakeOrderResult =serverTakeOrderResult;}

    public Boolean getWorkerTakeOrderResult() {
        return workerTakeOrderResult;
    }

    public void setWorkerTakeOrderResult(Boolean workerTakeOrderResult) {
        this.workerTakeOrderResult =workerTakeOrderResult;}

    public Order() {
        super();
    }
}
