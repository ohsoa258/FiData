package com.fisk.task.dto.tableservice;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.task.vo.tableservice.TableServiceDetailVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-24
 * @Description:
 */
@Data
public class TableServiceDetailDTO {
    @ApiModelProperty("天数")
    public Integer lookday;
    @ApiModelProperty("表服务名称")
    public String tableServiceName;
    @ApiModelProperty(value = "page")
    public Page<TableServiceDetailVO> page;
}
