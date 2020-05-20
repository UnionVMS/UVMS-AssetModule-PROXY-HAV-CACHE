package se.havochvatten.vessel.proxy.cache.bean;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.asset.client.model.ContactInfo;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearByIdResponse;
import se.havochvatten.service.client.fishingtripws.v1_0.GetFishingTripListByQueryResponse;
import se.havochvatten.service.client.fishingtripws.v1_0.fishingtrip.LOGTOTAL;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCSResponse;
import se.havochvatten.service.client.notificationws.v4_0.generalnotification.GearChangeNotificationType;
import se.havochvatten.service.client.orgpersws.v1_3.GetPersonByCivicNrResponse;
import se.havochvatten.service.client.orgpersws.v1_3.GetPersonsRepresentedByOrgResponse;
import se.havochvatten.service.client.orgpersws.v1_3.RolePersonType;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselEuFormatByIRCSResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNationResponse;
import se.havochvatten.service.client.vesselws.v2_1.VesselException;
import se.havochvatten.service.client.vesselws.v2_1.vessel.Vessel;
import se.havochvatten.vessel.proxy.cache.constant.ParameterKey;
import se.havochvatten.vessel.proxy.cache.mapper.ResponseMapper;
import se.havochvatten.vessel.proxy.cache.utils.GearChangeNotificationTypeComparator;

@Stateless
public class VesselServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(VesselServiceBean.class);

    @Inject
    ClientProxyBean client;
    
    @Inject
    private PortServiceBean portService;

    @EJB
    private AssetClient assetClient;
    
    @Inject
    private ParameterServiceBean parameterService;

    public List<Vessel> getVesselList(String nation) {
        GetVesselListByNationResponse vesselListByNation;
        try {
            vesselListByNation = client.getVesselListByNation(nation);
        } catch (VesselException e) {
            LOG.error("Could not get vessels by nation {}", nation);
            return new ArrayList<>();
        }
        return vesselListByNation.getVessel();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void enrichVesselsAndSendToAsset(List<Vessel> vesselList)  {
        for (Vessel vessel : vesselList) {
            enrichVesselAndSendToAsset(vessel);
        }
    }

    public void enrichVesselAndSendToAsset(Vessel vessel) {
        try {
            AssetDTO asset = ResponseMapper.mapToAsset(vessel);
            String portOfRegistration = asset.getPortOfRegistration() + " " + portService.getPorts().getOrDefault(asset.getPortOfRegistration(), "Unknown harbour code");
            asset.setPortOfRegistration(portOfRegistration);
            
            GetVesselAndOwnerListByIdResponse owners = client.getVesselAndOwnerListById(vessel.getVesselId());
            List<ContactInfo> contacts = new ArrayList<>();
            if (owners != null) {
                ResponseMapper.enrichWithOrganisation(asset, owners.getOwner());
                contacts = ResponseMapper.mapToContactInfo(owners.getOwner());
            }

            if (asset.getProdOrgCode() != null) {
                GetPersonsRepresentedByOrgResponse personByOrg = client.getPersonByOrg(asset.getProdOrgCode());
                if (personByOrg != null) {
                    for (RolePersonType person : personByOrg.getRolePerson()) {
                        contacts.add(ResponseMapper.mapToContactInfo(person));
                    }
                }
            }

            if (asset.getFlagStateCode().equals("SWE")) {
                contacts.addAll(getMastersBasedOnDeparturesLast30Days(asset.getIrcs()));
            }

            GetVesselEuFormatByIRCSResponse vesselEuFormat = client.getVesselEuFormatByIRCS(vessel.getIrcs());
            if (vesselEuFormat != null) {
                ResponseMapper.enrichAssetWithEuFormatInformation(asset, vesselEuFormat.getVesselEuFormat());
            }
            
            if (vessel.getIrcs() != null) {
                GetGearChangeNotificationListByVesselIRCSResponse gearType = client.getGearTypeByIRCS(vessel.getIrcs());
                setGearTypeInformation(asset, gearType);
            }
            
            assetClient.upsertAssetAsync(ResponseMapper.mapToAssetBO(asset, contacts));
        } catch (JMSException e) {
            LOG.error("Could not send message to Asset. Vessel: {}", vessel.getVesselId());
        } catch (Exception e) {
            LOG.error("Exception occured when sending vessels to Asset", e);
        }
    }

    public Vessel getVesselByIrcs(String ircs) throws VesselException {
        return client.getVesselByIrcs(ircs).getVessel();
    }
    
    public Vessel getVesselByCfr(String cfr) throws VesselException {
        return client.getVesselByCfr(cfr).getVessel();
    }

    private void setGearTypeInformation(AssetDTO asset, GetGearChangeNotificationListByVesselIRCSResponse gearType) {
        if (gearType != null && !gearType.getGearChangeNotification().isEmpty()) {
            // Sort the gear types by latest date
            Collections.sort(gearType.getGearChangeNotification(), new GearChangeNotificationTypeComparator());
            GearChangeNotificationType gearChangeNotificationType = gearType.getGearChangeNotification().get(0);
            GetGearByIdResponse gearTypeByCode = getGearTypeByCode(gearChangeNotificationType);
            if (gearTypeByCode != null) {
                asset.setMainFishingGearCode(gearTypeByCode.getGear().getFaoCode());
                asset.setGearFishingType(gearTypeByCode.getGear().getGearType().getNameEng());
            } else {
                asset.setGearFishingType("Unknown");
            }
        } else {
            asset.setGearFishingType("Unknown");
        }
    }

    private GetGearByIdResponse getGearTypeByCode(GearChangeNotificationType gearChangeNotificationType) {
        try {
            return client.getGearTypeByCode(BigInteger.valueOf(gearChangeNotificationType.getGearCode()));
        } catch (Exception e) {
            return null;
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

    private Collection<ContactInfo> getMastersBasedOnDeparturesLast30Days(String ircs) {
        GetFishingTripListByQueryResponse trips = client.getFishingTripsByDepartureLast30Days(ircs);
        Map<String, ContactInfo> commanders = new HashMap<>();
        if (trips != null) {
            if (!trips.getFishingTrip().isEmpty()) {
                LOG.info("Found {} trips for {}", trips.getFishingTrip().size(), ircs);
            }
            for (LOGTOTAL trip : trips.getFishingTrip()) {
                String civicNumber = trip.getCIVICNR();
                GetPersonByCivicNrResponse person = client.getPersonByCivicNumber(Long.valueOf(civicNumber));
                ContactInfo contactInfo = ResponseMapper.mapToContactInfo(person.getRolePerson());
                contactInfo.setType("Master");
                commanders.put(civicNumber, contactInfo);
            }
        }
        return commanders.values();
    }
}
