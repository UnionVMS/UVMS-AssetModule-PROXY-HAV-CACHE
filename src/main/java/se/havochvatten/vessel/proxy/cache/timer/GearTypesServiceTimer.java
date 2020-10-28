package se.havochvatten.vessel.proxy.cache.timer;
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

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.havochvatten.vessel.proxy.cache.bean.GearTypesServiceBean;

@Singleton
@Startup
public class GearTypesServiceTimer {

    private static final Logger LOG = LoggerFactory.getLogger(GearTypesServiceTimer.class);

    @Inject
    private GearTypesServiceBean gearTypesServiceBean;

    @Resource(lookup="java:/UvmsVesselCacheProxyExecutorService")
    private ManagedScheduledExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService.schedule(this::updateGears, 4, TimeUnit.MINUTES);
    }

    @Schedule(hour = "1")
    public void updateGears() {
        LOG.info("GearTypesServiceTask run!");
        long start = System.currentTimeMillis();
        gearTypesServiceBean.updateGearTypes();
        long tot = System.currentTimeMillis() - start;
        LOG.info("--------------- GearTypesServiceTask total time {} ms      -------------", tot);
    }
}
