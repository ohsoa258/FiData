package com.fisk.task.dto.nifi;

import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class NifiRemoveDTO {
    /*
    * 应用id
    * */
    @ApiModelProperty(value = "应用id")
    public String appId;
    /*
     * 是否删除应用
     * */
    @ApiModelProperty(value = "是否删除应用")
    public Boolean delApp;
    /*
     *任务组id
     * */
    @ApiModelProperty(value = "任务组id")
    public String groupId;
    /*
    * 物理/事实/维度/指标表id
    * */
    @ApiModelProperty(value = "物理/事实/维度/指标表id")
    public Long tableId;
    /*
    * 表类型
    * */
    @ApiModelProperty(value = "表类型")
    public OlapTableEnum olapTableEnum;
    /*
     *组件ids
     * */
    @ApiModelProperty(value = "组件ids")
    public List<String> ProcessIds;
    /*
     *控制器服务ids
     * */
    @ApiModelProperty(value = "控制器服务ids")
    public List<String> controllerServicesIds;

    /*
    * inputportsid
    * */
    @ApiModelProperty(value = "输入端口id")
    public List<String> inputPortIds;

    /*
     * outputportsid
     * */
    @ApiModelProperty(value = "输出端口id")
    public List<String> outputPortIds;

    /*
    * inputportConnectsid
    * */
    @ApiModelProperty(value = "输入端口连接id")
    public List<String> inputportConnectIds;

    /*
    * outputportConnectsid
    * */
    @ApiModelProperty(value = "输出端口连接id")
    public List<String> outputportConnectIds;
}
