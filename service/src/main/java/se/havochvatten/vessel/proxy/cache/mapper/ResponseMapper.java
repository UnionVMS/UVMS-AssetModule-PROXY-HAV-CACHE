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

package se.havochvatten.vessel.proxy.cache.mapper;

import eu.europa.ec.fisheries.wsdl.asset.types.*;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.service.client.vesselcompws.v2_0.orgpers.OrganisationType;
import se.havochvatten.service.client.vesselcompws.v2_0.orgpers.RolePersonType;
import se.havochvatten.service.client.vesselcompws.v2_0.vessel.OwnerType;
import se.havochvatten.service.client.vesselcompws.v2_0.vessel.Vessel;

import java.util.List;

public class ResponseMapper {

    public static Asset mapToAsset(GetVesselAndOwnerListByIdResponse vesselAndOwnerListByIdResponse){
        Asset asset = new Asset();

        Vessel vessel = vesselAndOwnerListByIdResponse.getVessel();
        List<OwnerType> owners = vesselAndOwnerListByIdResponse.getOwner();

        AssetId assetId = new AssetId();
        assetId.setType(AssetIdType.CFR);
        assetId.setValue(vessel.getCfr());

        if (!owners.isEmpty()) {
            OwnerType owner = owners.get(0);
            OrganisationType organisation = owner.getOrganisation();
            RolePersonType rolePerson = owner.getRolePerson();

            if(rolePerson!=null){
                mapToAssetContact(asset, rolePerson);
            }else if (organisation!=null){
                mapToAssetContact(asset, organisation);
            }
        }

        asset.setActive(vessel.isActive());
        asset.setAssetId(assetId);
        asset.setCfr(vessel.getCfr());
        asset.setCountryCode(vessel.getIso3AlphaNation());
        asset.setName(vessel.getVesselName());
        asset.setExternalMarking(vessel.getDistrict());
        asset.setGrossTonnage(vessel.getEuTon());
        asset.setLengthOverAll(vessel.getLoa());
        asset.setHomePort(vessel.getDefaultPort()!=null ? vessel.getDefaultPort().getPort() : null);
        asset.setImo(vessel.getImoNumber());
        asset.setIrcs(vessel.getIrcs());
        asset.setHasIrcs(vessel.getIrcs() !=null ? "Y" : "N");
        asset.setSource(CarrierSource.NATIONAL);
        asset.setGrossTonnage(vessel.getEuTon());
        asset.setLengthOverAll(vessel.getLoa());
        asset.setPowerMain(vessel.getEnginePower());
        //asset.setHasLicense(vessel.getOwner().getAuthorizationAndLicenses().isEmpty() ? false : true);
        //asset.setMmsiNo(vessel.get);
        //asset.setLengthBetweenPerpendiculars();
        //asset.setLicenseType();
        //asset.setProducer();
        return asset;
    }

    private static void mapToAssetContact(Asset asset, RolePersonType rolePerson) {
        AssetContact assetContact = new AssetContact();
        assetContact.setEmail(rolePerson.getEmail());
        assetContact.setName(rolePerson.getPersonAdress().getName().getGivenname() + " " + rolePerson.getPersonAdress().getName().getSurname());
        assetContact.setNumber(rolePerson.getHomePhone() != null ? rolePerson.getHomePhone().getTelephoneNumber() : rolePerson.getMobilePhone().getTelephoneNumber());
        asset.setContact(assetContact);
    }

    private static void mapToAssetContact(Asset asset, OrganisationType organisationType) {
        AssetContact assetContact = new AssetContact();
        assetContact.setEmail(organisationType.getEmail());
        assetContact.setName(organisationType.getOrganisationAdress().getOrgName());
        assetContact.setNumber(organisationType.getPhone1()!=null ? organisationType.getPhone1().getTelephoneNumber() : null);
        asset.setContact(assetContact);
    }
}
