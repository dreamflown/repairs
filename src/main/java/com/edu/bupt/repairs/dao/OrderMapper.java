package com.edu.bupt.repairs.dao;


import com.edu.bupt.repairs.model.*;


public interface OrderMapper {

    /*每个状态都要：*/

    int update(Task task);/*更新status*/

    int update(TaskLog taskLog);/*更新task_type,status_timestamp*/

    /*1.发起维修申请*/

    /*从合同模块根据用户id查出facilitator_id,clearing_form*/

    int insert1(Task task);/*插入id，user_id，status,facilitator_id,clearing_form*/

    int insert1(TaskItem taskItem);/*插入task_id*/

    int insert1(TaskLog taskLog);/*插入user_id,task_type,task_id,facilitator_id,status_timestamp*/

    int insert1(Review review);/*插入task_id*/


    /*2.生成工单*/

    int insert2(Task task);/*插入user_id, maintainer_id,estimated_time,deadline,device_id,level,request_latitude, request_longitude,description,enclosure_url*/

    int insert2(TaskItem taskItem);/*插入device_id*/

    int insert2(TaskLog taskLog);/*插入user_id,maintainer_id*/

    int insert2(Review review);/*插入user_id*/

    /**
     * 3.确认服务完成

     * @return Order
     */
    int insert3(TaskItem taskItem);/*插入finished_time*/

    /**
     * 4.提交备件更换方案
     * @return Order
     */

    int insert4(DeviceOrder deviceOrder);/*插入id,task_id,device_type,count,start_time,cost*/

    /**
     * 5.提交评价
     * @return Order
     */
    int insert5(Review review);/*插入id,score,contents*/

    /**
     * 6.选择服务商
     * @return Order
     */
    int update6(Task task);/*更新facilitator_id*/

    int update6(TaskLog taskLog);/*更新facilitator_id*/

}
