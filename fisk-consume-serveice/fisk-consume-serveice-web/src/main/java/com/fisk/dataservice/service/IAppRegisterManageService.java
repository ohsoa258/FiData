package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.app.*;
import com.fisk.dataservice.entity.AppConfigPO;
import com.fisk.dataservice.vo.app.AppApiParmVO;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import com.fisk.dataservice.vo.app.AppRegisterVO;


import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 应用注册接口
 * @author dick
 */
public interface IAppRegisterManageService extends IService<AppConfigPO> {

    /**
     * 查询下游系统总数
     * @return 字段
     */
    Integer getAppCount();

    /**
     * 获取过滤器表字段
     * @return 字段
     */
    List<FilterFieldDTO> getColumn();

    /**
     * 筛选器
     * @param query 查询条件
     * @return 筛选结果
     */
    Page<AppRegisterVO> pageFilter(AppRegisterQueryDTO query);

    /**
     * 分页查询
     * @param page 分页条件
     * @return 应用列表
     */
    Page<AppRegisterVO> getAll(Page<AppRegisterVO> page);

    /**
     * 添加数据
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(AppRegisterDTO dto);

    /**
     * 编辑数据
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(AppRegisterEditDTO dto);

    /**
     * 删除数据
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(int id);

    /**
     * 分页查询应用API订阅
     * @param dto dto
     * @return api订阅列表
     */
    Page<AppApiSubVO> getSubscribeAll(AppApiSubQueryDTO dto);

    /**
     * 应用订阅api
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum appSubscribe(AppApiSubSaveDTO dto);

    /**
     * 重置应用密码
     * @param dto 应用密码信息
     * @return 执行结果
     */
    ResultEnum resetPwd(AppPwdResetDTO dto);

    /**
     * 生成文档
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum createDoc(CreateAppApiDocDTO dto, HttpServletResponse response);

    /**
     * 下载文档
     * @param fileName 文件路径
     * @return 执行结果
     */
//    ResponseEntity downloadDoc(String fileName);

    /**
     * 查询内置参数
     * @param dto dto
     * @return 内置参数列表
     */
    List<AppApiParmVO> getParmAll(AppApiParmQueryDTO dto);

    /**
     * 设置内置参数
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum setParm(AppApiBuiltinParmEditDTO dto);
}
