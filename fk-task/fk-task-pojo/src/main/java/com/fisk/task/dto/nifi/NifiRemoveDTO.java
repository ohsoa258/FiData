package com.fisk.task.dto.nifi;

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
     *组件ids
     * */
    public List<String> ProcessIds;
    /*
     *控制器服务ids
     * */
    public List<String> controllerServicesIds;


}
