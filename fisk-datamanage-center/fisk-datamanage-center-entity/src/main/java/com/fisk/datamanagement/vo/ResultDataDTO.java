package com.fisk.datamanagement.vo;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ResultDataDTO<T> {
    public AtlasResultEnum code;
    public T data;
}
