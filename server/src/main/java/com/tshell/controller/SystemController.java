package com.tshell.controller;


import io.quarkus.runtime.Quarkus;
import io.smallrye.common.annotation.RunOnVirtualThread;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/system")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class SystemController {


    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Path("/shutdown")
    public void shutdown(){
        Quarkus.asyncExit(0);
    }


}
