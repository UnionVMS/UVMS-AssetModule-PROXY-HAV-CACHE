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

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.CustomCode;
import eu.europa.ec.fisheries.uvms.asset.client.model.CustomCodesPK;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearsResponse;
import se.havochvatten.service.client.equipmentws.v1_0.error.GearType;
import se.havochvatten.service.client.equipmentws.v1_0.error.GearTypeType;

@Stateless
public class GearTypesServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(GearTypesServiceBean.class);
    
    private static final String GEAR = "FISHING_GEAR";
    private static final String GEAR_TYPE = "FISHING_TYPE";
    private static final String GEAR_GROUP = "FISHING_GEAR_GROUP";

    @EJB
    private AssetClient assetClient;

    @Inject
    private ClientProxyBean clientProxyBean;

    public void updateGearTypes(){
        try {
            GetGearsResponse response = clientProxyBean.getGearTypeList();
            List<GearType> gearTypes = response.getGearList().getGear();
            LOG.info("Gear types size: {}", gearTypes.size());
            for (GearType gearType : gearTypes) {
                createGearAndConstants(gearType);
            }
        } catch (Exception e) {
            LOG.error("Could not update fishing gears", e);
        }
    }
    
    private void createGearAndConstants(GearType gearType) {
        try {
            createGearTypeIfNotExists(gearType.getGearType());
            createGearGroupIfNotExists(gearType.getGearGroup());
            createFishingGear(gearType);
        } catch (Exception e) {
            LOG.error("Could not create gear {}", gearType.getFaoCode(), e);
        }
    }

    private void createFishingGear(GearType gearType) {
        List<CustomCode> codes = assetClient.getCodeForDate(GEAR, gearType.getFaoCode(), OffsetDateTime.now());
        if (codes.isEmpty()) {
            CustomCode gear = new CustomCode();
            CustomCodesPK key = new CustomCodesPK(GEAR, gearType.getFaoCode());
            gear.setPrimaryKey(key);
            gear.setDescription(gearType.getNameEng());
            Map<String, String> nameValue = new HashMap<>();
            nameValue.put(GEAR_TYPE, Integer.toString(gearType.getGearType().getCode()));
            nameValue.put(GEAR_GROUP, Integer.toString(gearType.getGearGroup().getCode()));
            gear.setNameValue(nameValue);
            assetClient.createCustomCode(gear);
        }
    }

    private void createGearTypeIfNotExists(GearTypeType gearType) {
        List<CustomCode> codes = assetClient.getCodeForDate(GEAR_TYPE, Integer.toString(gearType.getCode()), OffsetDateTime.now());
        if (codes.isEmpty()) {
            CustomCode type = new CustomCode();
            CustomCodesPK key = new CustomCodesPK(GEAR_TYPE, Integer.toString(gearType.getCode()));
            type.setPrimaryKey(key);
            type.setDescription(gearType.getNameEng());
            assetClient.createCustomCode(type);
        }
    }
    
    private void createGearGroupIfNotExists(GearTypeType gearGroup) {
        List<CustomCode> codes = assetClient.getCodeForDate(GEAR_GROUP, Integer.toString(gearGroup.getCode()), OffsetDateTime.now());
        if (codes.isEmpty()) {
            CustomCode group = new CustomCode();
            CustomCodesPK key = new CustomCodesPK(GEAR_GROUP, Integer.toString(gearGroup.getCode()));
            group.setPrimaryKey(key);
            group.setDescription(gearGroup.getNameEng());
            assetClient.createCustomCode(group);
        }
    }
}
