package com.edu.bupt.repairs.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
public class Contract {
    // 合同id
    private BigInteger id;
    // 甲方
    private String firstParty;
    // 乙方
    private String secondParty;
    // 设备
    private List<Device> devices;
    // 合同类型：期帐，包年，现结
    private String type;
    // 指定维修工
    private List<User> workers;
    // 签订时间
    private Timestamp createTime;
    // 到期时间
    private Timestamp expireTime;

}
