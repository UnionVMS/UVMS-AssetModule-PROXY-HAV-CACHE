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

package se.havochvatten.vessel.proxy.cache.timer;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.havochvatten.service.client.vesselws.v2_1.vessel.Vessel;
import se.havochvatten.vessel.proxy.cache.bean.VesselServiceBean;

@Singleton
@Startup
public class VesselServiceTimer {

    private static final Logger LOG = LoggerFactory.getLogger(VesselServiceTimer.class);

    @Inject
    private VesselServiceBean vesselServiceBean;

    @Resource(lookup="java:/UvmsVesselCacheProxyExecutorService")
    private ManagedScheduledExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService.schedule(this::updateVessels, 6, TimeUnit.MINUTES);
    }

    @Schedule(hour = "2")
    public void updateVessels() {
        LOG.info("VesselServiceTask run!");
        try {
            long start = System.currentTimeMillis();
            List<String> nations = vesselServiceBean.getNationsFromDatabase();
            for (String nation : nations) {
                List<Vessel> vesselList = vesselServiceBean.getVesselList(nation);
                LOG.info("Found {} assets for nation: {}", vesselList.size(), nation);
                vesselServiceBean.enrichVesselsAndSendToAsset(vesselList);
            }
            long tot = System.currentTimeMillis() - start;
            LOG.info("--------------- VesselServiceTask total time {} s      -------------", tot / 1000);
        } catch (Exception e) {
            LOG.error("Something went wrong in VesselServiceTask", e);
        }
    }

    @PreDestroy
    public void destroy(){
        if(!executorService.isShutdown()){
            executorService.shutdownNow();
        }
    }
}
