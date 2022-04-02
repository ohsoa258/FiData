package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.datasecurity.TablesecurityConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.datamasking.DataSourceIdDTO;
import com.fisk.datagovernance.entity.datasecurity.TablesecurityConfigPO;
import com.fisk.datagovernance.entity.datasecurity.UserGroupInfoPO;
import com.fisk.datagovernance.map.datasecurity.TableSecurityConfigMap;
import com.fisk.datagovernance.mapper.datasecurity.PermissionManagementMapper;
import com.fisk.datagovernance.mapper.datasecurity.TablesecurityConfigMapper;
import com.fisk.datagovernance.service.datasecurity.TableSecurityConfigService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDropDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
@Slf4j
public class TablesecurityConfigServiceImpl extends ServiceImpl<TablesecurityConfigMapper, TablesecurityConfigPO> implements TableSecurityConfigService {

    @Resource
    private PermissionManagementServiceImpl permissionManagementServiceImpl;

    @Resource
    private PermissionManagementMapper permissionManagementMapper;

    @Resource
    private UserGroupInfoServiceImpl userGroupInfoServiceImpl;

    @Resource
    private UserClient userClient;

    @Override
    public TablesecurityConfigDTO getData(long id) {

        TablesecurityConfigPO po = this.getById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // po -> dto
        TablesecurityConfigDTO dto = TableSecurityConfigMap.INSTANCES.poToDto(po);
        // TODO 根据访问类型和用户(组)id,查询用户(组)名称
        dto.name = getUserGroupNameOrUserName(dto.accessType, dto.userGroupId);
        return dto;
    }

    @Override
    public ResultEnum addData(TablesecurityConfigDTO dto) {

        // dto -> po
        TablesecurityConfigPO model = TableSecurityConfigMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 保存主表数据
        return this.save(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(TablesecurityConfigDTO dto) {

        // 参数校验
        TablesecurityConfigPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        // 执行修改
        return this.updateById(TableSecurityConfigMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(long id) {
        // 参数校验
        TablesecurityConfigPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 执行删除
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<TablesecurityConfigDTO> getList(DataSourceIdDTO dto) {
        // 根据创建时间倒叙
        List<TablesecurityConfigPO> listPo = this.query()
                .eq("datasource_id", dto.datasourceId)
                .eq("table_id", dto.tableId)
                .orderByDesc("create_time").list();
        List<TablesecurityConfigDTO> list = TableSecurityConfigMap.INSTANCES.listPoToDto(listPo);

        // TODO 根据访问类型和用户(组)id,查询用户(组)名称
        list.forEach(e -> e.name = getUserGroupNameOrUserName(e.accessType, e.userGroupId));

        return list;
    }

    @Override
    public ResultEnum editDefaultConfig(long defaultConfig) {

        UpdateWrapper updateWrapper = new UpdateWrapper();
        // 修改表中default_config这一列的数据
        updateWrapper.set("default_config", defaultConfig);
        return baseMapper.update(null, updateWrapper) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 根据访问类型和用户(组)id,获取用户(组)名称
     *
     * @return java.lang.String 用户(组)名称
     * @description
     * @author Lock
     * @date 2022/4/2 17:44
     * @version v1.0
     * @params accessType 访问类型 0: 空  1:用户组  2: 用户
     * @params userGroupId 用户(组)id
     */
    public String getUserGroupNameOrUserName(Long accessType, Long userGroupId) {

        // 用户组
        if (accessType == 1) {
            UserGroupInfoPO userGroupInfoPo = userGroupInfoServiceImpl.query().eq("id", userGroupId).one();
            return userGroupInfoPo == null ? "当前用户(组)不存在" : userGroupInfoPo.userGroupName;
            // 用户
        } else if (accessType == 2) {
            try {
                ResultEntity<List<UserDropDTO>> result = userClient.listUserDrops();
                List<UserDropDTO> data = result.data;
                for (UserDropDTO e : data) {
                    if (e.id == userGroupId) {
                        return e.username;
                    }
                }
            } catch (Exception e) {
                log.error("远程调用失败，方法名：【user-service:listUserDrops】");
                return "当前用户(组)不存在";
            }
        }
        return null;
    }
}