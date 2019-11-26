package com.edu.bupt.repairs.dao;

import com.edu.bupt.repairs.model.Device;
import com.edu.bupt.repairs.model.Task;
import com.edu.bupt.repairs.model.TaskLog;

public interface DeviceMapper {
    /*每个状态都要：*/

    int update(Task task);/*更新status*/

    int update(TaskLog taskLog);/*更新task_type,status_timestamp*/

    /*1.提供备选备件*/

    int insert1(Device device);/*插入id, device_model, device_type,count,cost*/
}
