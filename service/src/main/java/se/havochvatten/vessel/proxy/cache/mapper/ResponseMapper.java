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

import java.util.ArrayList;
import java.util.List;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetBO;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.asset.client.model.ContactInfo;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.service.client.vesselcompws.v2_0.orgpers.OrganisationType;
import se.havochvatten.service.client.vesselcompws.v2_0.orgpers.RolePersonType;
import se.havochvatten.service.client.vesselcompws.v2_0.vessel.OwnerType;
import se.havochvatten.service.client.vesselcompws.v2_0.vessel.Vessel;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselEuFormatByCFRResponse;
import se.havochvatten.service.client.vesselws.v2_1.vessel.VesselEuFormatType;

public class ResponseMapper {
    
    private ResponseMapper() {};

    public static AssetBO mapToAsset(GetVesselAndOwnerListByIdResponse vesselAndOwnerListByIdResponse, GetVesselEuFormatByCFRResponse vesselEuFormat){

        AssetBO assetBo = new AssetBO();
        
        Vessel vessel = vesselAndOwnerListByIdResponse.getVessel();
        AssetDTO asset = new AssetDTO();

        asset.setActive(vessel.isActive());
        asset.setCfr(vessel.getCfr());
        asset.setFlagStateCode(vessel.getIso3AlphaNation());
        asset.setName(vessel.getVesselName());
        asset.setExternalMarking(vessel.getDistrict());
        asset.setGrossTonnage(vessel.getEuTon() != null ? vessel.getEuTon().doubleValue(): null);
        asset.setLengthOverAll(vessel.getLoa() != null ? vessel.getLoa().doubleValue() : null);
        asset.setPortOfRegistration(vessel.getDefaultPort() != null ? vessel.getDefaultPort().getPort() : null);
        asset.setImo(vessel.getImoNumber());
        asset.setIrcs(vessel.getIrcs());
        asset.setIrcsIndicator(vessel.getIrcs() != null);
        asset.setSource("NATIONAL");
        asset.setUpdatedBy("HAV VESSEL PROXY CACHE");
        asset.setPowerOfMainEngine(vessel.getEnginePower() != null ? vessel.getEnginePower().doubleValue() : null);
        asset.setHasLicence(vessel.isHasLicense());
        asset.setHasVms(vessel.isHasVms());
        asset.setVesselType("Fishing");

        if (vesselEuFormat != null) {
            VesselEuFormatType vesselEu = vesselEuFormat.getVesselEuFormat();
            if (vesselEu != null) {
                if (vesselEu.getIdentification() != null && vesselEu.getIdentification().getMmsi() != null) {
                    asset.setMmsi(vesselEu.getIdentification().getMmsi().toString());
                }
                if (vesselEu.getConstruction() != null) {
                    if (vesselEu.getConstruction().getYearOfConstruction() != null) {
                        asset.setConstructionYear(vesselEu.getConstruction().getYearOfConstruction().toString());
                    }
                    asset.setConstructionPlace(vesselEu.getConstruction().getPlaceOfConstruction());
                }
            }
        }
        
        assetBo.setAsset(asset);
        
        List<ContactInfo> contacts = new ArrayList<>();

        List<OwnerType> owners = vesselAndOwnerListByIdResponse.getOwner();
        for (OwnerType owner : owners) {
            OrganisationType organisation = owner.getOrganisation();
            RolePersonType rolePerson = owner.getRolePerson();

            ContactInfo contactInfo = null;
            if (rolePerson != null) {
                contactInfo = mapToContactInfo(rolePerson);
            } else if (organisation != null) {
                contactInfo = mapToContactInfo(organisation);
                asset.setProdOrgCode(organisation.getOrgNumber());
                asset.setProdOrgName(organisation.getOrganisationAdress().getOrgName());
            }
            contacts.add(contactInfo);
        }
        assetBo.setContacts(contacts);

        return assetBo;
    }

    private static ContactInfo mapToContactInfo(RolePersonType rolePerson) {
        ContactInfo assetContact = new ContactInfo();
        assetContact.setType("Person");
        assetContact.setEmail(rolePerson.getEmail());
        assetContact.setPhoneNumber(rolePerson.getHomePhone() != null ? rolePerson.getHomePhone().getTelephoneNumber() : rolePerson.getMobilePhone().getTelephoneNumber());
        assetContact.setName(rolePerson.getPersonAdress().getName().getGivenname() + " " + rolePerson.getPersonAdress().getName().getSurname());
        assetContact.setStreetName(rolePerson.getPersonAdress().getStreet());
        assetContact.setZipCode(rolePerson.getPersonAdress().getZipcode() != null ? rolePerson.getPersonAdress().getZipcode().toString() : null);
        assetContact.setCityName(rolePerson.getPersonAdress().getCity());
        
        return assetContact;
    }

    private static ContactInfo mapToContactInfo(OrganisationType organisationType) {
        ContactInfo assetContact = new ContactInfo();
        assetContact.setType("Organization");
        assetContact.setEmail(organisationType.getEmail());
        assetContact.setName(organisationType.getOrganisationAdress().getOrgName());
        assetContact.setPhoneNumber(organisationType.getPhone1()!=null ? organisationType.getPhone1().getTelephoneNumber() : null);
        assetContact.setStreetName(organisationType.getOrganisationAdress().getStreet());
        assetContact.setZipCode(organisationType.getOrganisationAdress().getZipcode() != null ? organisationType.getOrganisationAdress().getZipcode().toString() : null);
        assetContact.setCityName(organisationType.getOrganisationAdress().getCity());
        return assetContact;
    }
}
