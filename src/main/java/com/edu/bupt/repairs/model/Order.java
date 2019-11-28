package com.edu.bupt.repairs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    // 工单id
    private BigInteger id;
    // 工单状态
    private String status;
    // 合同id
    private BigInteger contractId;
    // 报修用户id
    private BigInteger uId;
    // 审核人id
    private BigInteger leaderId;
    // 服务提供商id
    private BigInteger ServiceProviderId;
    // 任务子项列表
    private List<Task> taskList;
    // 备件更换单号
    private BigInteger deviceApply;
    // 总维修费用
    private Float totalCost;
    // 工单创建时间
    private Timestamp createTime;
    // 计划完成时间
    private Timestamp ddl;
    // 维修结果
    private Boolean result;
    // 维修记录
    private String record;
    // 评价
    private BigInteger reviewId;

}
