package com.tshell.service;

import com.tshell.module.dto.hostGroup.AddHostGroupDTO;
import com.tshell.module.dto.hostGroup.UpdHostGroupDTO;
import com.tshell.module.entity.*;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.WebApplicationException;

/**
 * @author TheBlind
 * @date 2022/4/2
 */

@ApplicationScoped
public class HostGroupService {


    public boolean create(AddHostGroupDTO addHostGroupDTO) {
        SessionGroup sessionGroup = addHostGroupDTO.convert();
        sessionGroup.persist();
        return true;
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


    public boolean update(UpdHostGroupDTO updHostGroupDTO) {
        SessionGroup sessionGroup = SessionGroup.<SessionGroup>findByIdOptional(updHostGroupDTO.id()).orElseThrow(() -> new WebApplicationException("不存在"));
        updHostGroupDTO.copyProperty(sessionGroup);
        return true;
    }

}
