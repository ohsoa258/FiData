package com.fisk.datamanagement.vo;

import com.fisk.common.core.response.ResultEnum;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ResultDataDTO<T> {
    public ResultEnum code;
    public T data;
}
