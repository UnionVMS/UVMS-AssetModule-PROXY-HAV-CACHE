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

import java.util.Map;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.xml.ws.BindingProvider;
import se.havochvatten.service.client.equipmentws.v1_0.EquipmentPortType;
import se.havochvatten.service.client.equipmentws.v1_0.EquipmentService;
import se.havochvatten.service.client.notificationws.v4_0.GeneralNotificationPortType;
import se.havochvatten.service.client.notificationws.v4_0.GeneralNotificationService;
import se.havochvatten.service.client.vesselcompws.v2_0.VesselCompPortType;
import se.havochvatten.service.client.vesselcompws.v2_0.VesselCompService;
import se.havochvatten.service.client.vesselws.v2_1.VesselPortType;
import se.havochvatten.service.client.vesselws.v2_1.VesselService;
import se.havochvatten.vessel.proxy.cache.constant.ParameterKey;

@Singleton
@Startup
public class PortInitiatorServiceBean {

    private VesselPortType vesselPortType;
    private VesselCompPortType vesselCompServicePortType;
    private GeneralNotificationPortType generalNotificationPortType;
    private EquipmentPortType equipmentPortType;

    @Inject
    private ParameterServiceBean parameterService;

    private void setupVesselPortType() {
        VesselService vesselService = new VesselService();
        vesselPortType = vesselService.getVesselPortType();
        BindingProvider bp = (BindingProvider) vesselPortType;
        Map<String, Object> context = bp.getRequestContext();
        String endpointAddress = parameterService.getParameterValue(ParameterKey.NATIONAL_SERVICE_ENDPOINT);
        //LOG.debug("National endpoint vessel: " + endpointAddress);
        context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

    }

    public VesselPortType getVesselPortType() {
        if(vesselPortType == null){
            setupVesselPortType();
        }
        return vesselPortType;
    }

    public void setVesselPortType(VesselPortType vesselPortType){
        this.vesselPortType = vesselPortType;
    }

    private void setupVesselCompPortType() {
        VesselCompService vesselService = new VesselCompService();
        vesselCompServicePortType = vesselService.getVesselCompPortType();
        BindingProvider bp = (BindingProvider) vesselCompServicePortType;
        Map<String, Object> context = bp.getRequestContext();
        //String endpointAddress = "http://livmipl02p:8001/esb/VesselComp/v2"; /
        String endpointAddress = parameterService.getParameterValue(ParameterKey.NATIONAL_VESSEL_COMP_SERVICE_ENDPOINT);
        //LOG.debug("National endpoint vessel comp:" + endpointAddress);
        context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

    }

    private void setupGeneralNotificationPortType() {
        GeneralNotificationService generalNotificationService = new GeneralNotificationService();
        generalNotificationPortType = generalNotificationService.getGeneralNotificationPortType();
        BindingProvider bp = (BindingProvider) generalNotificationPortType;
        Map<String, Object> context = bp.getRequestContext();
        String endpointAddress = parameterService.getParameterValue(ParameterKey.NATIONAL_GENERAL_NOTIFICATION_SERVICE_ENDPOINT);
        context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
    }

    private void setupEquipmentPortType() {
        EquipmentService equipmentService = new EquipmentService();
        equipmentPortType = equipmentService.getEquipmentPortType();
        BindingProvider bp = (BindingProvider) equipmentPortType;
        Map<String, Object> context = bp.getRequestContext();
        String endpointAddress = parameterService.getParameterValue(ParameterKey.NATIONAL_EQUIPMENT_SERVICE_ENDPOINT);
        context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
    }

    public VesselCompPortType getVesselCompServicePortType() {
        if(vesselCompServicePortType == null){
            setupVesselCompPortType();
        }
        return vesselCompServicePortType;
    }

    public void setVesselCompServicePortType(VesselCompPortType vesselCompServicePortType) {
        this.vesselCompServicePortType = vesselCompServicePortType;
    }

    public GeneralNotificationPortType getGeneralNotificationPortType() {
        if(generalNotificationPortType==null){
            setupGeneralNotificationPortType();
        }
        return generalNotificationPortType;
    }

    public void setGeneralNotificationPortType(GeneralNotificationPortType generalNotificationPortType) {
        this.generalNotificationPortType = generalNotificationPortType;
    }

    public EquipmentPortType getEquipmentPortType() {
        if(equipmentPortType == null){
            setupEquipmentPortType();
        }
        return equipmentPortType;
    }

    public void setEquipmentPortType(EquipmentPortType equipmentPortType) {
        this.equipmentPortType = equipmentPortType;
    }

}
