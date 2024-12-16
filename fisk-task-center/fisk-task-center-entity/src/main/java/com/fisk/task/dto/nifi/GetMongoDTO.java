package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetMongoDTO extends BaseProcessorDTO {

    @ApiModelProperty(value = "mongodb数据库连接池服务")
    public String clientService;

    @ApiModelProperty(value = "mongodb数据库名称")
    public String mongoDatabaseName;

    @ApiModelProperty(value = "mongodb集合名称")
    public String mongoCollectionName;

    /**
     * Standard JSON  //  Extended JSON
     */
    @ApiModelProperty(value = "输出的JSON类型")
    public String JSONType;

    /**
     * 例值:{"username": "Tom"}
     */
    @ApiModelProperty(value = "mongodb查询条件")
    public String query;

    /**
     * 为空则查询全部字段 例值：{"_id": 1, "username": 1, "product": 1, "price": 1, "type": 1}
     */
    @ApiModelProperty(value = "mongodb投影（查询字段）")
    public String projection;

    @ApiModelProperty(value = "单次返回的文件流的最大数据量")
    public String resultsPerFlowFile;

    /**
     * nifi组件并发数量（目前只给数接/数仓的查询组件开启并发）
     */
    @ApiModelProperty(value = "nifi组件并发数量")
    public Integer concurrencyNums;


}
