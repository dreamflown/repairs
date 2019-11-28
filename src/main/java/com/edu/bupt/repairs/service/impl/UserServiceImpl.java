package com.edu.bupt.repairs.service.impl;

import com.edu.bupt.repairs.model.User;
import com.edu.bupt.repairs.service.UserService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public User getUserInfo(BigInteger uId) {
        return UserMapper.getUserByUid(uId);
    }

    public List<User> getUserListForFirstPartyAndGroupName(String firstParty, String secondParty, String groupName){
        return UserMapper.selectGroupByBothPartiesAndGroupName(firstParty, secondParty, groupName);
    }
}
