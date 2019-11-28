package com.edu.bupt.repairs.service;

import com.alibaba.fastjson.JSONObject;

import java.math.BigInteger;
import java.text.DecimalFormat;

public interface WorkerService {
    /**
     * 1.接单
     * @return Order
     */
    JSONObject takeOrder(BigInteger task_id);


    /**
     * 2.拒绝接单
     * @return Order
     */
    JSONObject rejectTakeOrder(BigInteger maintainer_id);

    /**
     * 3.服务确认
     * @return Order
     */
    JSONObject QueRenFuWu(String suggestion, String result, DecimalFormat total_cost);

    /**
     * 4.提交备件更换申请
     * @return Order
     */
    JSONObject TiJiaoBeiJianGengHuanShenQing();
}
