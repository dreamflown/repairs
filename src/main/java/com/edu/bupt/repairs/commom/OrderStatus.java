package com.edu.bupt.repairs.commom;

import org.springframework.stereotype.Component;

@Component
public class OrderStatus {
    public static final String QvXiao = "已取消";

    public static final String ShenQing = "维修申请";

    public static final String ShenHeZhong = "审核中";

    public static final String ZhiXing = "待执行";

    public static final String WeiXiu = "维修中";

    public static final String QueRenFuWu = "待确认服务";

    public static final String SPShenHeZhangDan = "待服务商审核账单";

    public static final String LDShenHeZhangDan = "待负责人审核账单";

    public static final String YanShou = "待验收";

    public static final String PingJia = "待评价";
}
