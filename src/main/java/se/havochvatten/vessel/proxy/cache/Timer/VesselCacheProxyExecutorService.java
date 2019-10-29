package se.havochvatten.vessel.proxy.cache.Timer;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.havochvatten.vessel.proxy.cache.bean.GearTypesServiceBean;
import se.havochvatten.vessel.proxy.cache.bean.VesselServiceBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Singleton
@Startup
public class VesselCacheProxyExecutorService {

    @EJB
     private VesselServiceBean vesselServiceBean;

    @EJB
    private GearTypesServiceBean gearTypesServiceBean;

    @Resource(lookup="java:/UvmsVesselCacheProxyExecutorService")
    private ManagedScheduledExecutorService executorService;

    private static final Logger LOG = LoggerFactory.getLogger(VesselCacheProxyExecutorService.class);

    @PostConstruct
    public void init(){
        LOG.debug("VesselCacheProxyExecutorService init!!!");
        VesselServiceTask vesselServiceTask = new VesselServiceTask(vesselServiceBean);
        GearTypesServiceTask gearTypesServiceTask = new GearTypesServiceTask(gearTypesServiceBean);

        executorService.scheduleWithFixedDelay(vesselServiceTask, 6, 1440, TimeUnit.MINUTES);
        executorService.scheduleWithFixedDelay(gearTypesServiceTask, 4, 1440, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy(){
        if(!executorService.isShutdown()){
            executorService.shutdownNow();
        }
    }

}
