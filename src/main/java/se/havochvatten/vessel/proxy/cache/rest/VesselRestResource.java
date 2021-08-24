/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package se.havochvatten.vessel.proxy.cache.rest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.havochvatten.service.client.vesselws.v2_1.vessel.Vessel;
import se.havochvatten.vessel.proxy.cache.bean.VesselServiceBean;
import se.havochvatten.vessel.proxy.cache.mapper.RequestMapper;

@Path("vessel")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
public class VesselRestResource {

    private static final Logger LOG = LoggerFactory.getLogger(VesselRestResource.class);
    
    @Inject
    private VesselServiceBean vesselService;
    
    @GET
    @Path("/ircs/{ircs}")
    public Response updateAssetByIrcs(@PathParam("ircs") String ircs) {
        LOG.info("Updating asset with ircs {}", ircs);
        try {
            Vessel vessel = vesselService.getVesselByIrcs(ircs);
            if (vessel == null) {
                vessel = RequestMapper.mapEuFormatToVessel(vesselService.getVesselEuFormatByIrcs(ircs));
            }
            vesselService.enrichVesselAndSendToAsset(vessel);
            return Response.ok("OK").build();
        } catch (Exception e) {
            LOG.error("Error when getting asset by ircs {}", ircs, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }
    
    @GET
    @Path("/cfr/{cfr}")
    public Response updateAssetByCfr(@PathParam("cfr") String cfr) {
        LOG.info("Updating asset with ircs {}", cfr);
        try {
            Vessel vessel = vesselService.getVesselByCfr(cfr);
            if (vessel == null) {
                vessel = RequestMapper.mapEuFormatToVessel(vesselService.getVesselEuFormatByCfr(cfr));
            }
            vesselService.enrichVesselAndSendToAsset(vessel);
            return Response.ok("OK").build();
        } catch (Exception e) {
            LOG.error("Error when getting asset by ircs {}", cfr, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @GET
    @Path("inactivate")
    public Response inactivateVessel() {
        LOG.info("Inactivating vessels");
        vesselService.inactivateVessels();
        return Response.ok("OK").build();
    }
}
