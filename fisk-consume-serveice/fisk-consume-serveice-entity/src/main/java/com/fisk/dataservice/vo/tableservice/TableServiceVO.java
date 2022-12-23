package com.fisk.dataservice.vo.tableservice;

import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableServiceVO extends TableServiceDTO {

    /**
     * 是否订阅 1：已订阅 0：未订阅
     */
    public Integer tableServiceSubState;

}
