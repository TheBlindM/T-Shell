package com.tshell.controller;

import cn.hutool.core.util.StrUtil;
import com.tshell.common.response.BaseResponse;
import com.tshell.common.response.PageData;
import com.tshell.module.dto.globalVariable.AddGlobalVar;
import com.tshell.module.dto.globalVariable.UpdGlobalVar;
import com.tshell.module.entity.GlobalVariable;
import com.tshell.module.entity.ShortcutCmd;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.*;
import javax.transaction.NotSupportedException;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

/**
 * 全局变量
 *
 * @author TheBlind
 * @date 2022/7/4
 */
@Path("/globalVariable")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class GlobalVariableController {

    private static final Logger log = LoggerFactory.getLogger(GlobalVariableController.class);

    @GET
    public Response get() {
        // Sort.by("id")
        return Response.ok(GlobalVariable.listAll()).build();
    }


    @GET
    @Path("{id}")
    public BaseResponse getSingle(@PathParam("id") Integer id) {
        return GlobalVariable.findByIdOptional(id).map(var -> BaseResponse.ok(var))
                .orElse(BaseResponse.err(Response.Status.NOT_FOUND, "Fruit with id of " + id + " does not exist."));
    }


    @Inject
    UserTransaction userTransaction;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class, value = Transactional.TxType.NEVER)
    public BaseResponse<GlobalVariable> create(@Valid AddGlobalVar addGlobalVar) throws Exception {
        if (isNameExists(addGlobalVar.varName())) {
            throw new WebApplicationException("该变量名称已存在", 500);
        }
        userTransaction.begin();
        GlobalVariable convert = addGlobalVar.convert();
        convert.persist();
        userTransaction.commit();
        return BaseResponse.ok(convert);
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse<GlobalVariable> update(@PathParam("id") Integer id, @Valid UpdGlobalVar updGlobalVar) throws Exception {

        GlobalVariable entity = GlobalVariable.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }

        if (!Objects.equals(updGlobalVar.varName(), entity.getVarName()) && GlobalVariable.count("varName = ?1", updGlobalVar.varName()) != 0) {
            throw new WebApplicationException("该变量名称已存在", 500);
        }

        updGlobalVar.copyProperty(entity);

        return BaseResponse.ok(entity);
    }


    private boolean isNameExists(String name) throws Exception {
        try {
            userTransaction.begin();
            return GlobalVariable.count("varName =?1", name) != 0;
        } catch (SystemException | NotSupportedException e) {
            throw new RuntimeException(e);
        } finally {
            userTransaction.commit();
        }
    }




    @DELETE
    @Path("{id}")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse delete(@PathParam("id") Integer id) {
        GlobalVariable entity = GlobalVariable.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return BaseResponse.ok();
    }


    @GET
    @Path("/page")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse page(@QueryParam("name") String name,
                             @DefaultValue(value = "1") @QueryParam("page") Integer pageIndex,
                             @DefaultValue(value = "10") @QueryParam("pageSize") Integer pageSize) {
        PanacheQuery<GlobalVariable> query = null;
        if (StrUtil.isBlank(name)) {
            query = GlobalVariable.findAll();
        } else {
            query = GlobalVariable.find("varName like ?1", Sort.ascending("id"), String.format("%s%%", name));
        }
        PanacheQuery<GlobalVariable> page = query.page(Page.of(pageIndex - 1, pageSize));
        return BaseResponse.ok(new PageData(pageIndex, page.list(), page.pageCount(), page.list().size()));
    }


    @GET
    @Path("matchItem{id}")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse getMatchItem(@PathParam("id") Integer id) {
        GlobalVariable entity = GlobalVariable.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return BaseResponse.ok();
    }

}
