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
package se.havochvatten.vessel.proxy.cache.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.havochvatten.service.client.geographyws.v2_0.GeographyException;
import se.havochvatten.service.client.geographyws.v2_0.PortInformationType;

@Singleton
@Startup
public class PortServiceBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(PortServiceBean.class);

    private Map<String, String> ports = new HashMap<>();
    
    @Inject
    private ClientProxyBean clientProxy;
    
    @PostConstruct
    public void init() throws GeographyException {
        readPorts();
    }
    
    @Schedule(dayOfWeek = "0", hour = "1")
    public void updatePorts() throws GeographyException {
        readPorts();
    }
    
    public Map<String, String> getPorts() {
        return ports;
    }
    
    private void readPorts() throws GeographyException {
        List<PortInformationType> portInformation = clientProxy.getPorts();
        LOG.info("Updating ports: found {} ports.", portInformation.size());
        ports = portInformation.stream()
            .collect(Collectors.toMap(PortInformationType::getPortCode, PortInformationType::getPortName, (port1, port2) -> port1));
    }
}
