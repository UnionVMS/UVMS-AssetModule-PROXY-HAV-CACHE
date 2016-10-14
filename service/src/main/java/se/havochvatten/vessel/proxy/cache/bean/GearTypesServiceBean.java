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
import eu.europa.ec.fisheries.wsdl.asset.types.FishingGear;
import eu.europa.ec.fisheries.wsdl.asset.types.FishingGearType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearsResponse;
import se.havochvatten.service.client.equipmentws.v1_0.error.GearType;
import se.havochvatten.vessel.proxy.cache.ClientProxy;
import se.havochvatten.vessel.proxy.cache.constant.Constants;
import se.havochvatten.vessel.proxy.cache.exception.ProxyException;
import se.havochvatten.vessel.proxy.cache.message.ProxyMessageSender;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.Queue;
import javax.persistence.NoResultException;
import java.util.List;


@LocalBean
@Stateless
public class GearTypesServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(GearTypesServiceBean.class);

    @Resource(mappedName = Constants.ASSET_MODULE_QUEUE)
    private Queue assetModuleQueue;

    @Resource(mappedName = Constants.PROXY_QUEUE)
    private Queue responseModuleQueue;

    @EJB
    private ClientProxy clientProxyBean;

    @EJB
    private ProxyMessageSender proxyMessageSender;


    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void updateGearTypes(){
        try {
            GetGearsResponse response = clientProxyBean.getGearTypeList();
            List<GearType> gearTypes = response.getGearList().getGear();
            if (gearTypes.size() > 0) {
                LOG.debug("#######  Gear types size: " + gearTypes.size());
                for (GearType gearType : gearTypes) {
                    FishingGear fishingGear = mapToFishingGear(gearType);
                    LOG.debug("Send gear type: " + gearType.getId());
                    sendFishingGearUpdateToAssetModule(fishingGear);
                }
            }
        } catch (NoResultException e) {
            LOG.error("NoResultException: {}", e.getMessage());
        }
    }


    private void sendFishingGearUpdateToAssetModule(FishingGear fishingGear) {
        try {
            String upsertFishingGearListRequest = AssetModuleRequestMapper.createUpsertFishingGearModuleRequest(fishingGear, "UVMS Vessel Cache");
            String s = proxyMessageSender.sendMessage(assetModuleQueue, responseModuleQueue, upsertFishingGearListRequest);Thread.sleep(1000L);
        } catch (AssetModelMarshallException e) {
                LOG.error("Could not marshalle the request upsertFishingGearListRequest");
        } catch (ProxyException e) {
            LOG.error("Cannot send reqest to Asset module, queue: " + Constants.ASSET_MODULE_QUEUE);
        } catch (InterruptedException e) {
            LOG.error("Could not set the thread to sleep in 10 seconds");
        }catch (Exception exception){
            LOG.error("An unexpected exception occurred");
        }
    }


    private FishingGear mapToFishingGear(GearType gearType) {
        FishingGear fishingGear = new FishingGear();
        FishingGearType fishingGearType = new FishingGearType();
        fishingGear.setCode(gearType.getFaoCode());
        fishingGear.setDescription(gearType.getNameSwe());
        fishingGear.setName(gearType.getNameSwe());
        fishingGear.setExternalId(gearType.getId().longValue());
        fishingGear.setFishingGearType(fishingGearType);
        fishingGearType.setName(gearType.getGearType().getNameSwe());
        fishingGearType.setCode(gearType.getGearType().getCode());
        return fishingGear;
    }

}
