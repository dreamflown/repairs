package com.edu.bupt.repairs.dao;

import com.edu.bupt.repairs.model.Task;
import com.edu.bupt.repairs.model.TaskLog;

public interface ServerMapper {

    /*每个状态都要：*/

    int update(Task task);/*更新status*/

    int update(TaskLog taskLog);/*更新task_type,status_timestamp*/

    /*1.确认接单*/
    int insert1(Task task);/*插入maintainer_id*/

    /*2.拒绝接单*/

    int delete2(Task task);/*删除facilitator_id*/

    int delete2(TaskLog taskLog);/*删除facilitator_id*/

    /*3.分配维修工*/

    int update3(Task task);/*更新maintainer_id*/

    int update3(TaskLog taskLog);/*更新maintainer_id*/
}

