package com.edu.bupt.repairs.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
public class Task {
    // 任务id
    private BigInteger id;
    // 维修工id
    private BigInteger workerId;
    // 设备名称
    private String deviceName;
    // 设备地址
    private String deviceAddress;
    // 故障类型
    private String troubleType;
    // 故障信息
    private String troubleInfo;
    // 故障等级
    private Integer troubleLevel;
    // 图片
    private String imageUtl;
    // 视频
    private String videoUrl;
    // 音频
    private String audioUrl;
    // 人工费
    private float laborCost;
    // 备件更换费用
    private float deviceCost;


}
