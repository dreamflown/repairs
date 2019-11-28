package com.edu.bupt.repairs.service;

import com.alibaba.fastjson.JSONObject;

import com.edu.bupt.repairs.model.Order;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Date;


public interface OrderService {

    /**
     * 1.发起维修申请
     * @return Order
     */
    JSONObject addWeiXiuShenQing(BigInteger id,BigInteger user_id,int status,int status_type);
    /*调用合同模块获得facilitator_id,clearing_form*/


    /**
     * 2.创建工单
     * @return Order
     */
    JSONObject addOrder(BigInteger user_id, BigInteger maintainer_id,Date estimated_time,Date deadline,
                        BigInteger device_id,int level, DecimalFormat request_latitude, DecimalFormat request_longitude,
                        String description,String enclosure_url);

    int addOrder(Order order);



    /**
     * 3.确认服务完成

     * @return Order
     */
    JSONObject QueRenFuWu(BigInteger user_id,Date finished_time);



    /**
     * 4.提交备件更换方案
     * @return Order
     */
    JSONObject addBeiJianGengHuanFangAn(BigInteger id,BigInteger task_id,String device_type,
                                        int count,Date start_time,DecimalFormat cost);

    /**
     * 5.提交评价
     * @return Order
     */
    JSONObject addPingJia(BigInteger id,int score,String contents);

    /**
     * 6.选择服务商
     * @return Order
     */
    JSONObject selectFacilitator(BigInteger user_id,BigInteger facilitator_id);

    /**
     * 7.修改工单状态
     * @param orderId
     * @param NextStatus
     * @return
     */
    Order changeOrderStatus(BigInteger orderId, String NextStatus);

    /**
     * 8.获取工单信息
     * @param orderId
     * @return
     */
    Order getOrderInfo(BigInteger orderId);

}
