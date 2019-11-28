package com.edu.bupt.repairs.service;

import com.edu.bupt.repairs.model.User;

import java.math.BigInteger;
import java.util.List;

public interface UserService {
    User getUserInfo(BigInteger uId);

    List<User> getUserListForFirstPartyAndGroupName(String firstParty, String secondParty, String groupName);
}
