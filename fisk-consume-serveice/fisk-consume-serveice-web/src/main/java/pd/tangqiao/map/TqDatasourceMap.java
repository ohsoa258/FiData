package pd.tangqiao.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import pd.tangqiao.entity.TqDatasourceConfigDTO;
import pd.tangqiao.entity.TqDatasourceConfigPO;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TqDatasourceMap {

    TqDatasourceMap INSTANCES = Mappers.getMapper(TqDatasourceMap.class);

    /**
     * po -> dto
     */
    @Mapping(target = "conType", source = "conType", ignore = true)
    TqDatasourceConfigDTO poToDTO(TqDatasourceConfigPO po);

}
