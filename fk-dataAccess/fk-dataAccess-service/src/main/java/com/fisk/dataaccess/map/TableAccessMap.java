package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableAccessNonDTO;
import com.fisk.dataaccess.dto.datafactory.TableIdAndNameDTO;
import com.fisk.dataaccess.dto.datamodel.TableAccessDataDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.vo.datafactory.TableIdAndNameVO;
import com.fisk.datafactory.dto.components.ChannelDataChildDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableAccessMap {

    TableAccessMap INSTANCES = Mappers.getMapper(TableAccessMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    TableAccessPO dtoToPo(TableAccessDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    TableAccessDTO poToDto(TableAccessPO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    TableAccessNonDTO poToDtoNon(TableAccessPO po);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<TableAccessDTO> listPoToDto(List<TableAccessPO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<TableAccessPO> listDtoToPo(List<TableAccessDTO> list);

    /**
     * list集合 dto -> po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "tableName", target = "name")
    })
    TableIdAndNameVO tableDtoToPo(TableIdAndNameDTO dto);

    /**
     * list dto -> po
     *
     * @param list list
     * @return target
     */
    List<TableIdAndNameVO> tableDtosToPos(List<TableIdAndNameDTO> list);

    /**
     * poList==>DtoList
     *
     * @param list list
     * @return target
     */
    List<TableAccessDataDTO> poListToDtoList(List<TableAccessPO> list);

    /**
     * po -> dto
     *
     * @param po po
     * @return target
     */
    TbTableAccessDTO tbPoToDto(TableAccessPO po);

    /**
     * dto->po
     *
     * @param dto dto
     * @return target
     */
    TableAccessPO tbDtoToPo(TbTableAccessDTO dto);

    /**
     * poList==>DtoList
     *
     * @param list list
     * @return target
     */
    List<TbTableAccessDTO> listTbPoToDto(List<TableAccessPO> list);

    /**
     * DtoList==>poList
     *
     * @param list list
     * @return target
     */
    List<TableAccessPO> listTbDtoToPo(List<TbTableAccessDTO> list);

    /**
     * list: po -> dto
     *
     * @param list source
     * @return target
     */
    List<ChannelDataChildDTO> listPoToChannelDataDto(List<TableAccessPO> list);
}
