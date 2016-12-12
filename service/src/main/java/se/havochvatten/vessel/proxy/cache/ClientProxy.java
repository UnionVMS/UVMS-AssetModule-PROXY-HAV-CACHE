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
package se.havochvatten.vessel.proxy.cache;

import se.havochvatten.service.client.equipmentws.v1_0.GetGearByIdResponse;
import se.havochvatten.service.client.equipmentws.v1_0.GetGearsResponse;
import se.havochvatten.service.client.notificationws.v4_0.GetGearChangeNotificationListByVesselIRCSResponse;
import se.havochvatten.service.client.vesselcompws.v2_0.GetVesselAndOwnerListByIdResponse;
import se.havochvatten.service.client.vesselws.v2_1.GetVesselListByNationResponse;
import se.havochvatten.vessel.proxy.cache.exception.ProxyException;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.math.BigInteger;

/**
 *
 * @author jojoha
 */
@Local
public interface ClientProxy {

    public GetVesselListByNationResponse getVesselListByNation(String value) throws ProxyException;
    public GetVesselAndOwnerListByIdResponse getVesselAndOwnerListById(String id) throws ProxyException;
    public GetGearChangeNotificationListByVesselIRCSResponse getGearTypeByIRCS(String ircs) throws ProxyException;
    public GetGearByIdResponse getGearTypeByCode(BigInteger id) throws ProxyException;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    GetGearsResponse getGearTypeList();
}
