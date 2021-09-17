package com.fisk.task.dto.pgsql;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/15 10:44
 * Description:
 */
@Data
public class PgsqlDelTableDTO extends MQBaseDTO {
    /**
     * 应用id
     */
    public String appId;
    /**
     * 表数组
     */
    public List<TableListDTO> tableList;

    public boolean delApp;

}


