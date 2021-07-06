package com.fisk.system.dto;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServiceRegistryDTO extends BaseDTO {

    public  int id;

    /**
     *服务code
    */
    public  String serveCode;

    /**
    *上一级服务code
    */
    public  String parentServeCode;

    /**
    *服务中文名称
    */
    public  String serveCnName;

    /**
    *服务英文名称
    */
    public  String serveEnName;

    /**
    *服务url
    */
    public  String serveUrl;

    /**
    *服务图标
     */
    public  String icon;

    /**
     *排序号
     */
    public  int sequenceNo;

    /**
    *父级服务下一级服务list
    */
    public List<ServiceRegistryDTO> dtos;


    public ServiceRegistryDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<ServiceRegistryDTO> convertEntityList(Collection<T> list){
        return list.stream().map(ServiceRegistryDTO::new).collect(Collectors.toList());
    }
}
