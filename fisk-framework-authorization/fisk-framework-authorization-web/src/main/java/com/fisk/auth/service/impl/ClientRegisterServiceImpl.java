package com.fisk.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.dto.clientregister.ClientRegisterDTO;
import com.fisk.auth.dto.clientregister.ClientRegisterPageDTO;
import com.fisk.auth.dto.clientregister.ClientRegisterQueryDTO;
import com.fisk.auth.entity.ClientRegisterPO;
import com.fisk.auth.map.ClientRegisterMap;
import com.fisk.auth.mapper.ClientRegisterMapper;
import com.fisk.auth.service.IClientRegisterService;
import com.fisk.auth.utils.JwtUtils;
import com.fisk.auth.vo.ClientRegisterVO;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-04 16:46:24
 */
@Service
public class ClientRegisterServiceImpl extends ServiceImpl<ClientRegisterMapper, ClientRegisterPO> implements IClientRegisterService {

    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private RedisUtil redis;
    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private GetMetadata getMetadata;

    @Override
    public ClientRegisterDTO getData(long id) {

        ClientRegisterPO po = this.getById(id);
        if (po == null) {
            throw new FkException(ResultEnum.CLIENT_ISEMPTY);
        }
        // po -> dto
        return ClientRegisterMap.INSTANCES.poToDto(po);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addData(ClientRegisterDTO dto) {

        // 当前字段名不可重复
        List<String> list = this.list().stream().map(e -> e.clientName).collect(Collectors.toList());
        if (list.contains(dto.clientName)) {
            return ResultEnum.NAME_EXISTS;
        }

        // dto -> po
        ClientRegisterPO model = ClientRegisterMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 保存
        int insert = baseMapper.insert(model);
        if (insert < 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 1.将客户端信息存储到redis一份
        // 1.1创建自定义荷载对象
        UserDetail userDetail = UserDetail.of(model.id, dto.clientName);
        // 1.2生成token
        String token = SystemConstants.AUTH_TOKEN_HEADER + jwtUtils.createJwt(userDetail);
        // 1.3写入redis
        UserInfo userInfo = UserInfo.of(model.id, dto.clientName, token);
        // TODO 过期时间待会修改
        redis.set(RedisKeyBuild.buildClientInfo(model.id), userInfo, dto.expireTimestamp);
        // 2.将token更新到添加的这一条数据上
        ClientRegisterPO clientRegisterPO = this.query().eq("id", model.id).one();
        clientRegisterPO.tokenValue = token;
        // 3.入mysql库
        return this.updateById(clientRegisterPO) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(ClientRegisterDTO dto) {
        // 判断名称是否重复
        QueryWrapper<ClientRegisterPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ClientRegisterPO::getClientName, dto.clientName);
        ClientRegisterPO po = baseMapper.selectOne(queryWrapper);
        if (po != null && po.id != dto.id) {
            return ResultEnum.WORKFLOWNAME_EXISTS;
        }

        // 参数校验
        ClientRegisterPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        // 执行修改
        return this.updateById(ClientRegisterMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(long id) {
        // 参数校验
        ClientRegisterPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 删除
        // 1.去查询redis是否有这个值,有即删除
        UserInfo userInfo = (UserInfo) redis.get(RedisKeyBuild.buildClientInfo(id));
        if (userInfo != null) {
            redis.del(RedisKeyBuild.buildClientInfo(id));
        }
        // 2.删除当前记录
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<String> getClientInfoList() {
        return this.list().stream().map(e -> e.clientName).collect(Collectors.toList());
    }

    @Override
    public Page<ClientRegisterVO> listData(ClientRegisterQueryDTO query) {

        StringBuilder querySql = new StringBuilder();
        if (query.key != null && query.key.length() > 0) {
            querySql.append(" and workflow_name like concat('%', " + "'" + query.key + "'" + ", '%') ");
        }

        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));
        ClientRegisterPageDTO data = new ClientRegisterPageDTO();
        data.page = query.page;
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();

        return baseMapper.filter(query.page, data);
    }

    @Override
    public List<FilterFieldDTO> getColumn() {
        return getMetadata.getMetadataList(
                "dmp_system_db",
                "tb_client_register",
                "",
                FilterSqlConstants.TB_CLIENT_REGISTER_SQL);
    }

}