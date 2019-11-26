package com.edu.bupt.repairs.service;

import com.alibaba.fastjson.JSONObject;

import java.math.BigInteger;
import java.text.DecimalFormat;

public interface DeviceService {
    /**
     * 1.提供备选备件
     * @return Order
     */
    JSONObject addBeiXuanBeiJian(BigInteger id, String device_model, String device_type,int count, DecimalFormat cost);

}
