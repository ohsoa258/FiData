package com.fisk.datamodel.dto.fact;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactDTO {
    public int id;
    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 业务过程id
     */
    public int businessProcessId;
    /**
     * 事实表名称
     */
    public String factTabName;
    /**
     * 事实表中文名称
     */
    public String factTableCnName;
    /**
     * 事实表描述
     */
    public String factTableDesc;

    /*
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;

}
