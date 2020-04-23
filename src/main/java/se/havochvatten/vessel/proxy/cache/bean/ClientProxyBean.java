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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.slf4j.LoggerFactory;
import se.havochvatten.service.client.equipmentws.v1_0.EquipmentException;
import se.havochvatten.service.client.equipmentws.v1_0.EquipmentPortType;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearById;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearByIdResponse;
import se.havochvatten.service.client.equipmentws.v1_0.GetGears;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearsResponse;
import se.havochvatten.service.client.geographyws.v2_0.GeographyException;
import se.havochvatten.service.client.geographyws.v2_0.PortInformationType;
import se.havochvatten.service.client.notificationws.v4_0.GeneralNotificationPortType;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCS;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCSResponse;
import se.havochvatten.service.client.notificationws.v4_0.NotificationException;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListById;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.service.client.vesselcompws.v2_0.VesselCompPortType;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselByCFR;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselByCFRResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselByIRCS;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselByIRCSResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselEuFormatByIRCS;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselEuFormatByIRCSResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNation;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNationResponse;
import se.havochvatten.service.client.vesselws.v2_1.VesselException;
import se.havochvatten.vessel.proxy.cache.mapper.RequestMapper;

@Stateless
public class ClientProxyBean {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ClientProxyBean.class);

    @EJB
    PortInitiatorServiceBean port;

    public GetVesselListByNationResponse getVesselListByNation(String iso3AlphaNation) throws VesselException {
        GetVesselListByNation getVesselListByNation = RequestMapper.mapToGetVesselListByNation(iso3AlphaNation);
        return port.getVesselPortType().getVesselListByNation(getVesselListByNation);
    }
    
    public GetVesselByIRCSResponse getVesselByIrcs(String ircs) throws VesselException {
        GetVesselByIRCS request = new GetVesselByIRCS();
        request.setIrcs(ircs);
        return port.getVesselPortType().getVesselByIRCS(request);
    }
    
    public GetVesselByCFRResponse getVesselByCfr(String cfr) throws VesselException {
        GetVesselByCFR request = new GetVesselByCFR();
        request.setCfr(cfr);
        return port.getVesselPortType().getVesselByCFR(request);
    }

    public GetVesselAndOwnerListByIdResponse getVesselAndOwnerListById(String id) {
        GetVesselAndOwnerListById getVesselAndOwnerListById = RequestMapper.mapToGetVesselAndOwnerListById(id);
        VesselCompPortType vesselCompServicePortType = port.getVesselCompServicePortType();
        GetVesselAndOwnerListByIdResponse response = null;
        try {
            response = vesselCompServicePortType.getVesselAndOwnerListById(getVesselAndOwnerListById);
        } catch (se.havochvatten.service.client.vesselcompws.v2_0.VesselException e) {
            LOG.warn("Could not get vessel and owner by id: {}", id, e);
        }
        return response;
    }

    public GetGearChangeNotificationListByVesselIRCSResponse getGearTypeByIRCS(String ircs) {
        GeneralNotificationPortType generalNotificationPortType = port.getGeneralNotificationPortType();
        GetGearChangeNotificationListByVesselIRCS gearChangeNotificationListByVesselIRCS = new GetGearChangeNotificationListByVesselIRCS();
        GetGearChangeNotificationListByVesselIRCSResponse response = null;
        gearChangeNotificationListByVesselIRCS.setIrcs(ircs);
        try {
             response = generalNotificationPortType.getGearChangeNotificationListByVesselIRCS(gearChangeNotificationListByVesselIRCS);
        } catch (NotificationException e) {
            LOG.warn("Could not get gear types for vessel with ircs: {}", ircs, e);
        }
        return response;
    }

    public GetGearByIdResponse getGearTypeByCode(BigInteger id) {
        GetGearById getGearById = new GetGearById();
        getGearById.setId(id);
        GetGearByIdResponse gearById = null;
        try {
            gearById = port.getEquipmentPortType().getGearById(getGearById);
        } catch (EquipmentException e) {
            LOG.warn("Could not get gear type information, gear type id: {}", id, e);
        }
        return gearById;
    }

    public GetGearsResponse getGearTypeList() {
        GetGears getGearsRequest = new GetGears();
        GetGearsResponse gears = null;
        EquipmentPortType equipmentPortType = port.getEquipmentPortType();
        try {
            gears = equipmentPortType.getGears(getGearsRequest);
        } catch (EquipmentException e) {
            LOG.warn("Could not get gear type list", e);
        }
        return gears;
    }

    public GetVesselEuFormatByIRCSResponse getVesselEuFormatByIRCS(String ircs) {
        if (ircs == null) {
            return null;
        }
        String modifiedIrcs = ircs.replace("-", "");
        GetVesselEuFormatByIRCS getVesselEuFormatParam = new GetVesselEuFormatByIRCS();
        getVesselEuFormatParam.setIrcs(modifiedIrcs);
        GetVesselEuFormatByIRCSResponse response = null;
        try {
            response = port.getVesselPortType().getVesselEuFormatByIRCS(getVesselEuFormatParam);
        } catch (VesselException e) {
            LOG.warn("Could not get vessel in eu format, ircs: {}, {}", modifiedIrcs, e.getMessage());
        }
        return response;
    }
    
    public List<PortInformationType> getPorts() throws GeographyException {
        return port.getGeographyPortType().getListPorts(Arrays.asList("SE"));
    }
}

