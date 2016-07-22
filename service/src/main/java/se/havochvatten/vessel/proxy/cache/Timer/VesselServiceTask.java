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

package se.havochvatten.vessel.proxy.cache.Timer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.havochvatten.service.client.vesselws.v2_1.error.Vessel;
import se.havochvatten.vessel.proxy.cache.bean.VesselServiceBean;
import se.havochvatten.vessel.proxy.cache.exception.ProxyException;

import java.util.List;

public class VesselServiceTask implements Runnable{

    private static final Logger LOG = LoggerFactory.getLogger(VesselServiceTask.class);
    private VesselServiceBean vesselServiceBean;

    public VesselServiceTask(VesselServiceBean vesselServiceBean){
        this.vesselServiceBean = vesselServiceBean;
    }

    @Override
    public void run() {

        LOG.debug(" VesselServiceTask run");
        try {
            long start = System.currentTimeMillis();
            List<String> nations = vesselServiceBean.getNationsFromDatabase();
            List<Vessel> vesselList;
            vesselList = vesselServiceBean.getVesselList(nations);
            vesselServiceBean.sendVesselAndOwnerInfoToAssetModule(vesselList);
            long tot = System.currentTimeMillis() - start;
            LOG.debug("--------------- VesselServiceTask total time " +  tot +" ms      -------------");
        } catch (ProxyException e) {
            LOG.error(e.getMessage());
        }

    }
}
