package com.fisk.datamodel.vo;

import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataModelTableVO {

    /**
     * 表集合id
     */
    public List<Long> ids;
    /**
     * 表类型：维度/事实..
     */
    public OlapTableEnum type;

}
