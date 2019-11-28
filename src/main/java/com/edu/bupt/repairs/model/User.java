package com.edu.bupt.repairs.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

@Data
@AllArgsConstructor
public class User {
    BigInteger uId;

    String name;

    String phone;

    String company;
}
