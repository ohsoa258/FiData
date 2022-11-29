package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.pageFilter.dto.OperatorVO;
import com.fisk.mdm.dto.masterdata.*;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupDropDownVO;
import com.fisk.mdm.vo.masterdata.BathUploadMemberListVO;
import com.fisk.mdm.vo.masterdata.BathUploadMemberVO;
import com.fisk.mdm.vo.model.ModelDropDownVO;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 主数据服务
 *
 * @author ChenYa
 * @date 2022/04/25
 */
public interface IMasterDataService {

    /**
     * 分页
     *
     * @param dto
     * @param response
     * @return
     */
    ResultObjectVO getMasterDataPage(MasterDataQueryDTO dto, HttpServletResponse response);

    /**
     * 获取模型实体版本数据
     *
     * @return
     */
    List<ModelDropDownVO> getModelEntityVersionStruct();

    /**
     * 根据模型id,实体id获取属性组列表
     *
     * @param modelId
     * @param entity
     * @return
     */
    List<AttributeGroupDropDownVO> listAttributeGroup(Integer modelId, Integer entity);

    /**
     * 下载模板
     *
     * @param entityId
     * @param response
     * @return
     */
    ResultEnum downloadTemplate(int entityId, HttpServletResponse response);

    /**
     * 导入模板数据
     *
     * @param dto
     * @param file
     * @return
     */
    BathUploadMemberListVO importTemplateData(ImportParamDTO dto, MultipartFile file);

    /**
     * 导出数据分页展示
     * @param dto
     * @return
     */
    BathUploadMemberVO importDataQuery(ImportDataQueryDTO dto);

    /**
     * 模板数据提交
     *
     * @param dto
     * @return
     * @throws SQLException
     */
    ResultEnum importDataSubmit(ImportDataSubmitDTO dto);

    /**
     * 模板导入数据-实时修改
     *
     * @param dto
     * @return
     */
    ResultEnum updateImportData(UpdateImportDataDTO dto);

    /**
     * 主数据新增
     *
     * @param dto
     * @return
     */
    ResultEnum addMasterData(MasterDataDTO dto);

    /**
     * 主数据删除
     *
     * @param dto
     * @return
     */
    ResultEnum delMasterData(MasterDataDTO dto);

    /**
     * 主数据更新
     *
     * @param dto
     * @return
     */
    ResultEnum updateMasterData(MasterDataDTO dto);

    /**
     * 主数据详情
     * @param dto
     * @return
     */
    //MasterDataDetailsVO getMasterData(MasterDataDetailsParamDTO dto);

    /**
     * 筛选运算符
     *
     * @return
     */
    List<OperatorVO> getSearchOperator();

    /**
     * 获取实体表code和name数据
     *
     * @param dto
     * @return
     */
    List<Map<String, Object>> listEntityCodeAndName(MasterDataBaseDTO dto);


}
