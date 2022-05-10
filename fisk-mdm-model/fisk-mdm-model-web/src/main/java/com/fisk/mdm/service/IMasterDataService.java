package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.masterdata.ImportDataQueryDTO;
import com.fisk.mdm.dto.masterdata.ImportDataSubmitDTO;
import com.fisk.mdm.dto.masterdata.ImportParamDTO;
import com.fisk.mdm.dto.masterdata.MasterDataDTO;
import com.fisk.mdm.vo.masterdata.BathUploadMemberListVo;
import com.fisk.mdm.vo.masterdata.BathUploadMemberVO;
import com.fisk.mdm.vo.masterdata.ExportResultVO;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

/**
 * 主数据服务
 *
 * @author ChenYa
 * @date 2022/04/25
 */
public interface IMasterDataService {

    /**
     * 得到所有
     *
     * @param entityId       实体id
     * @param modelVersionId 模型版本id
     * @return {@link ResultObjectVO}
     */
    ResultEntity<ResultObjectVO> getAll(Integer entityId, Integer modelVersionId);

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
     * @param dto
     * @return
     * @throws SQLException
     */
    ResultEnum importDataSubmit(ImportDataSubmitDTO dto) throws SQLException;

    /**
     * 主数据新增
     * @param dto
     * @return
     */
    ResultEnum addMasterData(MasterDataDTO dto);

    /**
     * 主数据删除
     * @param dto
     * @return
     */
    ResultEnum delMasterData(MasterDataDTO dto);


}
