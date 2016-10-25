package se.havochvatten.vessel.proxy.cache.Utils;

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

import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.vessel.proxy.cache.exception.ValidationException;

public class Validate {

    public static void  validateGetVesselAndOwnerListByIdResponse(GetVesselAndOwnerListByIdResponse response) throws ValidationException {
        if(response == null){
            throw new ValidationException("GetVesselAndOwnerListByIdResponse is null");
        }

        else if(response.getVessel() == null){
            throw new ValidationException("GetVesselAndOwnerListByIdResponse has no vessel");
        }

        else if(response.getVessel().getIrcs() == null || response.getVessel().getIrcs().isEmpty()){
            throw new ValidationException("Vessel has no IRCS, cannot get GearType if vessel has no IRCS");
        }
    }
}
