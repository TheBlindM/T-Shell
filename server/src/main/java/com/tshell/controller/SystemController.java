package com.tshell.controller;


import io.quarkus.runtime.Quarkus;
import io.smallrye.common.annotation.RunOnVirtualThread;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
