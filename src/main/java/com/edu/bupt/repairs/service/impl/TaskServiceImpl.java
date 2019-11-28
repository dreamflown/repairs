package com.edu.bupt.repairs.service.impl;

import com.edu.bupt.repairs.commom.OrderStatus;
import com.edu.bupt.repairs.model.*;
import com.edu.bupt.repairs.model.message.AuditResultMsg;
import com.edu.bupt.repairs.model.message.ToLeaderMsg;
import com.edu.bupt.repairs.service.OperationService;
import com.edu.bupt.repairs.service.OrderService;
import com.edu.bupt.repairs.service.TaskService;
import com.edu.bupt.repairs.service.UserService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.istack.internal.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    OrderStatus status;

    @Autowired
    UserService userService;

    @Autowired
    OrderService orderService;

    @Autowired
    OperationService opService;

    @Override
    public String submitTask(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取报修用户信息
        @NotNull
        BigInteger uId = json.get("uId").getAsBigInteger();
        User user = userService.getUserInfo(uId);
        if (user == null) {
            return "您的账号信息已过期，请重新注册";
        }

        String name = user.getName();
        String phone = user.getPhone();
        String company = user.getCompany();

        BigInteger leaderId = json.get("leaderId").getAsBigInteger();                   // 审核人id
        BigInteger serviceProviderId = json.get("serviceProviderId").getAsBigInteger(); // 服务商id

        // 自动生成时间信息
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Timestamp createTime = new Timestamp(cal.getTime().getTime()); // 报修时间
//        cal.add(Calendar.DAY_OF_WEEK, 1); //增加一周
//        Timestamp ddl = new Timestamp(cal.getTime().getTime()); // 计划完成时间

        // 生成工单编号
        Random rnd = new Random();
        rnd.setSeed(createTime.getTime());
        BigInteger orderId = new BigInteger(10, rnd);

        // 新建工单并插入数据库
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(status.ShenQing);
        order.setUId(uId);
        order.setLeaderId(leaderId);
        order.setServiceProviderId(serviceProviderId);
        order.setCreateTime(createTime);
        orderService.addOrder(order);

        // 新建维修任务
        JsonArray tasks = json.get("tasks").getAsJsonArray();
        List<Task> taskList = null;
        for (int i=0; i<tasks.size(); i++) {
            JsonObject taskJson = (JsonObject) tasks.get(i);
            String deviceName = taskJson.get("deviceName").getAsString();                       // 设备名称
            String deviceAddress = taskJson.get("deviceAddress").getAsString();                 // 设备地址
            String troubleType = taskJson.get("troubleType").getAsString();                     // 故障类型
            String troubleInfo = taskJson.get("troubleInfo").getAsString();                     // 故障信息
            float laborCost = taskJson.get("laborCost").getAsFloat();                           // 人工费用
            String imageUtl = taskJson.get("imageUtl").getAsString();                           // 图片
            String videoUrl = taskJson.get("videoUrl").getAsString();                           // 视频
            String audioUrl = taskJson.get("audioUrl").getAsString();                           // 音频

            Task task = new Task();
            task.setDeviceName(deviceName);
            task.setDeviceAddress(deviceAddress);
            task.setTroubleType(troubleType);
            task.setTroubleInfo(troubleInfo);
            task.setLaborCost(laborCost);
            task.setImageUtl(imageUtl);
            task.setVideoUrl(videoUrl);
            task.setAudioUrl(audioUrl);
            taskList.add(task);
        }

        // 发送消息给负责人
        new ToLeaderMsg<Order>().send(uId, leaderId, order);

        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(uId);
        op.setOrderId(orderId);
        op.setInfo("提交维修任务申请");
        opService.logger(op);

        return "success";
    }

    @Override
    public void leaderApprovePass(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息，工单状态变为待执行
        @NotNull
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.JieDan);
        if (order == null) {
            return;
        }

        BigInteger leaderId = json.get("leaderId").getAsBigInteger();       // 负责人id
        BigInteger uId = order.getUId();                                    // 报修用户id
        BigInteger SPid = order.getServiceProviderId();                     // 服务商id
        String auditComments = json.get("auditComment").getAsString();      // 审核意见

        // 给报修用户发送消息
        new AuditResultMsg().send(leaderId, uId, order, auditComments);
        // 给服务提供商发送消息
        new ToLeaderMsg<Order>().send(leaderId, SPid, order);

        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(leaderId);
        op.setOrderId(orderId);
        op.setInfo("维修任务审批通过");
        opService.logger(op);

    }

    @Override
    public void leaderApproveFail(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息, 工单状态退回申请中
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.ShenQing);
        if (order == null) {
            return;
        }

        BigInteger leaderId = json.get("leaderId").getAsBigInteger();   // 负责人id
        BigInteger uId = order.getUId();                                // 报修用户id
        String auditComments = json.get("auditComment").getAsString();  // 审核意见

        // 给报修用户发送消息
        new AuditResultMsg().send(leaderId, uId, order, auditComments);

        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(leaderId);
        op.setOrderId(orderId);
        op.setInfo("维修任务审批驳回");
        opService.logger(op);
    }

    @Override
    public void cancelTask(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息, 工单状态变为已取消
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.QvXiao);
        if (order == null) {
            return;
        }

        BigInteger uId = order.getUId();                                // 报修用户id
        // 给审核人发消息
        BigInteger leaderId = json.get("leaderId").getAsBigInteger();   // 负责人id
        new ToLeaderMsg<Order>().send(leaderId, leaderId, order);
        // 报修用户发消息
        new ToLeaderMsg<Order>().send(uId, uId, order);
        // 给服务提供商发送消息
        BigInteger SPId = order.getServiceProviderId();                 // 服务商id
        new ToLeaderMsg<Order>().send(leaderId, SPId, order);
        // 给维修工发送消息
        for (Task task:order.getTaskList()){
            BigInteger workerId = task.getWorkerId();                   // 维修工id
            new ToLeaderMsg<Order>().send(uId, workerId, order);
        }

        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(uId);
        op.setOrderId(orderId);
        op.setInfo("已取消维修任务");
        opService.logger(op);

    }

    @Override
    public void serviceProviderReceiveTask(String data) {

        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

//        Boolean serverTakeOrderResult=true;
//        Order order=new Order();
//        order.setServerTakeOrderResult(serverTakeOrderResult);
//
//        Task task=new Task();
//        task.setMaintainerId(uId);
//
//        //存入数据库
//        int result=serverMapper.insert1(task);
//        if (result<1){
//            throw new Exception(ErrorCodeEnum.MDC10021019);
//        }

        // 获取工单信息
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.getOrderInfo(orderId);
        if (order == null) {
            return;
        }

        // 派发任务:根据服务提供商和报修单位签订的合同，以及履行改合同义务的维修列表，指派维修工
        BigInteger SPId = json.get("SPId").getAsBigInteger();
        BigInteger leaderId = json.get("leaderId").getAsBigInteger();
        BigInteger workerId = dispatchTask(SPId, leaderId, orderId);  // TODO 派发任务
        if (workerId == null) {
            System.out.println("维修工正忙，请联系服务提供商更换维修工");
        }
        for (Task task: order.getTaskList()){
            task.setWorkerId(workerId);
        }

        // 工单状态变为待执行
        order = orderService.changeOrderStatus(orderId, status.ZhiXing);

        // 给用户发送消息
        BigInteger uId = order.getUId();  // 报修用户id
        new ToLeaderMsg<Order>().send(SPId, uId, order);
        // 给维修工发消息
        new ToLeaderMsg<Order>().send(SPId, workerId, order);

        // 记录此次操作，工单状态追踪
        Operation op = new Operation();
        op.setOperator(SPId);
        op.setOrderId(orderId);
        op.setInfo("服务提供商已接单");
        opService.logger(op);

    }

    @Override
    public void serviceProviderRejectTask(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息，工单状态退回申请
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.ShenQing);
        if (order == null) {
            return;
        }

        // 通知报修用户
        BigInteger SPId = json.get("SPId").getAsBigInteger();
        BigInteger uId = order.getUId();
        new AuditResultMsg().send(SPId, uId, order,"服务提供商正忙，请稍后重新申请，或与服务提供商联系");

        // 记录此次操作，工单状态追踪
        Operation op = new Operation();
        op.setOperator(SPId);
        op.setOrderId(orderId);
        op.setInfo("服务提供商正忙，请稍后重新申请，或与服务提供商联系");
        opService.logger(op);

    }

    @Override
    public void maintenanceWorkerReceiveTask(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息，工单状态改为维修中
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.WeiXiu);
        if (order == null) {
            return;
        }

        // 通知报修用户
        BigInteger workerId = json.get("workerId").getAsBigInteger();
        BigInteger uId = order.getUId();
        new AuditResultMsg().send(workerId, uId, order,"维修工已接单，请等待维修工联系");
        // 通知服务提供商
        BigInteger SPId = json.get("SPId").getAsBigInteger();
        new AuditResultMsg().send(workerId, SPId, order,"维修工已接单");

        // 记录此次操作，工单状态追踪
        Operation op = new Operation();
        op.setOperator(workerId);
        op.setOrderId(orderId);
        op.setInfo("维修工已接单，请等待维修工联系");
        opService.logger(op);

    }

    @Override
    public void maintenanceWorkerRejectTask(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息，工单状态保持待执行
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.getOrderInfo(orderId);
        if (order == null) {
            return;
        }

        // 重新分派维修工
        BigInteger SPId = json.get("SPId").getAsBigInteger();
        BigInteger leaderId = json.get("leaderId").getAsBigInteger();
        BigInteger workerId = dispatchTask(SPId, leaderId, orderId);  // TODO 派发任务
        if (workerId == null) {
            System.out.println("维修工正忙，请联系服务提供商更换维修工");
        }
        for (Task task: order.getTaskList()){
            task.setWorkerId(workerId);
        }

        // 工单状态变为待执行
        order = orderService.changeOrderStatus(orderId, status.ZhiXing);

        // 通知报修用户
        BigInteger uId = order.getUId();
        new AuditResultMsg().send(workerId, uId, order,"维修工正忙，当前转单中");

        // 记录此次操作，工单状态追踪
        Operation op = new Operation();
        op.setOperator(workerId);
        op.setOrderId(orderId);
        op.setInfo("维修工正忙，当前转单中");
        opService.logger(op);

    }

    @Override
    public void maintenanceWorkerEnsureService(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息，工单状态改为待确认
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.getOrderInfo(orderId);
        if (order == null) {
            return;
        }

        BigInteger taskId = json.get("taskId").getAsBigInteger();
        Boolean result = json.get("result").getAsBoolean(); // 维修结果
        String record = json.get("record").getAsString();   // 维修记录
        order.setResult(result);
        order.setRecord(record);

        // 工单状态改为待报修用户确认服务
        order = orderService.changeOrderStatus(orderId, status.QueRenFuWu);

        // 通知报修用户
        BigInteger uId = order.getUId();
        BigInteger workerId = order.getTaskList().get(0).getWorkerId();
        new AuditResultMsg().send(workerId, uId, order,"维修服务已完成，请您及时确认");

        // 记录此次操作，工单状态追踪
        Operation op = new Operation();
        op.setOperator(workerId);
        op.setOrderId(orderId);
        op.setInfo("维修服务已完成，等待报修人确认");
        opService.logger(op);


    }

    @Override
    public void maintenanceWorkerExchangeTask(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息，工单状态保持待执行
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.getOrderInfo(orderId);
        if (order == null) {
            return;
        }

        // 重新分派维修工
        BigInteger SPId = json.get("SPId").getAsBigInteger();
        BigInteger leaderId = json.get("leaderId").getAsBigInteger();
        BigInteger workerId = dispatchTask(SPId, leaderId, orderId);  // TODO 派发任务
        if (workerId == null) {
            System.out.println("维修工正忙，请联系服务提供商更换维修工");
        }
        for (Task task: order.getTaskList()){
            task.setWorkerId(workerId);
        }

        // 工单状态变为待执行
        order = orderService.changeOrderStatus(orderId, status.ZhiXing);

        // 通知报修用户
        BigInteger uId = order.getUId();
        new AuditResultMsg().send(workerId, uId, order,"维修工取消任务，等待转单中");

        // 记录此次操作，工单状态追踪
        Operation op = new Operation();
        op.setOperator(workerId);
        op.setOrderId(orderId);
        op.setInfo("维修工取消任务，当前等待转单中");
        opService.logger(op);
    }

    @Override
    public void maintenanceWorkerApplyForDevices(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.getOrderInfo(orderId);
        if (order == null) {
            return;
        }

        // 获取备件申请信息
        DeviceOrder deviceOrder = new DeviceOrder();
        deviceOrder.setOrderId(orderId);

        JsonArray deviceArray = json.get("devices").getAsJsonArray();
        List<Device> devices = null;
        float totalCost = 0;
        for (int i=0; i< deviceArray.size(); i++) {
            JsonObject deviceJson = (JsonObject) deviceArray.get(i);
            String serial = deviceJson.get("serial").getAsString(); // 设备编号
            String name = deviceJson.get("name").getAsString(); // 设备编号
            String manufacturer = deviceJson.get("manufacturer").getAsString(); // 设备编号
            String model = deviceJson.get("model").getAsString(); // 设备编号
            float cost = deviceJson.get("cost").getAsFloat(); // 设备编号
            totalCost += cost;
            devices.add(new Device(serial, name, manufacturer, model, cost));
        }
        deviceOrder.setDevices(devices);
        deviceOrder.setTotalCost(totalCost);

        deviceOrderService.saveDeviceOrder(deviceOrder);

        order = orderService.changeOrderStatus(orderId, status.SPShenHeZhangDan);

        // 通知报修用户
        BigInteger workerId = json.get("workerId").getAsBigInteger();
        BigInteger uId = order.getUId();
        new AuditResultMsg().send(workerId, uId, order, "提交设备订单申请，服务提供商正在审核中");
        BigInteger SPId = order.getServiceProviderId();
        new AuditResultMsg().send(workerId, SPId, order, "收到一份新的设备订单申请，请及时审核");

        // 记录此次操作，工单状态追踪
        Operation op = new Operation();
        op.setOperator(workerId);
        op.setOrderId(orderId);
        op.setInfo("提交设备订单申请，服务提供商正在审核中");
        opService.logger(op);

    }

    @Override
    public void ensureService(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息, 工单状态变为待验收
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.YanShou);
        if (order == null) {
            return;
        }

        BigInteger uId = order.getUId();                                // 报修用户id
        // 给审核人发消息
        BigInteger leaderId = json.get("leaderId").getAsBigInteger();   // 负责人id
        new AuditResultMsg().send(uId, leaderId, order, "维修服务确认完成，请您及时支付");
        // 给维修工发送消息
        BigInteger workerId = order.getTaskList().get(0).getWorkerId();
        new AuditResultMsg().send(uId, leaderId, order, "用户确认维修服务完成");

        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(uId);
        op.setOrderId(orderId);
        op.setInfo("已确认维修服务完成");
        opService.logger(op);
    }

    @Override
    public void serviceProviderApproveBillPass(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息, 工单状态变为待审核人确认账单
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.LDShenHeZhangDan);
        if (order == null) {
            return;
        }

        BigInteger deviceOrderId = json.get("deviceOrderId").getAsBigInteger();
        DeviceOrder deviceOrder = deviceOrderService.getDeviceOrderInfo(deviceOrderId);
        if (deviceOrder == null) {
            return;
        }
        JsonArray deviceArray = json.get("devices").getAsJsonArray();
        float discount = json.get("discount").getAsFloat();
        if (discount-0 < 1.0e-6) {
            discount = 1;
        }
        float totalCost = 0;
        for (int i=0; i<deviceArray.size(); i++) {
            JsonObject deviceJson = (JsonObject)deviceArray.get(0);
            float cost = deviceJson.get("cost").getAsFloat();
            totalCost += cost;
        }
        totalCost *= discount;
        deviceOrder.setTotalCost(totalCost);
        deviceOrder.saveDeviceOrder(deviceOrder);

        // 给负责人发送消息
        BigInteger leaderId = json.get("leaderId").getAsBigInteger(); // 负责人id
        BigInteger SPId = order.getServiceProviderId(); // 服务商id
        BigInteger workerId = order.getTaskList().get(0).getWorkerId();
        String auditComments = json.get("auditComment").getAsString(); // 审核意见
        new AuditResultMsg().send(SPId, workerId, order, auditComments);
        new AuditResultMsg().send(SPId, leaderId, order, "您有新的账单等待审核，请及时查阅");


        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(SPId);
        op.setOrderId(orderId);
        op.setInfo("服务提供商审核备件更换申请通过，审核人审核中");
        opService.logger(op);

    }

    @Override
    public void serviceProviderApproveBillFail(String data) {

        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息, 工单状态变为维修中
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.WeiXiu);
        if (order == null) {
            return;
        }

        // 给负责人发送消息
        BigInteger SPId = order.getServiceProviderId(); // 服务商id
        BigInteger workerId = order.getTaskList().get(0).getWorkerId();
        String auditComments = json.get("auditComment").getAsString(); // 审核意见
        new AuditResultMsg().send(SPId, workerId, order, auditComments);


        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(SPId);
        op.setOrderId(orderId);
        op.setInfo("备件更换申请被驳回，请重新提交");
        opService.logger(op);

    }

    @Override
    public void leaderApproveBillPass(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息, 工单状态变为维修中
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.WeiXiu);
        if (order == null) {
            return;
        }

        BigInteger SPId = order.getServiceProviderId(); // 服务商id
        BigInteger workerId = order.getTaskList().get(0).getWorkerId();
        BigInteger leaderId = order.getLeaderId();
        String auditComments = json.get("auditComment").getAsString(); // 审核意见
        // 通知服务商
        new AuditResultMsg().send(leaderId, SPId, order, auditComments);
        // 通知维修工
        new AuditResultMsg().send(SPId, workerId, order, auditComments);

        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(SPId);
        op.setOrderId(orderId);
        op.setInfo("审核人通过备件更换申请，请开始维修");
        opService.logger(op);

    }

    @Override
    public void leaderApproveBillFail(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息, 工单状态变维修中
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.WeiXiu);
        if (order == null) {
            return;
        }

        BigInteger SPId = order.getServiceProviderId(); // 服务商id
        BigInteger workerId = order.getTaskList().get(0).getWorkerId();
        BigInteger leaderId = order.getLeaderId();
        String auditComments = json.get("auditComment").getAsString(); // 审核意见
        // 通知服务商
        new AuditResultMsg().send(leaderId, SPId, order, auditComments);
        // 通知维修工
        new AuditResultMsg().send(SPId, workerId, order, auditComments);

        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(SPId);
        op.setOrderId(orderId);
        op.setInfo("审核人驳回备件更换申请，请重新提交");
        opService.logger(op);
    }

    @Override
    public void leaderEnsureAndPay(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息, 工单状态变成待评价
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.PingJia);
        if (order == null) {
            return;
        }

        BigInteger SPId = order.getServiceProviderId(); // 服务商id
        BigInteger workerId = order.getTaskList().get(0).getWorkerId();
        BigInteger leaderId = order.getLeaderId();
        BigInteger uId = order.getUId();
        // 通知服务商
        new AuditResultMsg().send(leaderId, SPId, order, "维修服务已支付");
        // 通知维修工
        new AuditResultMsg().send(SPId, workerId, order, "维修服务已支付");
        // 通知报修用户
        new AuditResultMsg().send(SPId, uId, order, "维修服务已支付,请及时评价");

        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(leaderId);
        op.setOrderId(orderId);
        op.setInfo("维修服务已支付");
        opService.logger(op);
    }

    @Override
    public void leaderRejectPay(String data) {
        // todo
    }

    @Override
    public void evaluate(String data) {
        JsonObject json = new JsonParser().parse(data).getAsJsonObject();

        // 获取工单信息, 工单状态变成已完成
        BigInteger orderId = json.get("orderId").getAsBigInteger();
        Order order = orderService.changeOrderStatus(orderId, status.WanCheng);
        if (order == null) {
            return;
        }

        BigInteger uId = order.getUId();
        int score = json.get("score").getAsInt();
        String content = json.get("content").getAsString();
        Review review = new Review();
        review.setUserId(uId);
        review.setScore(score);
        review.setContents(content);
        review = reviewService.saveReview(review);
        order.setReviewId(review.getId());

        BigInteger uId = order.getUId();

        // 记录此次操作,工单状态追踪
        Operation op = new Operation();
        op.setOperator(uId);
        op.setOrderId(orderId);
        op.setInfo("维修服务已完成");
        opService.logger(op);
    }
}
