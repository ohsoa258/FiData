package com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter;

import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗保存配置DTO
 * @date 2022/10/8 16:23
 */
@Data
public class BusinessFilterSaveDTO {

    /**
     * 数据质量数据源表主键ID
     */
    @ApiModelProperty(value = "数据质量数据源表主键ID")
    public int datasourceId;

    /**
     * 数据源类型 1:FiData 2:custom
     */
    @ApiModelProperty(value = "数据源类型 1:FiData 2:custom")
    public SourceTypeEnum sourceTypeEnum;

    /**
     * custom模式下是表名称
     * FiData类型下是表Id
     */
    @ApiModelProperty(value = "custom模式下是表名称/FiData类型下是表Id")
    public String tableUnique;

    /**
     * 表业务类型 1：dw维度表、2：dw事实表、3、doris维度表  4、doris事实表 5、宽表
     */
    @ApiModelProperty(value = "表业务类型 1：dw维度表、2：dw事实表、3、doris维度表  4、doris事实表 5、宽表")
    public int tableBusinessType;

    /**
     * API基础信息配置
     */
    @ApiModelProperty(value = "API基础信息配置")
    public BusinessFilterApiConfigDTO apiConfig;

    /**
     * API请求参数配置
     */
    @ApiModelProperty(value = "API请求参数配置")
    public List<BusinessFilterApiParamDTO> apiParamConfig;

    /**
     * API返回结果配置
     */
    @ApiModelProperty(value = "API返回结果配置")
    public List<BusinessFilterApiResultDTO> apiResultConfig;
}
