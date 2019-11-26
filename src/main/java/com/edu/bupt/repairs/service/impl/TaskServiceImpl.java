package com.edu.bupt.repairs.service.impl;

import com.edu.bupt.repairs.commom.OrderStatus;
import com.edu.bupt.repairs.model.Order;
import com.edu.bupt.repairs.model.message.AuditResultMsg;
import com.edu.bupt.repairs.model.message.ToLeaderMsg;
import com.edu.bupt.repairs.service.OrderService;
import com.edu.bupt.repairs.service.TaskService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.istack.internal.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    OrderStatus status;

    @Override
    public void submitTask(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取报修用户信息
        BigInteger uid = json.get("uid").getAsBigInteger();
        User user = userService.getUserInfo(uid);
        if (user = null) {
            return;
        }
        String name = user.getName();
        String phone = user.getPhone();
        String email = user.getEmail();
        String company = user.getCompany();

        // 获取其他信息
        String deviceName = json.get("deviceName").getAsString();  // 设备名称
        String deviceAddress = json.get("deviceAddress").getAsString(); // 设备地址
        String troubleType = json.get("trouble").getAsString(); // 故障类型
        String troubleInfo = json.get("troubleInfo").getAsString(); // 故障信息
        BigInteger serviceProviderId = json.get("serviceProviderId").getAsBigInteger(); // 服务商id
        float laborCost = json.get("laborCost").getAsFloat(); // 人工费用
        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 审核人id

        // TODO 获取图片和视频


        // 自动生成时间信息
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Timestamp createTime = new Timestamp(cal.getTime().getTime()); // 报修时间
        cal.add(Calendar.DAY_OF_WEEK, 1); //增加一周
        Timestamp ddl = new Timestamp(cal.getTime().getTime()); // 计划完成时间

        // 生成工单编号
        Random rnd = new Random();
        rnd.setSeed(createTime.getTime());
        BigInteger orderId = new BigInteger(10, rnd);

        // todo 查询缓存

        // 工单插入数据库
        Order order = new Order();
        order.setStatus(status.ShenHeZhong);
        orderMapper.insertOrder(order);

        // 发送消息给负责人
        new ToLeaderMsg<Order>().send(uid, leaderId, order);

        // 记录此次操作,工单状态追踪
        OrderOp op = new OrderOp();
        OrderService.logger(op);

    }

    @Override
    public void leaderApprovePass(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息 todo 先查询缓存
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.getOrderInfo(orderId);
        if (order == null) {
            return;
        }

        // 工单状态变为待执行
        order.setStatus(status.ZhiXing);

        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 负责人id
        BigInteger uid = order.getUid();  // 报修用户id
        BigInteger SPid = order.getSPid(); // 服务商id
        String auditComments = json.get("suditComment").getAsString(); // 审核意见

        // 给报修用户发送消息
        new AuditResultMsg().send(leaderId, uid, order, auditComments);
        // 给服务提供商发送消息
        new ToLeaderMsg<Order>().send(leaderId, SPid, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);

    }

    @Override
    public void leaderApproveFail(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息 todo 先查询缓存
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.getOrderInfo(orderId);
        if (order == null) {
            return;
        }

        // 工单状态变为待执行 TODO 写入缓存
        order.setStatus(status.ZhiXing);
        OrderService.changeOrderStatus(order);

        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 负责人id
        BigInteger uid = order.getUid();  // 报修用户id
        String auditComments = json.get("suditComment").getAsString(); // 审核意见

        // 给报修用户发送消息
        new AuditResultMsg().send(leaderId, uid, order, auditComments);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "fail", auditComments);
        OrderService.logger(op);
    }

    @Override
    public void cancelTask(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息 todo 先查询缓存
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.getOrderInfo(orderId);
        if (order == null) {
            return;
        }

        // 工单状态变为待执行 TODO 写入缓存
        order.setStatus(status.QvXiao);
        OrderService.changeOrderStatus(status.QvXiao);

        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 负责人id
        BigInteger uid = order.getUid();  // 报修用户id
        BigInteger SPId = order.getSPId(); // 服务商id
        BigInteger workerId = order.getWorkerId(); // 维修工id

        // 发送消息
        new ToLeaderMsg<Order>().send(uid, leaderId, order);
        new ToLeaderMsg<Order>().send(uid, SPId, order);
        new ToLeaderMsg<Order>().send(uid, workerId, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "cancel", "success", "用户已取消该单据任务");
        OrderService.logger(op);
    }

    @Override
    public void serviceProviderReceiveTask(String data) {

    }

    @Override
    public void serviceProviderRejectTask(String data) {

    }

    @Override
    public void maintenanceWorkerReceiveTask(String data) {

    }

    @Override
    public void maintenanceWorkerRejectTask(String data) {

    }

    @Override
    public void maintenanceWorkerEnsureService(String data) {

    }

    @Override
    public void maintenanceWorkerExchangeTask(String data) {

    }

    @Override
    public void maintenanceWorkerApplyForDevices(String data) {

    }

    @Override
    public void ensureService(String data) {

    }

    @Override
    public void serviceProviderApproveBillPass(String data) {

    }

    @Override
    public void serviceProviderApproveBillFail(String data) {

    }

    @Override
    public void leaderApproveBillPass(String data) {

    }

    @Override
    public void leaderApproveBillFail(String data) {

    }

    @Override
    public void leaderEnsureAndPay(String data) {

    }

    @Override
    public void leaderRejectPay(String data) {

    }

    @Override
    public void evaluate(String data) {

    }
}
