package com.edu.bupt.repairs.service;

public interface TaskService {
    /**
     * 报修用户提交工单任务申请
     * @param data
     */
    String submitTask(String data) throws Exception;

    void leaderApprovePass(String data);

    void leaderApproveFail(String data);

    void cancelTask(String data);

    void serviceProviderReceiveTask(String data);

    void serviceProviderRejectTask(String data);

    void maintenanceWorkerReceiveTask(String data);

    void maintenanceWorkerRejectTask(String data);

    void maintenanceWorkerEnsureService(String data);

    void maintenanceWorkerExchangeTask(String data);

    void maintenanceWorkerApplyForDevices(String data);

    void ensureService(String data);

    void serviceProviderApproveBillPass(String data);

    void serviceProviderApproveBillFail(String data);

    void leaderApproveBillPass(String data);

    void leaderApproveBillFail(String data);

    void leaderEnsureAndPay(String data);

    void leaderRejectPay(String data);

    void evaluate(String data);
}
