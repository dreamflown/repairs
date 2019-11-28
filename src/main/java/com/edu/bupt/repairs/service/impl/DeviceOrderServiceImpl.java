package com.edu.bupt.repairs.service.impl;

import com.edu.bupt.repairs.model.DeviceOrder;
import com.edu.bupt.repairs.service.DeviceOrderService;
import com.edu.bupt.repairs.utils.IDKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceOrderServiceImpl implements DeviceOrderService {

    private Map<BigInteger, DeviceOrder> deviceOrderMap = new ConcurrentHashMap();

    private IDKeyUtil keyConstructor = new IDKeyUtil(5L, 5L);

    @Autowired
    DeviceOrderMapper deviceOrderMapper;

    @Override
    public DeviceOrder getDeviceOrderInfo(@NotNull BigInteger deviceOrderId) {
        DeviceOrder deviceOrderCache = deviceOrderMap.get(deviceOrderId);
        if (deviceOrderCache == null) {
            DeviceOrder deviceOrder = deviceOrderMapper.selectDeviceOrderById(deviceOrderId);
            if (deviceOrder == null) {
                return null;
            }
            deviceOrderMap.putIfAbsent(deviceOrderId, deviceOrder);
            deviceOrderCache = deviceOrder;
        }
        return deviceOrderCache;
    }

    @Override
    public DeviceOrder saveDeviceOrder(DeviceOrder deviceOrder) {
        BigInteger deviceOrderId = keyConstructor.nextGlobalId();
        deviceOrder.setId(deviceOrderId);
        deviceOrderMap.putIfAbsent(deviceOrderId, deviceOrder);
        if (deviceOrderMapper.selectDeviceOrderById(deviceOrderId == null)) {
            deviceOrderMapper.insert(deviceOrder);
        }
        return deviceOrder;
    }
}
