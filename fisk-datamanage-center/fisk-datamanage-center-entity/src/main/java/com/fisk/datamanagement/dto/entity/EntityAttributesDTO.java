package com.fisk.datamanagement.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EntityAttributesDTO {
    @ApiModelProperty(value = "限定名")
    public String qualifiedName;
    @ApiModelProperty(value = "创建实例：数据库类型")
    public String rdbms_type;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "创建实例：平台")
    public String platform;
    @ApiModelProperty(value = "创建实例：主机名")
    public String hostname;
    @ApiModelProperty(value = "创建实例：端口")
    public String port;
    @ApiModelProperty(value = "创建实例：网络协议")
    public String protocol;
    @ApiModelProperty(value = "创建实例：用户名")
    public String userName;
    @ApiModelProperty(value = "创建实例：密码")
    public String password;
    @ApiModelProperty(value = "联系人信息")
    public String contact_info;
    @ApiModelProperty(value = "评论")
    public String comment;
    @ApiModelProperty(value = "描述")
    public String description;
    @ApiModelProperty(value = "添加字段：字段类型")
    public String data_type;
    @ApiModelProperty(value = "添加字段：字段长度")
    public String length;
    @ApiModelProperty(value = "所属人(后台自动获取)")
    public String owner;
    @ApiModelProperty(value = "添加数据库时,需要实例guid和typeName")
    public EntityIdAndTypeDTO instance;
    @ApiModelProperty(value = "添加表时,需要数据库guid和typeName")
    public EntityIdAndTypeDTO db;
    @ApiModelProperty(value = "添加字段时,需要表guid和typeName")
    public EntityIdAndTypeDTO table;
    @ApiModelProperty(value = "添加血缘时,输入参数guid和typeName")
    public List<EntityIdAndTypeDTO> inputs;
    @ApiModelProperty(value = "添加血缘时,输出参数guid和typeName")
    public List<EntityIdAndTypeDTO> outputs;
}
