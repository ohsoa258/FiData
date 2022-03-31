package com.fisk.datagovernance.service.datasecurity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.UserGroupAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.UserGroupAssignmentPO;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
public interface UserGroupAssignmentService extends IService<UserGroupAssignmentPO> {

    /**
     * 回显: 根据id查询数据
     *
     * @param id id
     * @return 查询结果
     */
    UserGroupAssignmentDTO getData(long id);

    /**
     * 添加
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(UserGroupAssignmentDTO dto);

    /**
     * 修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(UserGroupAssignmentDTO dto);

    /**
     * 删除
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);
}

