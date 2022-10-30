package com.tshell.service;

import com.tshell.module.entity.GlobalVariable;
import com.tshell.utils.PlaceholderResolver;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author TheBlind
 * @date 2022/8/11
 */
@ApplicationScoped
public class VariableService {


    @Inject
    EntityManager entityManager;


    public ResolveResult parsePlaceholders(String value) {

        AtomicBoolean whole = new AtomicBoolean(true);
        String v = PlaceholderResolver.getDefaultResolver().resolveByRule(value, (var) -> {
            Optional<String> valueByVarName = this.getValueByVarName(var);
            if (!valueByVarName.isPresent()) {
                whole.set(false);
            }
            return valueByVarName.orElse(var);
        });
        return new ResolveResult(value, whole.get());
    }

    public Map<String, String> getMatchItems(String value) {
        HashMap<String, String> result = HashMap.<String, String>newHashMap(5);
        PlaceholderResolver.getDefaultResolver().resolveByRule(value, (var) -> {
            Optional<String> valueByVarName = this.getValueByVarName(var);
            result.put(var, valueByVarName.orElse(null));
            return valueByVarName.orElse(var);
        });
        return result;
    }


    public Optional<String> getValueByVarName(String varName) {
        String sql = """
                SELECT
                 *
                FROM
                 GlobalVariable
                WHERE
                 varName = :varName
                 LIMIT 1 """;
        Query nativeQuery = entityManager.createNativeQuery(sql, GlobalVariable.class);
        nativeQuery.setParameter("varName", varName);
        List<GlobalVariable> resultList = nativeQuery.getResultList();

        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(resultList.get(0).getValue());
    }


    public record ResolveResult(
            String value,
            boolean whole
    ) {
    }


}
