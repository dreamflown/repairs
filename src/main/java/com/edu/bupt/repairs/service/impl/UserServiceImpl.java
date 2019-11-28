package com.edu.bupt.repairs.service.impl;

import com.edu.bupt.repairs.model.User;
import com.edu.bupt.repairs.service.UserService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public User getUserInfo(BigInteger uId) {
        return UserMapper.getUserByUid(uId);
    }
}
