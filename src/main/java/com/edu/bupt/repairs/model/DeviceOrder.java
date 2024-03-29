package com.edu.bupt.repairs.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
public class DeviceOrder {
    // 设备订单编号
    private BigInteger id;
    // 工单编号
    private BigInteger orderId;
    // 设备列表
    private List<Device> devices;
    // 总费用
    private float totalCost;

}
