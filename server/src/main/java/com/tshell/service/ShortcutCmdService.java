package com.tshell.service;

import cn.hutool.core.collection.CollectionUtil;
import com.tshell.core.client.TtyType;
import com.tshell.module.dto.shortcutCmd.AddShortcutCmdDTO;
import com.tshell.module.dto.shortcutCmd.AddShortcutCmdImplDTO;
import com.tshell.module.dto.shortcutCmd.UpdShortcutCmdDTO;
import com.tshell.module.dto.shortcutCmd.UpdShortcutCmdImplDTO;
import com.tshell.module.entity.*;
import com.tshell.module.vo.ShortcutCmdImplVO;
import com.tshell.module.vo.ShortcutCmdVO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.*;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author TheBlind
 */

@ApplicationScoped
public class ShortcutCmdService {

    @Inject
    EntityManager em;
    @Inject
    UserTransaction userTransaction;


    public boolean create(AddShortcutCmdDTO addShortcutCmdDTO) throws Exception {


        if (isNameExists(addShortcutCmdDTO.name())) {
            throw new WebApplicationException("该变量名称已存在", 500);
        }
        ShortcutCmd shortcutCmd = addShortcutCmdDTO.convert();
        userTransaction.begin();
        shortcutCmd.persist();
        userTransaction.commit();
        int shortcutCmdId = shortcutCmd.id;


        List<Integer> shortcutCmdGroupIdList = addShortcutCmdDTO.shortcutCmdGroupIdList();
        userTransaction.begin();
        if (CollectionUtil.isNotEmpty(shortcutCmdGroupIdList)) {
            shortcutCmdGroupIdList.forEach((shortcutCmdGroupId) -> {
                GroupShortcutCmd groupShortcutCmd = new GroupShortcutCmd();
                groupShortcutCmd.setShortcutCmdId(shortcutCmdId);
                groupShortcutCmd.setShortcutCmdGroupId(shortcutCmdGroupId);
                groupShortcutCmd.persist();
            });
        }


        List<AddShortcutCmdImplDTO> addShortcutCmdImplDTOList = addShortcutCmdDTO.shortcutCmdImplList();
        if (CollectionUtil.isNotEmpty(addShortcutCmdImplDTOList)) {
            for (AddShortcutCmdImplDTO addShortcutCmdImplDTO : addShortcutCmdImplDTOList) {
                ShortcutCmdImpl shortcutCmdImpl = addShortcutCmdImplDTO.convert();
                shortcutCmdImpl.setShortcutCmdId(shortcutCmdId);
                shortcutCmdImpl.persist();

                List<Integer> ttyTypeIdList = addShortcutCmdImplDTO.shortcutCmdTtyTypeIdList();

                if (CollectionUtil.isNotEmpty(ttyTypeIdList)) {
                    List<Integer> completeList = new ArrayList<>(ttyTypeIdList.size() * 2);
                    ttyTypeIdList.forEach(osTypeTypeId -> {
                        List<Integer> subIdList = TtyType.getSubIdList(osTypeTypeId);

                        long count = subIdList.stream().filter(ttyTypeIdList::contains).count();
                        // 只选择父节点
                        if (count == 0 && !subIdList.isEmpty()) {
                            completeList.addAll(subIdList);
                        }
                        completeList.add(osTypeTypeId);

                    });

                    completeList.forEach(osTypeId -> ShortcutCmdImplTtyType.builder().ttyTypeId(osTypeId).shortcutCmdImplId(shortcutCmdImpl.id).build().persist());

                }
            }
        }
        userTransaction.commit();
        return true;
    }

    public boolean isNameExists(String name) throws Exception {
        try {
            userTransaction.begin();
            return ShortcutCmd.count("name =?1", name) != 0;
        } finally {
            userTransaction.commit();
        }
    }

    public ShortcutCmdVO getSingle(Integer shortcutCmdId) {

        ShortcutCmd shortcutCmd = ShortcutCmd.findById(shortcutCmdId);
        List<GroupShortcutCmd> groupList = GroupShortcutCmd.list("shortcutCmdId = ?1", shortcutCmdId);
        List<Integer> shortcutCmdGroupIdList = groupList.stream().map(GroupShortcutCmd::getShortcutCmdGroupId).toList();

        List<ShortcutCmdImpl> shortcutCmdImplList = ShortcutCmdImpl.list("shortcutCmdId = ?1", shortcutCmdId);

        List<ShortcutCmdImplVO> shortcutCmdImplVOList = new ArrayList<>(shortcutCmdImplList.size());
        shortcutCmdImplList.forEach(shortcutCmdImpl -> {
            List<ShortcutCmdImplTtyType> cmdOsTypeList = ShortcutCmdImplTtyType.list("shortcutCmdImplId = ?1", shortcutCmdImpl.id);

            List<Integer> cmdOsTypeIdList = cmdOsTypeList.stream().map(ShortcutCmdImplTtyType::getTtyTypeId).toList();

            List<String> cmdOsTypeNameList = Arrays.stream(TtyType.values()).filter(ptyOsType ->
                    cmdOsTypeIdList.contains(ptyOsType.getId())
            ).map(TtyType::getName).toList();


            shortcutCmdImplVOList.add(new ShortcutCmdImplVO(shortcutCmdImpl.getCmdTemplate(), cmdOsTypeIdList, cmdOsTypeNameList));
        });

        return new ShortcutCmdVO(shortcutCmdId, shortcutCmd.getName(), shortcutCmdGroupIdList, shortcutCmdImplVOList);
    }


    @Transactional(rollbackOn = Exception.class)
    public boolean delete(Integer id) {
        if (ShortcutCmd.delete("id = ?1 ", id) > 0) {
            List<ShortcutCmdImpl> list = ShortcutCmdImpl.<ShortcutCmdImpl>list("shortcutCmdId = ?1", id);
            List<Integer> collect = list.stream().mapToInt(ShortcutCmdImpl::getId).boxed().collect(Collectors.toList());

            ShortcutCmdImpl.delete("id in ?1", collect);
            ShortcutCmdImplTtyType.delete("shortcutCmdImplId in ?1", collect);

            return true;
        }
        return false;
    }


    public boolean update(UpdShortcutCmdDTO updShortcutCmdDTO) throws Exception {

        Integer shortcutCmdId = updShortcutCmdDTO.id();

        ShortcutCmd shortcutCmd = ShortcutCmd.<ShortcutCmd>findByIdOptional(shortcutCmdId).orElseThrow(() -> new WebApplicationException(shortcutCmdId + "不存在"));
        if (!Objects.equals(updShortcutCmdDTO.name(), shortcutCmd.getName()) && isNameExists(updShortcutCmdDTO.name())) {
            throw new WebApplicationException("该变量名称已存在", 500);
        }
        userTransaction.begin();
        updShortcutCmdDTO.copyProperty(shortcutCmd);

        GroupShortcutCmd.delete("shortcutCmdId = ?1", shortcutCmdId);
        ShortcutCmdImpl.delete("shortcutCmdId = ?1", shortcutCmdId);
        userTransaction.commit();
        List<Integer> shortcutCmdGroupIdList = updShortcutCmdDTO.shortcutCmdGroupIdList();
        if (CollectionUtil.isNotEmpty(shortcutCmdGroupIdList)) {
            userTransaction.begin();
            shortcutCmdGroupIdList.stream().map((shortcutCmdGroupId) -> {
                GroupShortcutCmd groupShortcutCmd = new GroupShortcutCmd();
                groupShortcutCmd.setShortcutCmdId(shortcutCmdId);
                groupShortcutCmd.setShortcutCmdGroupId(shortcutCmdGroupId);
                return groupShortcutCmd;
            }).forEach(groupShortcutCmd ->
            {
                try {
                    groupShortcutCmd.persist();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            userTransaction.commit();
        }

        List<UpdShortcutCmdImplDTO> addShortcutCmdImplDTOList = updShortcutCmdDTO.shortcutCmdImplList();
        if (CollectionUtil.isNotEmpty(addShortcutCmdImplDTOList)) {
            for (UpdShortcutCmdImplDTO addShortcutCmdImplDTO : addShortcutCmdImplDTOList) {
                ShortcutCmdImpl shortcutCmdImpl = addShortcutCmdImplDTO.convert();
                shortcutCmdImpl.setShortcutCmdId(shortcutCmdId);
                userTransaction.begin();
                shortcutCmdImpl.persist();
                List<Integer> ttyTypeIdList = addShortcutCmdImplDTO.shortcutCmdTtyTypeIdList();
                ShortcutCmdImplTtyType.delete("shortcutCmdImplId = ?1", addShortcutCmdImplDTO.id());
                userTransaction.commit();
                if (CollectionUtil.isNotEmpty(ttyTypeIdList)) {
                    userTransaction.begin();
                    ttyTypeIdList.forEach(ttyTypeId -> ShortcutCmdImplTtyType.builder().ttyTypeId(ttyTypeId).shortcutCmdImplId(shortcutCmdImpl.id).build().persist());
                    userTransaction.commit();
                }

            }
        }

        return true;
    }


    @Transactional(rollbackOn = Exception.class)
    public List<ShortcutCmd> getShortcutCmdByNameAndOsTypeId(String name, Integer ttyTypeId) {

        String sql = """
                SELECT
                   sc.*
                 FROM
                   ShortcutCmd AS sc
                   LEFT JOIN ShortcutCmdImpl AS sci ON sci.shortcutCmdId = sc.id
                   LEFT JOIN ShortcutCmdImplTtyType AS scio ON scio.shortcutCmdImplId = sci.id
                 WHERE
                   (scio.ttyTypeId = :ttyTypeId  or scio.ttyTypeId is null)
                    AND sc.name != :name
                   AND sc.name LIKE :likeName 
                   group by sc.id
                   """;

        Query nativeQuery = em.createNativeQuery(sql, ShortcutCmd.class);
        nativeQuery.setParameter("likeName", String.format("%s%%", name));
        nativeQuery.setParameter("name", name);
        nativeQuery.setParameter("ttyTypeId", ttyTypeId);

        return nativeQuery.getResultList();
    }


    @Transactional(rollbackOn = Exception.class)
    public String getShortcutCmdByIdAndOsTypeId(Integer id, Integer ttyTypeId) {
        String sql = """
                SELECT
                	sci.* \s
                FROM
                	ShortcutCmdImpl AS sci
                	LEFT JOIN ShortcutCmdImplTtyType AS scio ON scio.shortcutCmdImplId = sci.id
                WHERE
                	 (scio.ttyTypeId = :ttyTypeId  or scio.ttyTypeId is null)
                	AND sci.shortcutCmdId = :id
                	order by scio.ttyTypeId desc 
                	limit 1
                	""";

        Query nativeQuery = em.createNativeQuery(sql, ShortcutCmdImpl.class);
        nativeQuery.setParameter("id", id);
        nativeQuery.setParameter("ttyTypeId", ttyTypeId);
        ShortcutCmdImpl singleResult = (ShortcutCmdImpl) nativeQuery.getSingleResult();
        if (singleResult == null) {
            throw new IllegalArgumentException("异常");
        }
        return singleResult.getCmdTemplate();
    }

}
