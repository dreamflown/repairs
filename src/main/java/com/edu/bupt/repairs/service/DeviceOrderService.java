package com.edu.bupt.repairs.service;

import com.edu.bupt.repairs.model.DeviceOrder;

import java.math.BigInteger;

public interface DeviceOrderService {
    DeviceOrder getDeviceOrderInfo(BigInteger deviceOrderId);

    DeviceOrder saveDeviceOrder(DeviceOrder deviceOrder);
}
