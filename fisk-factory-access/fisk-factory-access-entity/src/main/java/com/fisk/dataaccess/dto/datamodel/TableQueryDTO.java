package com.fisk.dataaccess.dto.datamodel;

import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-09-04
 * @Description:
 */
@Data
public class TableQueryDTO {
    private List<String> ids;
    private int type;
}
