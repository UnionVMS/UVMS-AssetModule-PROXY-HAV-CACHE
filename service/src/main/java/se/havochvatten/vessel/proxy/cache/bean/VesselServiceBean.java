package se.havochvatten.vessel.proxy.cache.bean;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.Asset;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetBO;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearByIdResponse;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCSResponse;
import se.havochvatten.service.client.notificationws.v4_0.generalnotification.GearChangeNotificationType;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNationResponse;
import se.havochvatten.service.client.vesselws.v2_1.vessel.Vessel;
import se.havochvatten.vessel.proxy.cache.ClientProxy;
import se.havochvatten.vessel.proxy.cache.ParameterService;
import se.havochvatten.vessel.proxy.cache.Utils.GearChangeNotificationTypeComparator;
import se.havochvatten.vessel.proxy.cache.Utils.Validate;
import se.havochvatten.vessel.proxy.cache.constant.ParameterKey;
import se.havochvatten.vessel.proxy.cache.exception.ProxyException;
import se.havochvatten.vessel.proxy.cache.exception.ValidationException;
import se.havochvatten.vessel.proxy.cache.mapper.ResponseMapper;

@LocalBean
@Stateless
public class VesselServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(VesselServiceBean.class);

    @EJB
    ClientProxy client;

    @EJB
    AssetClient assetClient;
    
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

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void sendVesselAndOwnerInfoToAssetModule(List<Vessel> vesselList)  {
        for (Vessel vessel : vesselList) {
            long start = System.currentTimeMillis();
            GetVesselAndOwnerListByIdResponse vesselAndOwnerListById;
            try {
                vesselAndOwnerListById = client.getVesselAndOwnerListById(vessel.getVesselId());
                Validate.validateGetVesselAndOwnerListByIdResponse(vesselAndOwnerListById);
                AssetBO assetBo = ResponseMapper.mapToAsset(vesselAndOwnerListById);
                GetGearChangeNotificationListByVesselIRCSResponse gearType = client.getGearTypeByIRCS(vesselAndOwnerListById.getVessel().getIrcs());
                setGearTypeInformation(assetBo.getAsset(), gearType);
                //TODO: Remove when we know how to get this gear type
//                asset.setGearType("PELAGIC");
                assetBo.getAsset().setGearFishingType(1);
//                sendUpsertAssetModuleRequest(assetBo);
                assetClient.upsertAssetAsync(assetBo);
                long totalTime = System.currentTimeMillis() - start;
                LOG.debug("Vessel id: " +vesselAndOwnerListById.getVessel().getVesselId() + " Owner: " +vesselAndOwnerListById.getOwner().size() +" Total time in ms: "  + totalTime);
            } catch (ProxyException e) {
                LOG.error("Could not get additional info for vessel with id: {}", vessel.getVesselId());
            } catch (ValidationException e) {
                LOG.error(e.getMessage());
            } catch (JsonProcessingException e) {
                LOG.error("Could not process JSON. Vessel: {}", vessel.getVesselId());
            } catch (MessageException e) {
                LOG.error("Could not send message to Asset. Vessel: {}", vessel.getVesselId());
            }
        }
    }

    private void setGearTypeInformation(Asset asset, GetGearChangeNotificationListByVesselIRCSResponse gearType) {
        if (gearType.getGearChangeNotification().size() > 0) {
            // Sort the gear types by latest date
            Collections.sort(gearType.getGearChangeNotification(), new GearChangeNotificationTypeComparator());
            GearChangeNotificationType gearChangeNotificationType = gearType.getGearChangeNotification().get(0);
            asset.setMainFishingGearCode(getGearTypeCode(gearChangeNotificationType.getGearCode()));
        }
    }

    private String getGearTypeCode(Long id) {
        String gearTypeInfo = null;
        GetGearByIdResponse gearTypeByCode;
        if(id == null){
            LOG.error("Gear type id cannot be null");
        }else {
            try {
                gearTypeByCode = client.getGearTypeByCode(BigInteger.valueOf(id));
                if (gearTypeByCode != null) {
                    gearTypeInfo = gearTypeByCode.getGear().getId() + " " + gearTypeByCode.getGear().getNameEng();
                }
            } catch (ProxyException e) {
                LOG.error("Could not set gear type");
            }
        }
        return gearTypeInfo;
    }


//    private void sendUpsertAssetModuleRequest(Asset asset){
//        try {
//            String upsertAssetModuleRequest = AssetModuleRequestMapper.createUpsertAssetModuleRequest(asset, Constants.NATIONAL);
//            String s = proxyMessageSender.sendMessage(assetModuleQueue, responseModuleQueue, upsertAssetModuleRequest);
//        } catch (AssetModelMarshallException e) {
//            LOG.error("Could not map asset to createUpsertAssetModuleRequest", e.getMessage());
//        }catch (ProxyException e) {
//            LOG.error("Could not sen upsert message to Asset module with queue: " + Constants.ASSET_MODULE_QUEUE, e.getMessage());
//        }
//    }

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
