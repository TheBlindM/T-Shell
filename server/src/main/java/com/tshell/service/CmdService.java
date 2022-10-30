package com.tshell.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.tshell.module.dto.cmd.*;
import com.tshell.module.entity.CmdOsType;
import com.tshell.module.entity.Option;
import com.tshell.module.entity.Parameter;
import com.tshell.module.vo.CmdOptionVO;
import com.tshell.module.vo.CmdParameterVO;
import com.tshell.module.vo.CmdVO;
import com.tshell.module.entity.Cmd;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author TheBlind
 * @date 2022/4/2
 */

@ApplicationScoped
public class CmdService {


    @Inject
    EntityManager em;

    public boolean create(AddCmdDto addCmdDto) {
        Cmd cmd = addCmdDto.convert();
        cmd.persist();
        Integer cmdId = cmd.id;


        List<AddCmdParameterDTO> cmdParameterList = addCmdDto.cmdParameterList();

        if (CollectionUtil.isNotEmpty(cmdParameterList)) {
            List<Parameter> parameterList = cmdParameterList.stream().map(AddCmdParameterDTO::convert).collect(Collectors.toUnmodifiableList());
            parameterList.forEach(parameter -> {
                parameter.setCmdId(cmdId);
                parameter.persist();
            });
        }

        List<Integer> osTypeIdList = addCmdDto.cmdOsTypeIdList();

        if (CollectionUtil.isNotEmpty(osTypeIdList)) {
            osTypeIdList.forEach(osTypeId -> CmdOsType.builder().osTypeId(osTypeId).cmdId(cmdId).build().persist());

        }

        List<AddCmdOptionDTO> cmdOptionList = addCmdDto.cmdOptionList();
        if (CollectionUtil.isNotEmpty(cmdOptionList)) {
            cmdOptionList.stream().forEach(cmdOsTypeDTO -> {
                List<String> optionNameList = cmdOsTypeDTO.optionNameList();
                if (CollectionUtil.isNotEmpty(optionNameList)) {
                    Option.builder().description(cmdOsTypeDTO.description()).cmdId(cmdId).jsonNames(JSONUtil.toJsonStr(optionNameList)).build().persist();
                }
            });
        }
        return true;
    }


    public CmdVO getSingle(Integer id) {


        Cmd cmd = Cmd.<Cmd>findByIdOptional(id).orElseThrow(() -> new WebApplicationException("Fruit with id of " + id + " does not exist.", 404));

        List<CmdOsType> cmdOsTypeList = CmdOsType.<CmdOsType>list("cmdId = ?1", id);

        List<Integer> cmdOsTypeIdList = cmdOsTypeList.stream().map(CmdOsType::getOsTypeId).collect(Collectors.toUnmodifiableList());


        List<Parameter> parameterList = Parameter.<Parameter>list("cmdId = ?1", id);
        List<CmdParameterVO> cmdParameterVOList = parameterList.stream().map(parameter -> new CmdParameterVO(parameter.id, parameter.getIndex(), parameter.getDescription())).collect(Collectors.toUnmodifiableList());


        List<Option> optionList = Option.<Option>list("cmdId = ?1", id);
        List<CmdOptionVO> cmdOptionVOList = optionList.stream().map(option -> new CmdOptionVO(option.id, JSONUtil.toList(option.getJsonNames(), String.class), option.getDescription())).collect(Collectors.toUnmodifiableList());


        return CmdVO.convert(cmd, cmdOsTypeIdList, cmdParameterVOList, cmdOptionVOList);
    }


    public boolean delete(Integer id) {
        if (Cmd.delete("id = ?1 or parentCmdId=?1", id) > 0) {
            Option.delete("cmdId = ?1", id);
            CmdOsType.delete("cmdId = ?1", id);
            Parameter.delete("cmdId = ?1", id);
            return true;
        }
        return false;
    }


    public boolean update(UpdCmdDto updCmdDto) {
        Integer cmdId = updCmdDto.id();
        Cmd cmd = Cmd.<Cmd>findByIdOptional(cmdId).orElseThrow(() -> new WebApplicationException(cmdId + "不存在"));
        updCmdDto.copyProperty(cmd);

        List<UpdCmdParameterDTO> updCmdParameterDTOList = updCmdDto.cmdParameterList();
        Parameter.delete("cmdId = ?1", cmdId);
        if (CollectionUtil.isNotEmpty(updCmdParameterDTOList)) {
            updCmdParameterDTOList.stream().map(updCmdParameterDTO -> Parameter.builder().index(updCmdParameterDTO.index()).cmdId(cmdId).description(updCmdParameterDTO.description()).build()).forEach(parameter -> parameter.persist());
        }


        List<UpdCmdOptionDTO> updCmdOptionDTOList = updCmdDto.cmdOptionList();
        Option.delete("cmdId = ?1", cmdId);
        if (CollectionUtil.isNotEmpty(updCmdOptionDTOList)) {
            updCmdOptionDTOList.forEach(cmdOsTypeDTO -> {
                List<String> optionNameList = cmdOsTypeDTO.optionNameList();
                if (CollectionUtil.isNotEmpty(optionNameList)) {
                    Option.builder().description(cmdOsTypeDTO.description()).cmdId(cmdId).jsonNames(JSONUtil.toJsonStr(optionNameList)).build().persist();
                }
            });
        }

        List<Integer> updCmdOsTypeIdList = updCmdDto.cmdOsTypeIdList();
        CmdOsType.delete("cmdId = ?1", cmdId);
        if (CollectionUtil.isNotEmpty(updCmdOsTypeIdList)) {
            updCmdOsTypeIdList.forEach(osTypeId -> CmdOsType.builder().osTypeId(osTypeId).cmdId(cmdId).build().persist());
        }


        return true;
    }


    public List<Cmd> getListByOsTypeId(Integer osTypeId) {
        Query nativeQuery = em.createNativeQuery("SELECT c.* from Cmd as c  LEFT JOIN CmdOsType as co ON co.cmdId=c.id  where co.osTypeId=?1", Cmd.class);
        nativeQuery.setParameter(1, osTypeId);
        return nativeQuery.getResultList();
    }


    @Transactional(rollbackOn = Exception.class)
    public List<Cmd> getListByOsTypeIdAndCmdText(Integer osTypeId,String cmdText) {
        Query nativeQuery = em.createNativeQuery("SELECT c.* from Cmd as c  LEFT JOIN CmdOsType as co ON co.cmdId=c.id  where co.osTypeId = :osTypeId and  c.cmdText != :cmdText  and c.cmdText like  :likeCmdText ", Cmd.class);
        nativeQuery.setParameter("osTypeId", osTypeId);
        nativeQuery.setParameter("likeCmdText", String.format("%s%%",cmdText));
        nativeQuery.setParameter("cmdText", cmdText);
        return nativeQuery.getResultList();
    }
}
