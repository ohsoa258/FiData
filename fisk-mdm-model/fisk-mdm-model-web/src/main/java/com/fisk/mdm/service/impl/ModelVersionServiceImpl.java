package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.complextype.GeographyDTO;
import com.fisk.mdm.dto.modelVersion.ModelCopyDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionUpdateDTO;
import com.fisk.mdm.entity.ModelVersionPO;
import com.fisk.mdm.enums.DataTypeEnum;
import com.fisk.mdm.enums.ModelVersionStatusEnum;
import com.fisk.mdm.map.ModelVersionMap;
import com.fisk.mdm.mapper.ModelVersionMapper;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.IComplexType;
import com.fisk.mdm.service.IModelService;
import com.fisk.mdm.service.IModelVersionService;
import com.fisk.mdm.vo.complextype.EchoFileVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.model.ModelInfoVO;
import com.fisk.mdm.vo.modelVersion.ModelVersionDropDownVO;
import com.fisk.mdm.vo.modelVersion.ModelVersionVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static com.fisk.common.service.mdmBEBuild.AbstractDbHelper.closeConnection;
import static com.fisk.common.service.mdmBEBuild.AbstractDbHelper.execQueryResultList;
import static com.fisk.mdm.utlis.DataSynchronizationUtils.MARK;
import static com.fisk.mdm.utlis.FileConvertUtils.createFileItem;


/**
 * @author ChenYa
 */
@Service
public class ModelVersionServiceImpl extends ServiceImpl<ModelVersionMapper, ModelVersionPO> implements IModelVersionService {

    @Value("${pgsql-mdm.type}")
    DataSourceTypeEnum type;
    @Value("${pgsql-mdm.url}")
    String connectionStr;
    @Value("${pgsql-mdm.username}")
    String acc;
    @Value("${pgsql-mdm.password}")
    String pwd;

    @Resource
    ModelVersionMapper modelVersionMapper;
    @Resource
    IModelService modelService;
    @Resource
    EntityService entityService;
    @Resource
    UserClient userClient;
    @Resource
    IComplexType iComplexType;


    /**
     * 新增模型版本
     * @param dto
     * @return
     */
    @Override
    public ResultEnum addData(ModelVersionDTO dto) {
        QueryWrapper<ModelVersionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ModelVersionPO::getName,dto.getName());
        ModelVersionPO modelVersionPo = modelVersionMapper.selectOne(queryWrapper);
        if (modelVersionPo != null){
            return ResultEnum.DATA_EXISTS;
        }

        return modelVersionMapper.insert(ModelVersionMap.INSTANCES.dtoToPo(dto)) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<ModelVersionVO> getByModelId(Integer modelId) {

        QueryWrapper<ModelVersionPO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ModelVersionPO::getModelId,modelId);

        List<ModelVersionVO> list = ModelVersionMap.INSTANCES.poToVoList(baseMapper.selectList(wrapper));

        // 获取创建人、修改人
        ReplenishUserInfo.replenishUserName(list, userClient, UserFieldEnum.USER_ACCOUNT);
        return list;
    }

    @Override
    public ResultEnum updateData(ModelVersionUpdateDTO dto) {
        ModelVersionPO modelVersionPo = modelVersionMapper.selectById(dto.getId());
        if (modelVersionPo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = modelVersionMapper.updateById(ModelVersionMap.INSTANCES.updateDtoToPo(dto));
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataById(Integer id) {
        ModelVersionPO modelVersionPo = modelVersionMapper.selectById(id);
        if (modelVersionPo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 最后一个版本不允许删除
        QueryWrapper<ModelVersionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ModelVersionPO::getModelId,modelVersionPo.getModelId());
        List<ModelVersionPO> list = modelVersionMapper.selectList(queryWrapper);
        if (list.size() <= 1){
            return ResultEnum.AVERSION_NOT_DELETE;
        }

        int res = modelVersionMapper.deleteById(id);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum copyDataByModelId(ModelCopyDTO dto) {
        ModelVersionPO modelVersionPo = modelVersionMapper.selectById(dto.getId());
        if (modelVersionPo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 复制的版本名称不能重复
        QueryWrapper<ModelVersionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ModelVersionPO::getModelId,dto.getModelId())
                .eq(ModelVersionPO::getName,dto.getName())
                .last(" limit 1 ");
        ModelVersionPO modelVersionPo1 = modelVersionMapper.selectOne(queryWrapper);
        if (modelVersionPo1 != null){
            return ResultEnum.NAME_EXISTS;
        }

        // 未提交的数据的不能复制
        if (!modelVersionPo.getStatus().equals(ModelVersionStatusEnum.SUBMITTED)) {
            return ResultEnum.UNCOMMITTED_CANNOT_COPIED;
        }

        // 1.创建一条版本数据
        ModelVersionPO versionPo = ModelVersionMap.INSTANCES.copyDtoToPo(dto);
        versionPo.setStatus(ModelVersionStatusEnum.OPEN);
        int res = modelVersionMapper.insert(versionPo);
        if (res <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 2.复制模型下的所有实体
        ModelInfoVO modelInfoVo = modelService.getEntityById(dto.getModelId(),null);
        List<EntityVO> list = modelInfoVo.getEntityVOList();
        if (CollectionUtils.isNotEmpty(list)){

            list.stream().forEach(e -> {

                if (StringUtils.isNotBlank(e.getTableName())){

                    // 创建连接对象
                    AbstractDbHelper dbHelper = new AbstractDbHelper();
                    Connection connection = dbHelper.connection(connectionStr, acc,
                            pwd,type);

                    // 1.复制数据
                    this.entityDataCopy(dbHelper,connection,e,(int)versionPo.getId(),dto.getId());

                    // 2.复制复杂类型数据
                    List<AttributeInfoDTO> attributeList = entityService.getAttributeById(e.getId(), null).getAttributeList();

                    List<AttributeInfoDTO> longitudeList = attributeList.stream().filter(item -> item.getDataType().equals(DataTypeEnum.LATITUDE_COORDINATE)).collect(Collectors.toList());
                    List<AttributeInfoDTO> fileList = attributeList.stream().filter(item -> item.getDataType().equals(DataTypeEnum.FILE)).collect(Collectors.toList());

                    // 3.查询复杂类型的code
                    if (CollectionUtils.isNotEmpty(longitudeList)){
                        // 经纬度类型
                        String field = longitudeList.stream().map(iter -> iter.getColumnName()).collect(Collectors.joining(","));
                        String sql = "SELECT " + field + " FROM " + e.getTableName() + " WHERE fidata_version_id = '" + dto.getId() + "'";
                        // 需要复制数据得code
                        List<String> codes = execQueryResultList(sql, connection, String.class);
                        this.copyLatitude(codes,(int)versionPo.getId(),connection,DataTypeEnum.LATITUDE_COORDINATE);
                    }

                    if (CollectionUtils.isNotEmpty(fileList)){
                        // 文件类型
                        String field = fileList.stream().map(iter -> iter.getColumnName()).collect(Collectors.joining(","));
                        String sql = "SELECT " + field + " FROM " + e.getTableName() + " WHERE fidata_version_id = '" + dto.getId() + "'";
                        // 需要复制数据得code
                        List<String> codes = execQueryResultList(sql, connection, String.class);
                        this.copyLatitude(codes,(int)versionPo.getId(),connection,DataTypeEnum.FILE);
                    }
                }
            });
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 复制数据
     * @param entityVo
     * @param newVersionId
     * @param oldVersionId
     */
    public void entityDataCopy(AbstractDbHelper dbHelper,Connection connection,
                               EntityVO entityVo,Integer newVersionId,Integer oldVersionId){

        String sql = null;
        try {
            // 1.生成Sql
            sql = this.buildDataCopySql(entityVo, newVersionId,oldVersionId);

            // 2.执行Sql
            dbHelper.executeSql(sql, connection);
        }catch (SQLException ex){
            log.error("【版本复制数据Sql】:" + sql + "【版本复制数据失败,异常信息】:" + ex);
            throw new FkException(ResultEnum.DATA_REPLICATION_FAILED);
        }finally {
            // 关闭数据库连接
            closeConnection(connection);
        }
    }

    /**
     * 生成复制数据Sql
     * @param entityVo
     * @param versionId
     */
    public String buildDataCopySql(EntityVO entityVo,Integer versionId,Integer oldVersionId){
        String tableName = entityVo.getTableName();

        StringBuilder str = new StringBuilder();
        str.append(" INSERT INTO ");
        str.append(tableName).append("(");
        // 拼接字段
        str.append(MARK + "version_id").append(",");
        str.append(this.field(entityVo.getId()));

        str.append(")").append("SELECT ");
        // 拼接字段
        str.append(versionId).append(",");
        str.append(this.field(entityVo.getId()));

        str.append(" FROM " + tableName);
        str.append(" WHERE fidata_version_id ='" + oldVersionId).append("'");
        return str.toString();
    }

    /**
     * 需要插入的字段
     * @param entityId
     * @return
     */
    public String field(Integer entityId){
        // 系统字段
        StringBuilder str = new StringBuilder();
        str.append(MARK + "lock_tag").append(",");
        str.append(MARK + "new_code").append(",");
        str.append(MARK + "create_time").append(",");
        str.append(MARK + "create_user").append(",");
        str.append(MARK + "update_time").append(",");
        str.append(MARK + "update_user").append(",");
        str.append(MARK + "del_flag");

        // 拼接业务字段
        List<AttributeInfoDTO> attributeList = entityService.getAttributeById(entityId,null).getAttributeList();
        if (CollectionUtils.isNotEmpty(attributeList)){
            String businessFiled = attributeList.stream().map(e -> e.getColumnName()).collect(Collectors.joining(","));
            str.append(",");
            str.append(businessFiled);

            // 业务字段
        }

        return str.toString();
    }

    /**
     * 复制复杂数据类型
     * @param codes
     * @param newVersionId
     * @param connection
     * @param type
     * @return
     */
    public ResultEnum copyLatitude(List<String> codes,Integer newVersionId, Connection connection,
                               DataTypeEnum type){

        String code = codes.stream().map(e -> "'" + e + "'").collect(Collectors.joining(","));

        // 经纬度类型
        if (type.equals(DataTypeEnum.LATITUDE_COORDINATE)){
            // 1.查询数据
            String sql = "SELECT * FROM " + "tb_geography" + " WHERE IN(" + code +")";
            List<GeographyDTO> list = execQueryResultList(sql, connection, GeographyDTO.class);

            // 2.结果集转换
            list.stream().forEach(e -> {
                e.setVersionId(newVersionId);
                iComplexType.addGeography(e);
            });
        }

        // 文件类型
        if (type.equals(DataTypeEnum.FILE)){
            // 1.查询数据
            String sql = "SELECT * FROM " + "tb_file" + " WHERE code IN(" + code +")";
            List<EchoFileVO> list = execQueryResultList(sql, connection, EchoFileVO.class);

            // 2.结果集转换
            list.forEach(e -> {
                FileItem fileItem = createFileItem(e.getFilePath());
                MultipartFile file = new CommonsMultipartFile(fileItem);
                iComplexType.uploadFile(newVersionId,file).getId();
            });
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 获取模型版本列表
     * @param modelId
     * @return
     */
    public List<ModelVersionDropDownVO> getModelVersionDropDown(int modelId){
        QueryWrapper<ModelVersionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(ModelVersionPO::getModelId,modelId);
        List<ModelVersionPO> list=baseMapper.selectList(queryWrapper);
        return ModelVersionMap.INSTANCES.poListToDropDownVoList(list);
    }
}
