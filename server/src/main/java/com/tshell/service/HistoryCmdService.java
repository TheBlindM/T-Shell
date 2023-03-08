package com.tshell.service;


import cn.hutool.core.date.LocalDateTimeUtil;
import com.tshell.module.dto.PageDTO;
import com.tshell.module.dto.historyCmd.AddHistoryCmdDTO;
import com.tshell.module.dto.historyCmd.GetHistoryCmdPageDTO;
import com.tshell.module.entity.HistoryCmd;
import com.tshell.module.vo.PageVO;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TheBlind
 */
@ApplicationScoped
public class HistoryCmdService {


    public void create(AddHistoryCmdDTO addHistoryCmdDTO) {
        HistoryCmd historyCmd = addHistoryCmdDTO.convert();
        historyCmd.persist();
    }

    public PageVO<HistoryCmd> getPage(PageDTO<GetHistoryCmdPageDTO> pageDTO) {
        GetHistoryCmdPageDTO param = pageDTO.param();
        StringBuilder hql = new StringBuilder("sessionId = :sessionId");
        Map<String, Object> params = HashMap.newHashMap(3);
        params.put("sessionId", param.sessionId());
        if (param.startDate() != null) {
            hql.append(" and  date(createTime) >= :startDate");
            params.put("startDate", LocalDateTimeUtil.format(param.startDate(), "yyyy-MM-dd"));
        }
        if (param.endDate() != null) {
            hql.append(" and  date(createTime) <= :endDate");
            params.put("endDate", LocalDateTimeUtil.format(param.endDate(), "yyyy-MM-dd"));
        }
        PanacheQuery<HistoryCmd> page = HistoryCmd.find(hql.toString(), params).page(pageDTO.page(), pageDTO.size());
        return new PageVO(page.pageCount(), page.list());
    }

    @Inject
    EntityManager em;

    @Transactional(rollbackOn = Exception.class)
    public List<HistoryCmd> getHistoryCmdByNameAndOsTypeId(String cmdText, Integer ttyTypeId) {
        String sql = """
                SELECT
                    hc.*
                FROM
                    HistoryCmd AS hc
                        LEFT JOIN Session AS s ON s.id = hc.sessionId
                                
                WHERE
                        s.ttyTypeId = :ttyTypeId
                  AND  hc.cmdText != :cmdText
                  AND hc.cmdText LIKE :likeCmdText
                  group by hc.cmdText
                  order by instr(hc.cmdText,:cmdText), length(hc.cmdText) , hc.createTime desc 
                  limit 3 
                  """;



        Query nativeQuery = em.createNativeQuery(sql, HistoryCmd.class);
        nativeQuery.setParameter("likeCmdText", String.format("%s%%", cmdText));
        nativeQuery.setParameter("cmdText",cmdText);
        nativeQuery.setParameter("ttyTypeId", ttyTypeId);
        return nativeQuery.getResultList();
    }


}
