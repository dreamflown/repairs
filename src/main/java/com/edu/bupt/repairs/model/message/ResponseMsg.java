package com.edu.bupt.repairs.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMsg<T> {

    private Integer ErrCode;

    private String ErrMsg;

    private T Data;

}
