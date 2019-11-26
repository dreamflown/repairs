package com.edu.bupt.repairs.dao;

import com.edu.bupt.repairs.model.*;

public interface ServerMapper {

    /*每个状态都要：*/

    int update(Task task);/*更新status*/

    int update(TaskLog taskLog);/*更新task_type,status_timestamp*/

    /*1.确认接单*/

    /*2.拒绝接单*/

    int update2(Task task);/*更新facilitator_id*/

    int update2(TaskLog taskLog);/*更新facilitator_id*/

    /*3.分配维修工*/

    int update3(Task task);/*更新maintainer_id*/

    int update3(TaskLog taskLog);/*更新maintainer_id*/
}
