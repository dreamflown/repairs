package com.edu.bupt.repairs.controller;

import com.edu.bupt.repairs.commom.OrderStatus;
import com.edu.bupt.repairs.model.Order;
import com.edu.bupt.repairs.model.message.ResponseMsg;
import com.edu.bupt.repairs.model.message.ToLeaderMsg;
import com.edu.bupt.repairs.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Date;

@RestController
@RequestMapping(value = "/repairs/order")
public class RepairsController {

    @Autowired
    OrderService orderService;
    @Autowired
    ServerService serverService;
    @Autowired
    WorkerService workerService;
    @Autowired
    DeviceService deviceService;

    @Autowired
    TaskService taskService;

    @Autowired
    OrderStatus status;

    /* role:1.报修用户，2.服务商，3.维修工,4.甲方负责人*/
    /* caozuo: 确认，撤销，驳回 */

    @PostMapping("/")
    public ResponseMsg dispatch(@RequestParam int role,
                            @RequestParam BigInteger orderId,
                            @RequestParam String status_type,
                            @RequestParam String caozuo,
                            @RequestBody String data){

        try {
            switch (status_type) {
                case "维修申请":
                    if (role == 1 && caozuo.equals("确认") || role == 3 && caozuo.equals("确认")) {
                        // 报修用户确认提交工单
                        taskService.submitTask(data);
                        orderService.changeOrderStatus(orderId, "提交成功", "审核中");

                    } else {
                        System.out.println("用户不具有该权限");
                        return new ResponseMsg<String>(42001, "用户未被授权", null);
                    }
                    break;

                case "审核中":
                    if (role == 4 && caozuo.equals("确认")){
                        // 甲方负责人审核通过
                        taskService.leaderApprovePass(data);
                        orderService.changeOrderStatus(orderId, "审批通过", "待执行");

                    } else if (role == 4 && caozuo.equals("驳回")){
                        // 甲方负责人审核驳回
                        taskService.leaderApproveFail(data);
                        orderService.changeOrderStatus(orderId, "审批失败", "维修申请");

                    } else if (role == 1 && caozuo.equals("撤销") || role == 3 && caozuo.equals("撤销")) {
                        // 报修用户撤销提交工单
                        taskService.cancelTask(data);
                        orderService.changeOrderStatus(orderId, "撤销申请", "已取消");

                    } else {
                        System.out.println("用户不具有该权限");
                        return new ResponseMsg<String>(42001, "用户未被授权", null);
                    }
                    break;

                case "待接单":
                    if (role == 2 && caozuo.equals("确认")){
                        // 服务商接单
                        taskService.serviceProviderReceiveTask(data);
                        orderService.changeOrderStatus(orderId, "服务商接单", "待接单");

                    } else if (role == 2 && caozuo.equals("驳回")) {
                        // 服务商拒单
                        taskService.serviceProviderRejectTask(data);
                        orderService.changeOrderStatus(orderId, "服务商拒单", "待接单");

                    } else if (role == 1 && caozuo.equals("撤销")) {
                        // 报修用户取消工单
                        taskService.cancelTask(data);
                        orderService.changeOrderStatus(orderId, "撤销维修任务", "已取消");

                    } else {
                        System.out.println("用户不具有该权限");
                        return new ResponseMsg<String>(42001, "用户未被授权", null);
                    }
                    break;

                case "待执行":
                    if (role == 3 && caozuo.equals("接单")) {
                    // 维修工接单
                    taskService.maintenanceWorkerReceiveTask(data);
                    orderService.changeOrderStatus(orderId, "维修工接单", "维修中");

                } else if (role == 3 && caozuo.equals("驳回")) {
                    // 维修工拒单，转单
                    taskService.maintenanceWorkerRejectTask(data);
                    orderService.changeOrderStatus(orderId, "维修工拒单", "待执行");

                } else if (role == 1 && caozuo.equals("撤销")) {
                    // 报修用户取消工单
                    taskService.cancelTask(data);
                    orderService.changeOrderStatus(orderId, "撤销维修任务", "已取消");

                } else {
                    System.out.println("用户不具有该权限");
                    return new ResponseMsg<String>(42001, "用户未被授权", null);
                }

                case "维修中":
                    if (role == 3 && caozuo.equals("确认")) {
                        // 维修工确认维修完成
                        taskService.maintenanceWorkerEnsureService(data);
                        orderService.changeOrderStatus(orderId, "维修工确认服务完成", "待确认服务");

                    } else if(role == 3 && caozuo.equals("转单")) {
                        // 维修工转单
                        taskService.maintenanceWorkerExchangeTask(data);
                        orderService.changeOrderStatus(orderId, "维修工转单", "待执行");

                    } else if(role == 3 && caozuo.equals("申请")) {
                        // 维修工申请备件更换
                        taskService.maintenanceWorkerApplyForDevices(data);
                        orderService.changeOrderStatus(orderId, "等待备件方案审核", "待服务商审核账单");

                    } else {
                        System.out.println("用户不具有该权限");
                        return new ResponseMsg<String>(42001, "用户未被授权", null);
                    }
                    break;

                case "待确认服务":
                    if (role == 1 && caozuo.equals("确认")) {
                        // 报修用户确认服务完成
                        taskService.ensureService(data);
                        orderService.changeOrderStatus(orderId,"用户确认服务", "待验收");

                    } else if (role == 1 && caozuo.equals("撤销") || role == 3 && caozuo.equals("撤销")) {
                        // 报修用户撤销提交工单
                        taskService.cancelTask(data);
                        orderService.changeOrderStatus(orderId, "撤销申请", "已取消");

                    } else {
                        System.out.println("用户不具有该权限");
                        return new ResponseMsg<String>(42001, "用户未被授权", null);
                    }
                    break;

                case "待服务商审核账单":
                    if (role == 2 && caozuo.equals("确认")) {
                        // 服务商审核账单通过
                        taskService.serviceProviderApproveBillPass(data);
                        orderService.changeOrderStatus(orderId, "服务商审核账单通过", "待负责人审核账单");

                    } else if (role == 2 && caozuo.equals("驳回")) {
                        // 服务商审核账单驳回
                        taskService.serviceProviderApproveBillFail(data);
                        orderService.changeOrderStatus(orderId, "服务商审核账单驳回", "维修中");

                    } else {
                        System.out.println("用户不具有该权限");
                        return new ResponseMsg<String>(42001, "用户未被授权", null);
                    }
                    break;

                case "待负责人审核账单":
                    if (role == 4 && caozuo.equals("确认")) {
                        // 负责人审核账单通过
                        taskService.leaderApproveBillPass(data);
                        orderService.changeOrderStatus(orderId, "负责人审核账单通过", "待确认服务");

                    } else if (role == 4 && caozuo.equals("驳回")) {
                        // 负责人审核账单驳回
                        taskService.leaderApproveBillFail(data);
                        orderService.changeOrderStatus(orderId, "负责人审核账单驳回", "待服务商审核账单");

                    } else {
                        System.out.println("用户不具有该权限");
                        return new ResponseMsg<String>(42001, "用户未被授权", null);
                    }
                    break;

                case "待验收":
                    if (role == 4 && caozuo.equals("确认")) {
                        // 负责人验收服务，并支付
                        taskService.leaderEnsureAndPay(data);
                        orderService.changeOrderStatus(orderId, "负责人已验收", "待评价");

                    } else if (role == 4 && caozuo.equals("取消")) {
                        // 负责人验收服务，不通过
                        taskService.leaderRejectPay(data);
                        orderService.changeOrderStatus(orderId, "负责人拒绝支付", "已取消");
                    } else {
                        System.out.println("用户不具有该权限");
                        return new ResponseMsg<String>(42001, "用户未被授权", null);
                    }
                    break;

                case "待评价":
                    if (role == 1 && caozuo.equals("评价")) {
                        // 报修用户评价服务
                        taskService.evaluate(data);
                        orderService.changeOrderStatus(orderId, "用户完成评价", "已完成");

                    } else {
                        System.out.println("用户不具有该权限");
                        return new ResponseMsg<String>(42001, "用户未被授权", null);
                    }
                    break;

                default:
                    System.out.println("该用户身份未识别");
                    return new ResponseMsg<String>(42001, "用户未被授权", null);

            }
            return new ResponseMsg<String>(0, "操作成功", null);

        } catch (NullPointerException e) {
            e.printStackTrace();
            return new ResponseMsg<String>(41004,"参数为空", null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseMsg<String>(41003, "非法参数", null);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return new ResponseMsg<String>(41003, "参数格式错误", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseMsg<String>(1, "未知异常", null);
        }



        if (role == 1) {
            if (status_type=="维修申请"){
                if (caozuo=="发起维修申请"){
                    BigInteger id;
                    BigInteger user_id;
                    int status;
                    int status_type;
                    orderService.addWeiXiuShenQing(id,user_id,status,status_type);
                    orderService.addOrder();
                }
            }
            else if (status_type == "生成工单") {
                if (caozuo == "提交工单") {
                    BigInteger user_id;
                    BigInteger maintainer_id;
                    Date estimated_time;
                    Date deadline;
                    BigInteger device_id;
                    int level;
                    DecimalFormat request_latitude;
                    DecimalFormat request_longitude;
                    String description;
                    String enclosure_url;
                    BigInteger facilitator_id;
                    orderService.addOrder(user_id,maintainer_id,estimated_time,deadline,device_id,level,request_latitude,
                    request_longitude,description,enclosure_url);
                    /*调用审批模块*/
                    serverService.takeOrder();
                }

            } else if (status_type == "服务确认") {
                if (caozuo == "确认服务完成") {
                    BigInteger user_id;
                    Date finished_time;
                    orderService.QueRenFuWu(user_id,finished_time);
                    /*调用支付模块*/
                    orderService.addPingJia();
                }
            } else if (status_type == "提交备件更换方案") {
                if (caozuo == "提交备件方案") {
                    BigInteger id;
                    BigInteger task_id;
                    String device_type;
                    int count;
                    Date start_time;
                    DecimalFormat cost;
                    orderService.addBeiJianGengHuanFangAn(id,task_id,device_type,
                    count,start_time,cost);
                    /*调用审批模块*/
                    deviceService.addBeiXuanBeiJian();

                }
            } else if (status_type == "服务评价") {
                if (caozuo == "提交评价") {
                    BigInteger id;
                    int score;
                    String contents;
                    orderService.addPingJia(id,score,contents);
                }
            }
            else if (status_type=="选择服务商"){
                if (caozuo=="选择服务商"){
                    BigInteger user_id;
                    BigInteger facilitator;
                    orderService.selectFacilitator(user_id,facilitator);
                }
            }
        }

        else if (role==2){
            if (status_type=="接单"){
                if (caozuo=="确认接单"){
                    BigInteger facilitator_id;
                    BigInteger maintainer_id;
                    serverService.takeOrder(facilitator_id);
                    workerService.takeOrder(maintainer_id;);
                }
                else if (caozuo=="拒绝接单"){
                    BigInteger facilitator_id;
                    BigInteger user_id;
                    serverService.rejectTakeOrder(facilitator_id);
                    orderService.selectFacilitator();
                }
            }
            if (status_type=="审批备件金额"){
                    serverService.ShenPiBeiJianJinE();
                    /*调用审批模块*/
            }
            }
        else if (role==3){
            if (status_type=="接单"){
                if (caozuo=="确认接单"){
                    BigInteger maintainer_id;
                    workerService.takeOrder(maintainer_id);
                }
                else if (caozuo=="拒绝接单"){
                    BigInteger maintainer_id;
                    workerService.rejectTakeOrder(maintainer_id);
                    serverService.FenPeiMaintainer(maintainer_id);}
            }
            else if (status_type=="服务确认"){
                if (caozuo=="确认服务完成"){
                    String suggestion;
                    String result;
                    DecimalFormat total_cost;
                    workerService.QueRenFuWu(suggestion,result,total_cost);
                    orderService.QueRenFuWu();
                }}
            }
            else if (status_type=="提交备件更换申请"){
                if (caozuo=="提交备件更换申请"){
                    workerService.TiJiaoBeiJianGengHuanShenQing();
                    serverService.ShenPiBeiJianJinE();
            }



        }
        else if (role==4){
            if (status_type=="提供备件"){
                if (caozuo=="提供设备信息"){
                    BigInteger id;
                    String device_model;
                    String device_type;
                    int count;
                    DecimalFormat cost;
                    deviceService.addBeiXuanBeiJian(id,device_model,device_type,count,cost);
                    workerService.QueRenFuWu();

                    }

            }
        }

    }

}

