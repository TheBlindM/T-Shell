package com.tshell.service;

import com.tshell.module.dto.session.AddSshSessionDTO;
import com.tshell.module.dto.session.UpdSshSessionDTO;
import com.tshell.module.entity.Session;
import com.tshell.module.entity.SshSession;
import com.tshell.module.vo.SshSessionVO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

/**
 * @author TheBlind
 */

@ApplicationScoped
public class SshSessionService {


    public SshSessionVO getSingle(String id) {
        Session session = Session.findById(id);
        SshSession sshSession = SshSession.<SshSession>find("sessionId = ?1", id).singleResultOptional().orElseThrow(() -> new WebApplicationException("host with sessionId of not exist", 404));
        return new SshSessionVO(session.id, sshSession.getIp(), sshSession.getPort(), sshSession.getUsername(), sshSession.getPwd(), session.getSessionGroupId(), session.getSessionName(), session.getTtyTypeId(),sshSession.getAuthType(),sshSession.getPrivateKeyFile(), sshSession.getPassphrase(),sshSession.getProxyHost(),sshSession.getProxyPort(),sshSession.getProxyType());
    }

    public boolean create(AddSshSessionDTO addSshSessionDTO) {
        Session session = addSshSessionDTO.convertSession();
        session.persist();
        SshSession sshSession = addSshSessionDTO.convertSshSession();
        sshSession.setSessionId(session.id);
        sshSession.persist();
        return true;
    }

    public boolean update(UpdSshSessionDTO updSshSessionDTO) {
        String id = updSshSessionDTO.sessionId();
        Session session = Session.<Session>findByIdOptional(id).orElseThrow(() -> new WebApplicationException("host with sessionId of not exist", 404));
        updSshSessionDTO.copyProperty(session);
        SshSession sshSession = SshSession.<SshSession>find("sessionId = ?1", id).singleResultOptional().orElseThrow(() -> new WebApplicationException("host with sessionId of not exist", 404));
        updSshSessionDTO.copyProperty(sshSession);
        return true;
    }


    public boolean updGroup(int id, int groupId) {
        Session session = Session.<Session>findByIdOptional(id).orElseThrow(() -> new WebApplicationException("host with sessionId of not exist", 404));
        session.setSessionGroupId(groupId);
        return true;
    }


}
