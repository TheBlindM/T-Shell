package com.tshell.service;

import cn.hutool.core.date.DateUtil;
import com.tshell.module.entity.ConnectionLog;
import com.tshell.module.vo.connectionLog.TopVO;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.sql.internal.NativeQueryImpl;
import org.hibernate.transform.AliasToBeanResultTransformer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * @author TheBlind
 * @version 1.0
 */

@ApplicationScoped
public class ConnectionLogService {

    @Inject
    EntityManager em;


    @Transactional(rollbackOn = Exception.class)
    public void start(String sessionId) {
        ConnectionLog.builder()
                .startTime(DateUtil.now())
                .sessionId(sessionId).build().persist();
    }

    public List<TopVO> topList() {
        String sql = """
                 select cl.sessionId, s.sessionName
                                   from ConnectionLog as cl
                                   join Session as s on  s.id=cl.sessionId
                                   group by sessionId
                                   order by  count(1) desc
                                   limit 10
                """;

        NativeQuery nativeQuery = em.createNativeQuery(sql).unwrap(NativeQueryImpl.class);
        nativeQuery.setResultTransformer(new AliasToBeanResultTransformer(TopVO.class));
        return nativeQuery.getResultList();
    }


}
