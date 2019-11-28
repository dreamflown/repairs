package com.edu.bupt.repairs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    // 设备编号
    private String serial;
    // 设备名称
    private String name;
    // 设备厂商
    private String manufacturer;
    // 设备型号
    private String model;
    // 设备价格
    private float cost;

}
