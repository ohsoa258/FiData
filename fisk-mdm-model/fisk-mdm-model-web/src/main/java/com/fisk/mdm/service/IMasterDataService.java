package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.masterdata.*;
import com.fisk.mdm.vo.masterdata.BathUploadMemberListVo;
import com.fisk.mdm.vo.masterdata.BathUploadMemberVO;
import com.fisk.mdm.vo.model.ModelDropDownVO;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.List;

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
     * @return
     */
    ResultObjectVO getMasterDataPage(MasterDataQueryDTO dto, HttpServletResponse response);

    /**
     * 获取模型实体版本数据
     * @return
     */
    List<ModelDropDownVO> getModelEntityVersionStruct();

    /**
     * 下载模板
     * @param entityId
     * @param response
     * @return
     */
    ResultEnum downloadTemplate(int entityId, HttpServletResponse response);

    /**
     * 导入模板数据
     * @param dto
     * @param file
     * @return
     */
    BathUploadMemberListVo importTemplateData(ImportParamDTO dto, MultipartFile file);

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
     * @param dto
     * @return
     */
    //ResultEnum updateMasterData(MasterDataDTO dto);

    /**
     * 主数据详情
     * @param dto
     * @return
     */
    //MasterDataDetailsVO getMasterData(MasterDataDetailsParamDTO dto);


}
