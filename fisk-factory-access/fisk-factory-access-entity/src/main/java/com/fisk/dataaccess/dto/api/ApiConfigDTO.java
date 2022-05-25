package com.fisk.dataaccess.dto.api;

import com.fisk.dataaccess.dto.TableAccessNonDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-01-17 14:45:02
 */
@Data
public class ApiConfigDTO {

    @ApiModelProperty(value = "主键", required = true)
    public long id;

    @ApiModelProperty(value = "应用id", required = true)
    public Long appId;

    @ApiModelProperty(value = "api名称", required = true)
    public String apiName;

    @ApiModelProperty(value = "api地址", required = true)
    public String apiAddress;

    @ApiModelProperty(value = "api请求方式(0: 空;  1: get;  2:post)", required = true)
    public Integer apiRequestType;

    @ApiModelProperty(value = "api描述", required = true)
    public String apiDes;

    @ApiModelProperty(value = "0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布", required = true)
    public Integer publish;

    @ApiModelProperty(value = "json解析的根节点")
    public String jsonKey;

    @ApiModelProperty(value = "实时api的json结构示例")
    public String pushData;

    @ApiModelProperty(value = "0: 保存;   1: 保存&发布", required = true)
    public int flag;

    /**
     * api下的物理表
     */
    public List<TableAccessNonDTO> list;

    @ApiModelProperty(value = "当前api的json格式")
    public String pushDataJson;

    @ApiModelProperty(value = "true: 勾选(发布之后,按照配置调用一次api);false: 不勾选")
    public boolean executeConfigFlag;
}
