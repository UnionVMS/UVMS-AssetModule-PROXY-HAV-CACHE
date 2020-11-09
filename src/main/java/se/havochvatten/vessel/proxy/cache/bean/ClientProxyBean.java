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

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.datatype.DatatypeFactory;
import org.slf4j.LoggerFactory;
import se.havochvatten.service.client.authlic_v2ws.v1_0.AuthLicException;
import se.havochvatten.service.client.authlic_v2ws.v1_0.GetFishingLicenceByCFR;
import se.havochvatten.service.client.authlic_v2ws.v1_0.GetFishingLicenceByCFRResponse;
import se.havochvatten.service.client.equipmentws.v1_0.EquipmentException;
import se.havochvatten.service.client.equipmentws.v1_0.EquipmentPortType;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearById;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearByIdResponse;
import se.havochvatten.service.client.equipmentws.v1_0.GetGears;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearsResponse;
import se.havochvatten.service.client.fishingtripws.v1_0.GetFishingTripListByQuery;
import se.havochvatten.service.client.fishingtripws.v1_0.GetFishingTripListByQueryResponse;
import se.havochvatten.service.client.fishingtripws.v1_0.fishingtrip.DateToFromSearchType;
import se.havochvatten.service.client.fishingtripws.v1_0.fishingtrip.TripSearchReqType;
import se.havochvatten.service.client.fishingtripws.v1_0.fishingtrip.TripSearchReqType.Search;
import se.havochvatten.service.client.fishingtripws.v1_0.fishingtrip.VesselListSearchType;
import se.havochvatten.service.client.geographyws.v2_0.GeographyException;
import se.havochvatten.service.client.geographyws.v2_0.PortInformationType;
import se.havochvatten.service.client.notificationws.v4_0.GeneralNotificationPortType;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCS;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCSResponse;
import se.havochvatten.service.client.notificationws.v4_0.NotificationException;
import se.havochvatten.service.client.orgpersws.v1_3.GetOrgByOrgNr;
import se.havochvatten.service.client.orgpersws.v1_3.GetOrgByOrgNrResponse;
import se.havochvatten.service.client.orgpersws.v1_3.GetPersonByCivicNr;
import se.havochvatten.service.client.orgpersws.v1_3.GetPersonByCivicNrResponse;
import se.havochvatten.service.client.orgpersws.v1_3.GetPersonsRepresentedByOrg;
import se.havochvatten.service.client.orgpersws.v1_3.GetPersonsRepresentedByOrgResponse;
import se.havochvatten.service.client.orgpersws.v1_3.OrgPersException;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListById;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.service.client.vesselcompws.v2_0.VesselCompPortType;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselByCFR;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselByCFRResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselByIRCS;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselByIRCSResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselEuFormatByCFR;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselEuFormatByCFRResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselEuFormatByIRCS;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselEuFormatByIRCSResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNation;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNationResponse;
import se.havochvatten.service.client.vesselws.v2_1.VesselException;
import se.havochvatten.vessel.proxy.cache.mapper.RequestMapper;

@Stateless
public class ClientProxyBean {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ClientProxyBean.class);

    @EJB
    PortInitiatorServiceBean port;

    public GetVesselListByNationResponse getVesselListByNation(String iso3AlphaNation) throws VesselException {
        GetVesselListByNation getVesselListByNation = RequestMapper.mapToGetVesselListByNation(iso3AlphaNation);
        return port.getVesselPortType().getVesselListByNation(getVesselListByNation);
    }
    
    public GetVesselByIRCSResponse getVesselByIrcs(String ircs) throws VesselException {
        GetVesselByIRCS request = new GetVesselByIRCS();
        request.setIrcs(ircs);
        return port.getVesselPortType().getVesselByIRCS(request);
    }

    public GetVesselEuFormatByIRCSResponse getVesselEuFormatByIrcs(String ircs) throws VesselException {
        GetVesselEuFormatByIRCS request = new GetVesselEuFormatByIRCS();
        request.setIrcs(ircs);
        return port.getVesselPortType().getVesselEuFormatByIRCS(request);
    }

    public GetVesselByCFRResponse getVesselByCfr(String cfr) throws VesselException {
        GetVesselByCFR request = new GetVesselByCFR();
        request.setCfr(cfr);
        return port.getVesselPortType().getVesselByCFR(request);
    }

    public GetVesselEuFormatByCFRResponse getVesselEuFormatByCfr(String cfr) throws VesselException {
        GetVesselEuFormatByCFR request = new GetVesselEuFormatByCFR();
        request.setCfr(cfr);
        return port.getVesselPortType().getVesselEuFormatByCFR(request);
    }

    public GetVesselAndOwnerListByIdResponse getVesselAndOwnerListById(String id) {
        if (id == null) {
            return null;
        }
        GetVesselAndOwnerListById getVesselAndOwnerListById = RequestMapper.mapToGetVesselAndOwnerListById(id);
        VesselCompPortType vesselCompServicePortType = port.getVesselCompServicePortType();
        GetVesselAndOwnerListByIdResponse response = null;
        try {
            response = vesselCompServicePortType.getVesselAndOwnerListById(getVesselAndOwnerListById);
        } catch (se.havochvatten.service.client.vesselcompws.v2_0.VesselException e) {
            LOG.warn("Could not get vessel and owner by id: {}", id, e);
        }
        return response;
    }

    public GetGearChangeNotificationListByVesselIRCSResponse getGearTypeByIRCS(String ircs) {
        GeneralNotificationPortType generalNotificationPortType = port.getGeneralNotificationPortType();
        GetGearChangeNotificationListByVesselIRCS gearChangeNotificationListByVesselIRCS = new GetGearChangeNotificationListByVesselIRCS();
        GetGearChangeNotificationListByVesselIRCSResponse response = null;
        gearChangeNotificationListByVesselIRCS.setIrcs(ircs);
        try {
             response = generalNotificationPortType.getGearChangeNotificationListByVesselIRCS(gearChangeNotificationListByVesselIRCS);
        } catch (NotificationException e) {
            LOG.warn("Could not get gear types for vessel with ircs: {}", ircs, e);
        }
        return response;
    }

    public GetGearByIdResponse getGearTypeByCode(BigInteger id) {
        GetGearById getGearById = new GetGearById();
        getGearById.setId(id);
        GetGearByIdResponse gearById = null;
        try {
            gearById = port.getEquipmentPortType().getGearById(getGearById);
        } catch (EquipmentException e) {
            LOG.warn("Could not get gear type information, gear type id: {}", id, e);
        }
        return gearById;
    }

    public GetGearsResponse getGearTypeList() {
        GetGears getGearsRequest = new GetGears();
        GetGearsResponse gears = null;
        EquipmentPortType equipmentPortType = port.getEquipmentPortType();
        try {
            gears = equipmentPortType.getGears(getGearsRequest);
        } catch (EquipmentException e) {
            LOG.warn("Could not get gear type list", e);
        }
        return gears;
    }

    public GetVesselEuFormatByIRCSResponse getVesselEuFormatByIRCS(String ircs) {
        if (ircs == null) {
            return null;
        }
        String modifiedIrcs = ircs.replace("-", "");
        GetVesselEuFormatByIRCS getVesselEuFormatParam = new GetVesselEuFormatByIRCS();
        getVesselEuFormatParam.setIrcs(modifiedIrcs);
        GetVesselEuFormatByIRCSResponse response = null;
        try {
            response = port.getVesselPortType().getVesselEuFormatByIRCS(getVesselEuFormatParam);
        } catch (VesselException e) {
            LOG.warn("Could not get vessel in eu format, ircs: {}, {}", modifiedIrcs, e.getMessage());
        }
        return response;
    }
    
    public List<PortInformationType> getPorts() throws GeographyException {
        return port.getGeographyPortType().getListPorts(Arrays.asList("SE"));
    }

    public GetPersonsRepresentedByOrgResponse getPersonByOrg(String orgNumber) {
        try {
            GetPersonsRepresentedByOrg request = new GetPersonsRepresentedByOrg();
            request.setOrgNumber(orgNumber);
            return port.getOrgPersPortType().getPersonsRepresentedByOrg(request);
        } catch (OrgPersException e) {
            LOG.warn("Could not get person by organisation: {}, {}", orgNumber, e.getMessage());
            return null;
        }
    }

    public GetPersonByCivicNrResponse getPersonByCivicNumber(long civicNr) {
        try {
            GetPersonByCivicNr personByCivicNr = new GetPersonByCivicNr();
            personByCivicNr.setCivicNumber(civicNr);
            return port.getOrgPersPortType().getPersonByCivicNr(personByCivicNr);
        } catch (OrgPersException e) {
            LOG.warn("Could not get person by civicNr: {}, {}", civicNr, e.getMessage());
            return null;
        }
    }

    public GetOrgByOrgNrResponse getOrgByOrgNumber(String orgNr) {
        try {
            GetOrgByOrgNr orgByorgNr = new GetOrgByOrgNr();
            orgByorgNr.setOrgNumber(orgNr);
            return port.getOrgPersPortType().getOrgByOrgNr(orgByorgNr);
        } catch (OrgPersException e) {
            LOG.warn("Could not get organisation by orgNr: {}, {}", orgNr, e.getMessage());
            return null;
        }
    }

    public GetFishingTripListByQueryResponse getFishingTripsByDepartureLast30Days(String ircs) {
        try {
            GetFishingTripListByQuery query = new GetFishingTripListByQuery();
            TripSearchReqType tripSearch = new TripSearchReqType();
            Search search = new Search();
            VesselListSearchType vesselListSearch = new VesselListSearchType();
            vesselListSearch.getIrcs().add(ircs);
            search.setVesselList(vesselListSearch);
            DateToFromSearchType dateSearch = new DateToFromSearchType();
            Instant fromDate = Instant.now().minus(30, ChronoUnit.DAYS);
            dateSearch.setFrom(DatatypeFactory.newInstance().newXMLGregorianCalendar(fromDate.toString()));
            Instant to = Instant.now();
            dateSearch.setTo(DatatypeFactory.newInstance().newXMLGregorianCalendar(to.toString()));
            search.setDepartureDate(dateSearch);
            tripSearch.setSearch(search);
            query.setTripSearchReq(tripSearch);
            return port.getFishingTripPortType().getFishingTripListByQuery(query);
        } catch (Exception e) {
            LOG.warn("Could not get fishing trip by IRCS: {}, {}", ircs, e.getMessage());
            return null;
        }
    }

    public GetFishingLicenceByCFRResponse getFishingLicenceByCFR(String cfr) {
        try {
            GetFishingLicenceByCFR request = new GetFishingLicenceByCFR();
            request.setCfr(cfr);
            return port.getAuthLicPortType().getFishingLicenceByCFR(request);
        } catch (AuthLicException e) {
            LOG.warn("Could not get fishing licence by cfr: {}, {}", cfr, e.getMessage());
            return null;
        }
    }
}