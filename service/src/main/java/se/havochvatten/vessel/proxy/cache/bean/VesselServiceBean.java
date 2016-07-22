package se.havochvatten.vessel.proxy.cache.bean;

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

import eu.europa.ec.fisheries.uvms.asset.model.exception.AssetModelMarshallException;
import eu.europa.ec.fisheries.uvms.asset.model.mapper.AssetModuleRequestMapper;
import eu.europa.ec.fisheries.wsdl.asset.types.Asset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearByIdResponse;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCSResponse;
import se.havochvatten.service.client.notificationws.v4_0.generalnotification.GearChangeNotificationType;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNationResponse;
import se.havochvatten.service.client.vesselws.v2_1.error.Vessel;
import se.havochvatten.vessel.proxy.cache.ClientProxy;
import se.havochvatten.vessel.proxy.cache.ParameterService;
import se.havochvatten.vessel.proxy.cache.Utils.GearChangeNotificationTypeComparator;
import se.havochvatten.vessel.proxy.cache.Utils.Validate;
import se.havochvatten.vessel.proxy.cache.constant.Constants;
import se.havochvatten.vessel.proxy.cache.constant.ParameterKey;
import se.havochvatten.vessel.proxy.cache.exception.ProxyException;
import se.havochvatten.vessel.proxy.cache.exception.ValidationException;
import se.havochvatten.vessel.proxy.cache.mapper.ResponseMapper;
import se.havochvatten.vessel.proxy.cache.message.ProxyMessageSender;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Queue;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@LocalBean
@Stateless
public class VesselServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(VesselServiceBean.class);

    @Resource(mappedName = Constants.ASSET_MODULE_QUEUE)
    private Queue assetModuleQueue;

    @EJB
    ClientProxy client;

    @Inject
    ProxyMessageSender proxyMessageSender;

    @EJB
    private ParameterService parameterService;


    public List<Vessel> getVesselList(List<String> nations) throws ProxyException {
        GetVesselListByNationResponse vesselListByNation;
        List<Vessel> vessels = new ArrayList<>();
        if (!nations.isEmpty()) {
            for(String nation : nations){
                vesselListByNation = client.getVesselListByNation(nation);
                vessels.addAll(vesselListByNation.getVessel());
            }
        }
        return vessels;
    }

    public void sendVesselAndOwnerInfoToAssetModule(List<Vessel> vesselList)  {
        List<Asset> upsertAssets = new ArrayList<>();
        //TODO: Remove subList
        for (Vessel vessel : vesselList.subList(10,20)) {
            long start = System.currentTimeMillis();
            GetVesselAndOwnerListByIdResponse vesselAndOwnerListById;
            try {
                vesselAndOwnerListById = client.getVesselAndOwnerListById(vessel.getVesselId());
                Validate.validateGetVesselAndOwnerListByIdResponse(vesselAndOwnerListById);
                Asset asset = ResponseMapper.mapToAsset(vesselAndOwnerListById);
                GetGearChangeNotificationListByVesselIRCSResponse gearType = client.getGearTypeByIRCS(vesselAndOwnerListById.getVessel().getIrcs());
                setGearTypeInformation(asset, gearType);
                //TODO: Remove when we know how to get this gear type
                asset.setGearType("PELAGIC");
                upsertAssets.add(asset);
                long totalTime = System.currentTimeMillis() - start;
                LOG.debug("Vessel id: " +vesselAndOwnerListById.getVessel().getVesselId() + " Owner: " +vesselAndOwnerListById.getOwner().size() +" Total time in ms: "  + totalTime);
            } catch (ProxyException e) {
                LOG.error("Could not get additional info for vessel with id: " + vessel.getVesselId());
            } catch (ValidationException e) {
                LOG.error(e.getMessage());
            }
        }
        sendUpsertAssetModuleRequest(upsertAssets);
    }

    private void setGearTypeInformation(Asset asset, GetGearChangeNotificationListByVesselIRCSResponse gearType) {
        if (gearType.getGearChangeNotification().size() > 0) {
            // Sort the gear types by latest date
            Collections.sort(gearType.getGearChangeNotification(), new GearChangeNotificationTypeComparator());
            GearChangeNotificationType gearChangeNotificationType = gearType.getGearChangeNotification().get(0);
            asset.setGearType(getGearTypeCode(gearChangeNotificationType.getGearCode()));
        }
    }

    private String getGearTypeCode(Long id) {
        String gearTypeInfo = null;
        GetGearByIdResponse gearTypeByCode;
        if(id == null){
            LOG.error("Gear type id cannot be null");
        }
        try {
            gearTypeByCode = client.getGearTypeByCode(BigInteger.valueOf(id));
            if(gearTypeByCode!=null){
                gearTypeInfo = gearTypeByCode.getGear().getId() + " " +  gearTypeByCode.getGear().getNameEng();
            }
        } catch (ProxyException e) {
            LOG.error("Could not set gear type");
        }
        return gearTypeInfo;
    }


    private void sendUpsertAssetModuleRequest(List<Asset> assets){
        try {
            String upsertAssetModuleRequest = AssetModuleRequestMapper.createUpsertAssetListModuleRequest(assets, Constants.NATIONAL);
            proxyMessageSender.sendMessage(assetModuleQueue, upsertAssetModuleRequest, null);
        } catch (AssetModelMarshallException e) {
            LOG.error("Could not map asset to createUpsertAssetModuleRequest", e.getMessage());
        }catch (ProxyException e) {
            LOG.error("Could not sen upsert message to Asset module with queue: " + Constants.ASSET_MODULE_QUEUE, e.getMessage());
        }
    }

    public List<String> getNationsFromDatabase(){
        String nations = parameterService.getParameterValue(ParameterKey.NATIONAL_VESSEL_NATIONS);
        List<String> nationList = new ArrayList<>();
        if(nations!=null){
            String trim = nations.replaceAll("\\s+","");
            nationList = Arrays.asList(trim.split(","));
        }
        return nationList;
    }
}
