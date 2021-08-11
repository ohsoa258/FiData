package com.fisk.dataservice.entity;

import com.fisk.dataservice.dto.FieldDTO;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/5 11:01
 */
@Data
public class GbfPO {
    private String tableName;
    private List<FieldDTO> fieldName;
}
