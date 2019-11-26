package com.edu.bupt.repairs.service;

import com.alibaba.fastjson.JSONObject;

import java.math.BigInteger;

public interface ServerService {
    /**
     * 1.接单
     * @return Order
     */
    JSONObject takeOrder(BigInteger facilitator_id);


    /**
     * 2.拒绝接单
     * @return Order
     */
    JSONObject rejectTakeOrder(BigInteger facilitator_id);

    /**
     * 3.分配维修工
     * @return Order
     */
    JSONObject FenPeiMaintainer(BigInteger maintainer_id);

    /**
     * 4.审批备件金额
     * @return Order
     */
    JSONObject ShenPiBeiJianJinE();
    /*调用审批模块*/
}
