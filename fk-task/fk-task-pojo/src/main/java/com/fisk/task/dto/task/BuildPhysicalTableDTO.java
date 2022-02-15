package com.fisk.task.dto.task;

import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.enums.DbTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class BuildPhysicalTableDTO extends MQBaseDTO {
    public List<TableFieldsDTO> tableFieldsDTOS;
    public String appAbbreviation;
    public String selectSql;
    public String tableName;
    public String appId;
    public String dbId;
    /**
     * 驱动类型
     */
    public DbTypeEnum driveType;

    /**
     * 版本号和存储过程
     */
    public ModelPublishTableDTO modelPublishTableDTO;

    /*
    * 同步方式
    * */
    public int syncMode;

    /*
    * 查询范围开始时间
    * */
    public String queryStartTime;

    /*
    * 查询范围结束时间
    * */
    public String queryEndTime;

    public boolean openTransmission;
}
