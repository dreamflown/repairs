package com.edu.bupt.repairs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private BigInteger id;

    private BigInteger userId;

    private int score;

    private String contents;

}
