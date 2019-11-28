package com.edu.bupt.repairs.service.impl;

import com.edu.bupt.repairs.model.Operation;
import com.edu.bupt.repairs.service.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationServiceImpl implements OperationService {

    @Autowired
    OperationMapper opMapper;

    @Override
    public int logger(Operation op) {
        return opMapper.insertOperation(op);
    }
}
