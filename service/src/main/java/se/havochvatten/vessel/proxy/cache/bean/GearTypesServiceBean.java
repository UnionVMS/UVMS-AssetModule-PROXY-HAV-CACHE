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
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;


@LocalBean
@Stateless
public class GearTypesServiceBean {

    @Resource(mappedName = Constants.ASSET_MODULE_QUEUE)
    private Queue assetModuleQueue;

    @EJB
    private ClientProxy clientProxyBean;

    @EJB
    private ProxyMessageSender proxyMessageSender;

    private static final Logger LOG = LoggerFactory.getLogger(GearTypesServiceBean.class);

    public void updateGearTypes(){
        GetGearsResponse response= clientProxyBean.getGearTypeList();
        List<GearType> gearTypes = response.getGearList().getGear();
        List<FishingGear> fishingGears = new ArrayList<>();
        if(gearTypes.size()>0) {
            //TODO: Remove subList
            for(GearType gearType : gearTypes.subList(0, 10) ){
                fishingGears.add(mapToFishingGear(gearType));
            }
        }
        try {
            String upsertFishingGearListRequest = AssetModuleRequestMapper.createUpsertFishingGearListRequest(fishingGears, "UVMS Vessel Cache");
            proxyMessageSender.sendMessage(assetModuleQueue, upsertFishingGearListRequest, null);
        } catch (AssetModelMarshallException e) {
                LOG.error("Could not marshalle the request upsertFishingGearListRequest");
        } catch (ProxyException e) {
            LOG.error("Cannot send reqest to Asset module, queue: " + Constants.ASSET_MODULE_QUEUE);
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
