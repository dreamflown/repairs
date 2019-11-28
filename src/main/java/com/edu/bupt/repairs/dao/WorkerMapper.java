package com.edu.bupt.repairs.dao;

import com.edu.bupt.repairs.model.DeviceOrder;
import com.edu.bupt.repairs.model.Task;
import com.edu.bupt.repairs.model.TaskItem;
import com.edu.bupt.repairs.model.TaskLog;

import java.math.BigInteger;

public interface WorkerMapper {
    /*每个状态都要：*/

    int update(Task task);/*更新status*/

    int update(TaskLog taskLog);/*更新task_type,status_timestamp*/

    /*1.确认接单*/

    /*2.拒绝接单*/



    int deleteMaintainerId(Task task);/*删除maintainerID*/

    /*3.确认服务完成*/
    int insert3(TaskItem taskItem);/*插入suggestion,result*/

    /*4.提交备件申请*/

    BigInteger selectByTaskItemId(Task task);/*选出maintainer_id*/
    int insert4(DeviceOrder deviceOrder);/*插入task_id,maintainer_id,device_id*/


}