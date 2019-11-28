package com.edu.bupt.repairs.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edu.bupt.repairs.model.Order;
import com.edu.bupt.repairs.service.OrderService;
import lombok.Synchronized;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderServiceImpl implements OrderService {

    Map<BigInteger, Order> orderMap = new ConcurrentHashMap<>();

    @Override
    public JSONObject addWeiXiuShenQing(BigInteger id, BigInteger user_id, int status, int status_type) {
        return null;
    }

    @Override
    public JSONObject addOrder(BigInteger user_id, BigInteger maintainer_id, Date estimated_time, Date deadline, BigInteger device_id, int level, DecimalFormat request_latitude, DecimalFormat request_longitude, String description, String enclosure_url) {
        return null;
    }

    public int addOrder(Order order){
        return orderMapper.insertOrder(order);
    }

    @Override
    public JSONObject QueRenFuWu(BigInteger user_id, Date finished_time) {
        return null;
    }

    @Override
    public JSONObject addBeiJianGengHuanFangAn(BigInteger id, BigInteger task_id, String device_type, int count, Date start_time, DecimalFormat cost) {
        return null;
    }

    @Override
    public JSONObject addPingJia(BigInteger id, int score, String contents) {
        return null;
    }

    @Override
    public JSONObject selectFacilitator(BigInteger user_id, BigInteger facilitator_id) {
        return null;
    }

    @Override
    @Synchronized
    public Order changeOrderStatus(BigInteger orderId, String nextStatus) {
        // 查缓存
        Order orderCache = orderMap.get(orderId);
        if(orderCache == null) {
            // 缓存没有查数据库
            Order order = orderMapper.getOrderByOrderId(orderId);
            if (order == null) {
                return null;
            } else {
                order.setStatus(nextStatus);
                orderCache = order;
            }
        }

        // 修改工单状态
        orderMapper.updateStatus(orderId, nextStatus);
        orderMap.put(orderId, orderCache);

        return orderCache;
    }

    @Override
    public Order getOrderInfo(BigInteger orderId) {

        return orderMapper.getOrderById(orderId);
    }
}
