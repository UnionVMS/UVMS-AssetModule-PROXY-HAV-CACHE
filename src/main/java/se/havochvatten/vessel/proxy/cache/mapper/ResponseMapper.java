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
import java.util.Arrays;
import java.util.List;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetBO;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.asset.client.model.ContactInfo;
import eu.europa.ec.fisheries.uvms.asset.client.model.FishingLicence;
import org.apache.commons.lang3.math.NumberUtils;
import se.havochvatten.service.client.vesselcompws.v2_0.orgpers.OrganisationType;
import se.havochvatten.service.client.vesselcompws.v2_0.orgpers.RolePersonType;
import se.havochvatten.service.client.vesselcompws.v2_0.vessel.OwnerType;
import se.havochvatten.service.client.vesselws.v2_1.vessel.AddressType;
import se.havochvatten.service.client.vesselws.v2_1.vessel.ConstructionType;
import se.havochvatten.service.client.vesselws.v2_1.vessel.ContactDetailsType;
import se.havochvatten.service.client.vesselws.v2_1.vessel.DimensionType;
import se.havochvatten.service.client.vesselws.v2_1.vessel.EngineType;
import se.havochvatten.service.client.vesselws.v2_1.vessel.EquipmentType;
import se.havochvatten.service.client.vesselws.v2_1.vessel.IdentificationType;
import se.havochvatten.service.client.vesselws.v2_1.vessel.RegistrationType;
import se.havochvatten.service.client.vesselws.v2_1.vessel.Vessel;
import se.havochvatten.service.client.vesselws.v2_1.vessel.VesselEuFormatType;

public class ResponseMapper {
    
    private static final List<String> OUT_EVENT_CODES = Arrays.asList("DES", "EXP", "RET");

    private ResponseMapper() {}

    public static AssetDTO mapToAsset(Vessel vessel) {
        AssetDTO asset = new AssetDTO();

        asset.setActive(vessel.isActive());
        asset.setFlagStateCode(vessel.getIso3AlphaNation());
        asset.setName(vessel.getVesselName());

        asset.setGrossTonnage(vessel.getEuTon() != null ? vessel.getEuTon().doubleValue(): null);
        asset.setLengthOverAll(vessel.getLoa() != null ? vessel.getLoa().doubleValue() : null);
        asset.setPortOfRegistration(vessel.getDefaultPort() != null ? vessel.getDefaultPort().getPort() : null);

        asset.setExternalMarking(vessel.getDistrict());
        asset.setCfr(vessel.getCfr());
        asset.setImo(vessel.getImoNumber());
        asset.setIrcs(vessel.getIrcs());
        asset.setIrcsIndicator(vessel.getIrcs() != null);
        asset.setNationalId(NumberUtils.isParsable(vessel.getVesselId()) ? Long.parseLong(vessel.getVesselId()) : null );

        asset.setSource("NATIONAL");
        asset.setUpdatedBy("HAV VESSEL PROXY CACHE");
        asset.setPowerOfMainEngine(vessel.getEnginePower() != null ? vessel.getEnginePower().doubleValue() : null);
        asset.setHasLicence(vessel.isHasLicense());
        asset.setHasVms(vessel.isHasVms());
        asset.setVesselType("Fishing");
        return asset;
    }
    
    public static void enrichWithEuFormatInformation(AssetDTO asset, List<ContactInfo> contacts, VesselEuFormatType vesselEu) {
        if (vesselEu != null) {
            enrichIdentificationType(asset, vesselEu);
            enrichRegistrationType(asset, vesselEu);
            enrichEquipmentType(asset, vesselEu);
            enrichDimensionType(asset, vesselEu);
            enrichEngineType(asset, vesselEu);
            enrichConstructionType(asset, vesselEu);
            if (vesselEu.getEvent() != null) {
                String eventCode = vesselEu.getEvent().getEventCode();
                asset.setEventCode(eventCode);
                if (OUT_EVENT_CODES.contains(eventCode)) {
                    asset.setActive(false);
                }
            }
            if (contacts.isEmpty() && !vesselEu.getOwner().isEmpty()) {
                enrichOwner(contacts, vesselEu);
            }
        }
    }

    private static void enrichIdentificationType(AssetDTO asset, VesselEuFormatType vesselEu) {
        IdentificationType identification = vesselEu.getIdentification();
        if (identification != null) {
            if (vesselEu.getIdentification().getMmsi() != null) {
                asset.setMmsi(vesselEu.getIdentification().getMmsi().toString());
            }
            if (vesselEu.getIdentification().getImo() != null) {
                asset.setImo(vesselEu.getIdentification().getImo());
            }
            if (vesselEu.getIdentification().getUvi() != null) {
                asset.setUvi(vesselEu.getIdentification().getUvi());
            }
        }
    }

    private static void enrichRegistrationType(AssetDTO asset, VesselEuFormatType vesselEu) {
        RegistrationType registration = vesselEu.getRegistration();
        if (registration != null) {
            if (asset.getPortOfRegistration() == null) {
                if (vesselEu.getRegistration().getPortName() != null) {
                    asset.setPortOfRegistration(vesselEu.getRegistration().getPortCode() + " " + vesselEu.getRegistration().getPortName());
                } else {
                    asset.setPortOfRegistration(vesselEu.getRegistration().getPortCode());
                }
            }
            if (registration.getDateOfEIS() != null) {
                asset.setVesselDateOfEntry(registration.getDateOfEIS().toGregorianCalendar().toInstant());
            }
            asset.setSegment(registration.getSegment());
            asset.setTypeOfExport(registration.getExportType());
            asset.setCountryOfImportOrExport(registration.getImportOrExportCountry());
            asset.setSegmentOfAdministrativeDecision(registration.getAdministrativeSegment());
            asset.setPublicAid(registration.getPublicAID());
        }
    }

    private static void enrichEquipmentType(AssetDTO asset, VesselEuFormatType vesselEu) {
        EquipmentType equipment = vesselEu.getEquipment();
        if (equipment != null) {
            asset.setVmsIndicator(equipment.isVmsIndicator());
            asset.setErsIndicator(equipment.isErsIndicator());
            asset.setAisIndicator(equipment.isAisIndicator());
            asset.setMainFishingGearCode(equipment.getMainFishingGear());
            asset.setSubFishingGearCode(equipment.getSubsidiaryFishingGear());
        }
    }

    private static void enrichDimensionType(AssetDTO asset, VesselEuFormatType vesselEu) {
        DimensionType dimension = vesselEu.getDimension();
        if (dimension != null) {
            if (dimension.getLoa() != null) {
                asset.setLengthOverAll(dimension.getLoa().doubleValue());
            }
            if (dimension.getTonnageGT() != null) {
                asset.setGrossTonnage(dimension.getTonnageGT().doubleValue());
            }
            if (dimension.getTonnageOther() != null) {
                asset.setOtherTonnage(dimension.getTonnageOther().doubleValue());
            }
            if (dimension.getLengthBetweenPerpendiculars() != null) {
                asset.setLengthBetweenPerpendiculars(dimension.getLengthBetweenPerpendiculars().doubleValue());
            }
        }
    }

    private static void enrichEngineType(AssetDTO asset, VesselEuFormatType vesselEu) {
        EngineType engine = vesselEu.getEngine();
        if (engine != null) {
            if (engine.getPowerOfMainEngine() != null) {
                asset.setPowerOfMainEngine(engine.getPowerOfMainEngine().doubleValue());
            }
            if (engine.getPowerOfAuxEngine() != null) {
                asset.setPowerOfAuxEngine(engine.getPowerOfAuxEngine().doubleValue());
            }
        }
    }

    private static void enrichConstructionType(AssetDTO asset, VesselEuFormatType vesselEu) {
        ConstructionType construction = vesselEu.getConstruction();
        if (construction != null) {
            if (construction.getYearOfConstruction() != null) {
                asset.setConstructionYear(construction.getYearOfConstruction().toString());
            }
            asset.setConstructionPlace(construction.getPlaceOfConstruction());
            asset.setHullMaterial(construction.getHull());
        }
    }

    private static void enrichOwner(List<ContactInfo> contacts, VesselEuFormatType vesselEu) {
        for (VesselEuFormatType.Owner owner : vesselEu.getOwner()) {
            if (owner.getPerson() != null) {
                ContactInfo contactInfo = new ContactInfo();
                contactInfo.setType("Person");
                if (owner.getPerson().getGivenName() != null) {
                    contactInfo.setName(owner.getPerson().getGivenName() + owner.getPerson().getFamilyName());
                } else {
                    contactInfo.setName(owner.getPerson().getFamilyName());
                }
                AddressType address = owner.getPerson().getAddress();
                ContactDetailsType contactDetails = owner.getPerson().getContactDetails();
                mapAddressType(contactInfo, address);
                mapContactDetailsType(contactInfo, contactDetails);
                contacts.add(contactInfo);
            }
            if (owner.getOrganisation() != null) {
                ContactInfo contactInfo = new ContactInfo();
                contactInfo.setType("Organization");
                contactInfo.setName(owner.getOrganisation().getName());
                AddressType address = owner.getOrganisation().getAddress();
                ContactDetailsType contactDetails = owner.getOrganisation().getContactDetails();
                mapAddressType(contactInfo, address);
                mapContactDetailsType(contactInfo, contactDetails);
                contacts.add(contactInfo);
            }
        }
    }

    private static void mapAddressType(ContactInfo contactInfo, AddressType address) {
        if (address != null) {
            contactInfo.setStreetName(address.getStreet());
            contactInfo.setPostalArea(address.getPostCode());
            contactInfo.setCityName(address.getCity());
            contactInfo.setCountry(address.getCountry());
        }
    }

    private static void mapContactDetailsType(ContactInfo contactInfo, ContactDetailsType contactDetails) {
        if (contactDetails != null) {
            contactInfo.setEmail(contactDetails.getEmail());
            contactInfo.setPhoneNumber(contactDetails.getPhone());
        }
    }

    public static void enrichWithOrganisation(AssetDTO asset, List<OwnerType> owners) {
        for (OwnerType owner : owners) {
            OrganisationType organisation = owner.getOrganisation();
            if (organisation != null) {
                asset.setProdOrgCode(organisation.getOrgNumber());
                asset.setProdOrgName(organisation.getOrganisationAdress().getOrgName());
            }
        }
    }
    
    public static List<ContactInfo> mapToContactInfo(List<OwnerType> owners) {
        List<ContactInfo> contacts = new ArrayList<>();

        for (OwnerType owner : owners) {
            OrganisationType organisation = owner.getOrganisation();
            RolePersonType rolePerson = owner.getRolePerson();

            ContactInfo contactInfo = null;
            if (rolePerson != null) {
                contactInfo = mapToContactInfo(rolePerson);
            } else if (organisation != null) {
                contactInfo = mapToContactInfo(organisation);
            }
            contacts.add(contactInfo);
        }
        return contacts;
    }
    
    public static AssetBO mapToAssetBO(AssetDTO asset, List<ContactInfo> contacts, FishingLicence fishingLicence) {
        AssetBO assetBo = new AssetBO();
        assetBo.setAsset(asset);
        assetBo.setContacts(contacts);
        assetBo.setFishingLicence(fishingLicence);
        return assetBo;
    }
    
    private static ContactInfo mapToContactInfo(RolePersonType rolePerson) {
        ContactInfo assetContact = new ContactInfo();
        assetContact.setType("Person");
        assetContact.setEmail(rolePerson.getEmail());
        if (rolePerson.getMobilePhone() != null) {
            assetContact.setPhoneNumber(rolePerson.getMobilePhone().getTelephoneNumber());
        } else if (rolePerson.getHomePhone() != null) {
            assetContact.setPhoneNumber(rolePerson.getHomePhone().getTelephoneNumber());
        }
        assetContact.setName(rolePerson.getPersonAdress().getName().getGivenname() + " " + rolePerson.getPersonAdress().getName().getSurname());
        assetContact.setStreetName(rolePerson.getPersonAdress().getStreet());
        assetContact.setZipCode(rolePerson.getPersonAdress().getZipcode() != null ? rolePerson.getPersonAdress().getZipcode().toString() : null);
        assetContact.setCityName(rolePerson.getPersonAdress().getCity());
        
        return assetContact;
    }

    public static ContactInfo mapToContactInfo(se.havochvatten.service.client.orgpersws.v1_3.RolePersonType rolePerson) {
        ContactInfo assetContact = new ContactInfo();
        assetContact.setType("Person");
        assetContact.setEmail(rolePerson.getEmail());
        if (rolePerson.getMobilePhone() != null) {
            assetContact.setPhoneNumber(rolePerson.getMobilePhone().getTelephoneNumber());
        } else if (rolePerson.getHomePhone() != null) {
            assetContact.setPhoneNumber(rolePerson.getHomePhone().getTelephoneNumber());
        }
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
        if (organisationType.getPhone1() != null && organisationType.getPhone1().getTelephoneNumber() != null) {
            assetContact.setPhoneNumber(organisationType.getPhone1().getTelephoneNumber());
        } else if (organisationType.getPhone2() != null && organisationType.getPhone2().getTelephoneNumber() != null) {
            assetContact.setPhoneNumber(organisationType.getPhone2().getTelephoneNumber());
        }
        assetContact.setStreetName(organisationType.getOrganisationAdress().getStreet());
        assetContact.setZipCode(organisationType.getOrganisationAdress().getZipcode() != null ? organisationType.getOrganisationAdress().getZipcode().toString() : null);
        assetContact.setCityName(organisationType.getOrganisationAdress().getCity());
        return assetContact;
    }
}
