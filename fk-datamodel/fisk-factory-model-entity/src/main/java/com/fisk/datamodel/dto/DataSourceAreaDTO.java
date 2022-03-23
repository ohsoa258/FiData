package com.fisk.datamodel.dto;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DataSourceAreaDTO extends BaseDTO {

    public long id;

    public String datasourceName;

    public String datasourceDes;

    public String databaseName;

    public String datasourceAddress;

    public String datasourceAccount;

    public String datasourcePwd;

    public DataSourceAreaDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<DataSourceAreaDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(DataSourceAreaDTO::new).collect(Collectors.toList());
    }

}
