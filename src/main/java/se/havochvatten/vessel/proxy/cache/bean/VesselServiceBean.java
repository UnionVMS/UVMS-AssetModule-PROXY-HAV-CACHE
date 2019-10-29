package se.havochvatten.vessel.proxy.cache.bean;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.asset.client.model.ContactInfo;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearByIdResponse;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCSResponse;
import se.havochvatten.service.client.notificationws.v4_0.generalnotification.GearChangeNotificationType;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselEuFormatByCFRResponse;
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

    public void enrichVesselsAndSendToAsset(List<Vessel> vesselList)  {
        for (Vessel vessel : vesselList) {
            enrichVesselAndSendToAsset(vessel);
        }
    }

    public void enrichVesselAndSendToAsset(Vessel vessel) {
        try {
            AssetDTO asset = ResponseMapper.mapToAsset(vessel);
            
            GetVesselAndOwnerListByIdResponse owners = client.getVesselAndOwnerListById(vessel.getVesselId());
            List<ContactInfo> contacts = new ArrayList<>();
            if (owners != null) {
                ResponseMapper.enrichWithOrganisation(asset, owners.getOwner());
                contacts = ResponseMapper.mapToContactInfo(owners.getOwner());
            }
            
            GetVesselEuFormatByCFRResponse vesselEuFormat = client.getVesselEuFormatByCFR(vessel.getCfr());
            if (vesselEuFormat != null) {
                ResponseMapper.enrichAssetWithEuFormatInformation(asset, vesselEuFormat.getVesselEuFormat());
            }
            
            GetGearChangeNotificationListByVesselIRCSResponse gearType = client.getGearTypeByIRCS(vessel.getIrcs());
            setGearTypeInformation(asset, gearType);
            
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
}
