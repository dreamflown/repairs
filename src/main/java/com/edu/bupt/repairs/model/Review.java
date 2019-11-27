package com.edu.bupt.repairs.model;

import java.math.BigInteger;

public class Review {

    private BigInteger userId;

    private int score;

    private String contents;

    public void setUserId(BigInteger userId){this.userId=userId;}

    public void setScore(int score){this.score=score;}

    public void setContents(String contents){this.contents=contents;}

}
