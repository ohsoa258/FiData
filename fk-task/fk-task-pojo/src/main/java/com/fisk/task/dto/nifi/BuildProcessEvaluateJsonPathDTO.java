package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildProcessEvaluateJsonPathDTO extends BaseProcessorDTO {
    /*
    * 自定义常量
    * */
    public List<String> selfDefinedParameter;

}
