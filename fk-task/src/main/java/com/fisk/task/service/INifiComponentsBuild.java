package com.fisk.task.service;

import com.davis.client.model.ControllerServiceEntity;
import com.davis.client.model.PositionDTO;
import com.davis.client.model.ProcessGroupEntity;

/**
 * Nifi组件创建
 *
 * @author gy
 */
public interface INifiComponentsBuild {

    /* ===========组=========== */

    /**
     * 创建组
     *
     * @param name        分组名称
     * @param details     描述
     * @param pid         父级id
     * @param positionDTO 位置信息
     * @return 创建的实体
     */
    ProcessGroupEntity buildProcessGroup(String name, String details, String pid, PositionDTO positionDTO);

    /**
     * 获取所有的组
     *
     * @param pid 父级id
     * @return 组
     */
    ProcessGroupEntity getProcessGroupByPid(String pid);

    /* ===========连接池=========== */
    /**
     * 数据库连接对象创建
     * @return 创建的连接对象
     */
    ControllerServiceEntity buildProcessControlService(String id, PositionDTO positionDTO);
}
