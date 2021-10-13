package com.fisk.task.dto.nifi;

import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;

import java.util.List;

@Data
public class NifiRemoveDTO {
    /*
    * 应用id
    * */
    public String appId;
    /*
     * 是否删除应用
     * */
    public Boolean delApp;
    /*
     *任务组id
     * */
    public String groupId;
    /*
    * 物理/事实/维度/指标表id
    * */
    public Long tableId;
    /*
    * 表类型
    * */
    public OlapTableEnum olapTableEnum;
    /*
     *组件ids
     * */
    public List<String> ProcessIds;
    /*
     *控制器服务ids
     * */
    public List<String> controllerServicesIds;

    /*
    * inputportsid
    * */
    public List<String> inputPortIds;

    /*
     * outputportsid
     * */
    public List<String> outputPortIds;


}
