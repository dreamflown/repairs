package com.edu.bupt.repairs.service.impl;

import com.edu.bupt.repairs.commom.OrderStatus;
import com.edu.bupt.repairs.dao.OrderMapper;
import com.edu.bupt.repairs.dao.ServerMapper;
import com.edu.bupt.repairs.dao.WorkerMapper;
import com.edu.bupt.repairs.model.*;
import com.edu.bupt.repairs.model.message.AuditResultMsg;
import com.edu.bupt.repairs.model.message.ToLeaderMsg;
import com.edu.bupt.repairs.service.OrderService;
import com.edu.bupt.repairs.service.TaskService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    WorkerMapper workerMapper;

    @Autowired
    ServerMapper serverMapper;

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
    public String  serviceProviderReceiveTask(String data) {

        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取维修工id
        BigInteger uid = json.get("uid").getAsBigInteger();
        User user = userService.getUserInfo(uid);
        if (user = null) {
            return;
        }

        Boolean serverTakeOrderResult=true;
        Order order=new Order();
        order.setServerTakeOrderResult(serverTakeOrderResult);

        Task task=new Task();
        task.setMaintainerId(uid);

        //存入数据库
        int result=serverMapper.insert1(task);
        if (result<1){
            throw new Exception(ErrorCodeEnum.MDC10021019);
        }

        // 工单状态变为待执行
        order.setStatus(status.ZhiXing);

        // 给用户发送消息
        BigInteger uid = order.getUid();  // 报修用户id
        BigInteger SPId = order.getSPId(); // 服务商id
        new ToLeaderMsg<Order>().send(uid, SPId, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);

        return"您已接单成功";

    }

    @Override
    public void serviceProviderRejectTask(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取服务商id
        BigInteger uid = json.get("uid").getAsBigInteger();
        User user = userService.getUserInfo(uid);
        if (user = null) {
            return;
        }

        Task task=new Task();

        task.setServerId(null);


        //从数据库删除服务商id
        int result=serverMapper.delete2(task);
        if(result<1){
            throw new Exception(ErrorCodeEnum.MDC10021019);
        }

        return "您已拒绝接单";

    }

    @Override
    public String maintenanceWorkerReceiveTask(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取维修工id
        BigInteger uid = json.get("uid").getAsBigInteger();
        User user = userService.getUserInfo(uid);
        if (user = null) {
            return;
        }

        Boolean workerTakeOrderResult=true;
        Order order=new Order();
        order.setWorkerTakeOrderResult(workerTakeOrderResult);

        // 工单状态变为待维修
        order.setStatus(status.WeiXiu);

        // 给用户发送消息
        BigInteger uid = order.getUid();  // 报修用户id
        BigInteger SPId = order.getSPId(); // 服务商id
        new ToLeaderMsg<Order>().send(uid, SPId, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);

        return "接单成功";

    }

    @Override
    public String maintenanceWorkerRejectTask(String data) {

        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取维修工id
        BigInteger uid = json.get("uid").getAsBigInteger();
        User user = userService.getUserInfo(uid);
        if (user = null) {
            return;
        }

        Boolean workerTakeOrderResult=false;
        Order order=new Order();
        order.setWorkerTakeOrderResult(workerTakeOrderResult);

        Task task=new Task();

        task.setMaintainerId(null);


        //从数据库删除维修工id
        int result=workerMapper.deleteMaintainerId(task);
        if(result<1){
            throw new Exception(ErrorCodeEnum.MDC10021019);
        }

        // 工单状态变为待执行
        order.setStatus(status.ZhiXing);

        // 给服务提供商发送消息
        BigInteger uid = order.getUid();  // 报修用户id
        BigInteger SPId = order.getSPId(); // 服务商id
        new ToLeaderMsg<Order>().send(uid, SPId, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);

        return "您已拒绝接单";

    }

    @Override
    public String maintenanceWorkerEnsureService(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取维修工id
        BigInteger uid = json.get("uid").getAsBigInteger();
        User user = userService.getUserInfo(uid);
        if (user = null) {
            return;
        }


        // 获取内容
        String suggestion = json.get("suggestion").getAsString();  // 维修工建议
        String result = json.get("result").getAsString(); // 维修结果

        TaskItem taskItem=new TaskItem();
        taskItem.setSuggestion(suggestion);
        taskItem.setResult(result);

        //存入数据库
        int result=workerMapper.insert3(taskItem);
        if (result<1){
            throw new Exception(ErrorCodeEnum.MDC10021019);
        }

        // 工单状态变为待验收
        order.setStatus(status.YanShou);

        // 给用户发送消息
        BigInteger SPId = order.getSPid(); // 服务商id
        BigInteger uid = order.getUid();  // 报修用户id

        new ToLeaderMsg<Order>().send(uid, SPId, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);

        return "你已确认服务完成";


    }

    @Override
    public void maintenanceWorkerExchangeTask(String data) {

    }

    @Override
    public void maintenanceWorkerApplyForDevices(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取task_id
        BigInteger task_id = json.get("task_id").getAsBigInteger();
        User user = userService.getUserInfo(task_id);


        // 存入数据库
        Task task=new Task();
        DeviceOrder deviceOrder=new DeviceOrder();
        BigInteger maintainer_id=workerMapper.selectByTaskItemId(task);
        int result=workerMapper.insert4(deviceOrder);//待完善
        if (result<1){
            throw Exception(ErrorCodeEnum.MDC10021019);
        }


        // 工单状态变为等待备件方案审核
        order.setStatus(status.LDShenHeZhangDan);


        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);



    }

    @Override
    public String ensureService(String data) {

        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取用户id
        BigInteger uid = json.get("uid").getAsBigInteger();
        User user = userService.getUserInfo(uid);
        if (user = null) {
            return;
        }

        // 工单状态变为待验收
        order.setStatus(status.YanShou);

        // 给负责人发送消息
        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 负责人id
        BigInteger uid = order.getUid();  // 报修用户id
        new ToLeaderMsg<Order>().send(uid, leaderId, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);



        return "您已确认服务完成";
    }

    @Override
    public void serviceProviderApproveBillPass(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取task_id
        BigInteger task_id = json.get("task_id").getAsBigInteger();

        Order order=new Order();

        order.setServerTakeOrderResult(true);

        // 工单状态变为待负责人审批账单
        order.setStatus(status.LDShenHeZhangDan);

        // 给负责人发送消息
        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 负责人id
        BigInteger SPid = order.getSPid(); // 服务商id
        String auditComments = json.get("suditComment").getAsString(); // 审核意见
        new ToLeaderMsg<Order>().send( leaderId,SPid, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);

        return "您已通过审批";

    }

    @Override
    public void serviceProviderApproveBillFail(String data) {

        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取device_id
        BigInteger deviceorder_id = json.get("deviceorder_id").getAsBigInteger();

        Order order=new Order();

        order.setServerApprovalPayResult(false);

        DeviceOrder deviceOrder;

        //数据库删除数据
        int result=serverMapper.delete4BYPrimaryId(deviceOrder);
        if (result<1){

            throw Exception(ErrorCodeEnum.MDC10021019);
        }
        // 工单状态变为维修中
        order.setStatus(status.WeiXiu);

        // 给负责人发送消息
        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 负责人id
        BigInteger uid = order.getUid();  // 报修用户id
        new ToLeaderMsg<Order>().send(uid, leaderId, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);



        return "您已驳回工单";

    }

    @Override
    public void leaderApproveBillPass(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取task_id
        BigInteger task_id = json.get("task_id").getAsBigInteger();

        Order order=new Order();


        // 工单状态变为待服务确认
        order.setStatus(status.QueRenFuWu);

        // 给报修用户发送消息
        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 负责人id
        String auditComments = json.get("suditComment").getAsString(); // 审核意见
        BigInteger uid = order.getUid();  // 报修用户id

        new AuditResultMsg().send(leaderId, uid, order, auditComments);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);

        return "您已通过账单审批";


    }

    @Override
    public void leaderApproveBillFail(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取device_id
        BigInteger deviceorder_id = json.get("deviceorder_id").getAsBigInteger();

        Order order=new Order();

        order.setServerApprovalPayResult(false);

        DeviceOrder deviceOrder;

        //数据库删除数据
        int result=serverMapper.delete4BYPrimaryId(deviceOrder);
        if (result<1){

            throw Exception(ErrorCodeEnum.MDC10021019);
        }
        // 工单状态变为维修中
        order.setStatus(status.WeiXiu);

        // 给服务商发送消息
        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 负责人id
        BigInteger SPId = order.getSPId(); // 服务商id
        new ToLeaderMsg<Order>().send(SPid, leaderId, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);



        return "您已驳回账单";

    }

    @Override
    public void leaderEnsureAndPay(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取甲方id
        BigInteger uid = json.get("uid").getAsBigInteger();
        User user = userService.getUserInfo(uid);
        if (user = null) {
            return;
        }

        // 工单状态变为待评价
        order.setStatus(status.PingJia);

        // 给用户发送消息
        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 负责人id
        BigInteger uid = order.getUid();  // 报修用户id
        new ToLeaderMsg<Order>().send(uid, leaderId, order);

        // 记录此次操作，工单状态追踪
        OrderOp op = new OrderOp(order, "approve", "pass", auditComments);
        OrderService.logger(op);



        return "您已确认支付";

    }

    @Override
    public void leaderRejectPay(String data) {

    }

    @Override
    public String evaluate(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取用户id
        BigInteger uid = json.get("uid").getAsBigInteger();
        User user = userService.getUserInfo(uid);
        if (user = null) {
            return;
        }


        // 获取评价
        int score = json.get("score").getAsInt();  // 服务评级
        String contents = json.get("contents").getAsString(); // 服务评论


        Review review=new Review();
        review.setUserId(uid);
        review.setContents(contents);
        review.setScore(score);

        //评价数据存入数据库

        int result=orderMapper.insert1(review);
        if (result<1){
            throw new Exception(ErrorCodeEnum.MDC10021019);
        }

        return "提交评级成功";
    }
}
