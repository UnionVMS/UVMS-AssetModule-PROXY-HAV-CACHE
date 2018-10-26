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

import org.slf4j.LoggerFactory;
import se.havochvatten.service.client.equipmentws.v1_0.*;
import se.havochvatten.service.client.notificationws.v4_0.GeneralNotificationPortType;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCS;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCSResponse;
import se.havochvatten.service.client.notificationws.v4_0.NotificationException;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListById;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.service.client.vesselcompws.v2_0.VesselCompPortType;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNation;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNationResponse;
import se.havochvatten.service.client.vesselws.v2_1.VesselException;
import se.havochvatten.vessel.proxy.cache.ClientProxy;
import se.havochvatten.vessel.proxy.cache.exception.ProxyException;
import se.havochvatten.vessel.proxy.cache.mapper.RequestMapper;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.math.BigInteger;

@Stateless
public class ClientProxyBean implements ClientProxy {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ClientProxyBean.class);

    @EJB
    PortInitiatorServiceBean port;

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public GetVesselListByNationResponse getVesselListByNation(String iso3AlphaNation) throws ProxyException {
        GetVesselListByNationResponse vesselListByNationResponse;
        GetVesselListByNation getVesselListByNation = RequestMapper.mapToGetVesselListByNation(iso3AlphaNation);
        try {
            vesselListByNationResponse = port.getVesselPortType().getVesselListByNation(getVesselListByNation);
        } catch (VesselException e) {
            LOG.error("Fail to call web service to get all Vessels by nation; " + iso3AlphaNation, e.getMessage());
            throw new ProxyException("Fail to call web service to get all Vessels by nation; " + iso3AlphaNation);
        }
        return vesselListByNationResponse;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public GetVesselAndOwnerListByIdResponse getVesselAndOwnerListById(String id) throws ProxyException {
        GetVesselAndOwnerListById getVesselAndOwnerListById = RequestMapper.mapToGetVesselAndOwnerListById(id);
        GetVesselAndOwnerListByIdResponse vesselAndOwnerListById;
        VesselCompPortType vesselCompServicePortType = port.getVesselCompServicePortType();

        try {
            vesselAndOwnerListById = vesselCompServicePortType.getVesselAndOwnerListById(getVesselAndOwnerListById);
        } catch (se.havochvatten.service.client.vesselcompws.v2_0.VesselException e) {
            LOG.error("Fail to call web service to get complement info for Vessel id: ; " + id);
            throw new ProxyException("Fail to call web service to get complement info for Vessel id: ; " + id);
        }

        return vesselAndOwnerListById;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public GetGearChangeNotificationListByVesselIRCSResponse getGearTypeByIRCS(String ircs) throws ProxyException {
        GeneralNotificationPortType generalNotificationPortType = port.getGeneralNotificationPortType();
        GetGearChangeNotificationListByVesselIRCS gearChangeNotificationListByVesselIRCS = new GetGearChangeNotificationListByVesselIRCS();
        GetGearChangeNotificationListByVesselIRCSResponse response;
        gearChangeNotificationListByVesselIRCS.setIrcs(ircs);
        try {
             response = generalNotificationPortType.getGearChangeNotificationListByVesselIRCS(gearChangeNotificationListByVesselIRCS);
        } catch (NotificationException e) {
            LOG.error("Could not get gertypes for vessel with ircs: " + ircs, e.getMessage());
            throw new ProxyException("Could not get gertypes for vessel with ircs: " + ircs);
        }
        return response;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public GetGearByIdResponse getGearTypeByCode(BigInteger id) throws ProxyException {
        GetGearById getGearById = new GetGearById();
        getGearById.setId(id);
        GetGearByIdResponse gearById;
        try {
            gearById = port.getEquipmentPortType().getGearById(getGearById);
        } catch (EquipmentException e) {
            LOG.error("Could not get gear type information, gear type id: " +  id, e.getMessage());
            throw new ProxyException("Could not get gear type information, gear type id: " +  id);
        }
        return gearById;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public GetGearsResponse getGearTypeList(){
        GetGears getGearsRequest = new GetGears();
        GetGearsResponse gears = null;
        EquipmentPortType equipmentPortType = port.getEquipmentPortType();
        try {
            gears = equipmentPortType.getGears(getGearsRequest);
        } catch (EquipmentException e) {
            LOG.error("Could not get gear type list", e);
        }
        return gears;
    }
}

