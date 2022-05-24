package com.fisk.task.dto.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class PipelineTableQueryDTO {


    /**
     * APP ID
     */
    @ApiModelProperty(value = "应用id")
    public Integer appId;

    /**
     * keyword
     */
    @ApiModelProperty(value = "keyword")
    public String keyword;

    /*
    * SynchronousTypeEnum
    *     TOPGODS(0,"toPgOds"),
    PGTOPG(1,"pgToPg"),
    PGTODORIS(2,"PgToDoris");
    * */
    @ApiModelProperty(value = "应用类别,0:接入,1:建模,2分析建模")
    public int appType;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<PipelineTableLogVO> page;
}
