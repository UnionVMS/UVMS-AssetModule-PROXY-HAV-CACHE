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

import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListById;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNation;
import se.havochvatten.service.client.vesselws.v2_1.vessel.DefaultPortType;
import se.havochvatten.service.client.vesselws.v2_1.vessel.Vessel;
import se.havochvatten.service.client.vesselws.v2_1.vessel.VesselEuFormatType;

public class RequestMapper {

    private RequestMapper() {}

    public static GetVesselListByNation mapToGetVesselListByNation(String iso3AlphaNation) {
        GetVesselListByNation getVesselListByNation = new GetVesselListByNation();
        getVesselListByNation.setIso3AlphaNation(iso3AlphaNation);
        return getVesselListByNation;
    }

    public static GetVesselAndOwnerListById mapToGetVesselAndOwnerListById(String id){
        GetVesselAndOwnerListById getVesselAndOwnerListById = new GetVesselAndOwnerListById();
        getVesselAndOwnerListById.setVesselId(id);
        return getVesselAndOwnerListById;
    }

    public static Vessel mapEuFormatToVessel(VesselEuFormatType vesselEuFormat) {
        Vessel vessel = new Vessel();
        vessel.setActive(true);
        vessel.setIso3AlphaNation(vesselEuFormat.getRegistration().getCountryOfRegistration());
        vessel.setVesselName(vesselEuFormat.getIdentification().getNameOfVessel());

        vessel.setEuTon(vesselEuFormat.getDimension().getTonnageGT());
        vessel.setLoa(vesselEuFormat.getDimension().getLoa());
        DefaultPortType port = new DefaultPortType();
        port.setPort(vesselEuFormat.getRegistration().getPortCode());
        vessel.setDefaultPort(port);

        vessel.setDistrict(vesselEuFormat.getIdentification().getExternalMarking());
        vessel.setCfr(vesselEuFormat.getIdentification().getCfr());
        vessel.setImoNumber(vesselEuFormat.getIdentification().getImo());
        vessel.setIrcs(vesselEuFormat.getIdentification().getIrcs());

        vessel.setEnginePower(vesselEuFormat.getEngine().getPowerOfAuxEngine());
        vessel.setHasLicense(vesselEuFormat.getEquipment().isLicenceIndicator());
        vessel.setHasVms(vesselEuFormat.getEquipment().isVmsIndicator());
        return vessel;
    }
}
