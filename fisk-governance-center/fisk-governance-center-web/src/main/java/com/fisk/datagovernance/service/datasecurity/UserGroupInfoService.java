package com.fisk.datagovernance.service.datasecurity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoDropDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoPageDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoQueryDTO;
import com.fisk.datagovernance.entity.datasecurity.UserGroupInfoPO;
import com.fisk.system.dto.userinfo.UserDropDTO;

import java.util.List;

/**
 * @author JianWenYang
 * @email jianwen@fisk.com.cn
 * @date 2022-03-28 15:47:33
 */
public interface UserGroupInfoService extends IService<UserGroupInfoPO> {

    /**
     * 分页获取用户组数据
     * @param dto
     * @return
     */
    IPage<UserGroupInfoPageDTO> listUserGroupInfos(UserGroupInfoQueryDTO dto);

    /**
     * 回显: 根据id查询数据
     *
     * @param id id
     * @return 查询结果
     */
    UserGroupInfoDTO getData(long id);

    /**
     * 添加
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum saveData(UserGroupInfoDTO dto);

    /**
     * 修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateData(UserGroupInfoDTO dto);

    /**
     * 删除
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

    /**
     * 获取用户组下拉数据
     * @return
     */
    List<UserGroupInfoDropDTO> listUserGroupInfoDrops();

    /**
     * 获取系统用户下拉列表
     * @return
     */
    List<UserDropDTO> listSystemUserDrops();

}