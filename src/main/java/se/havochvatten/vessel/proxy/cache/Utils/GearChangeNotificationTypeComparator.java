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

import se.havochvatten.service.client.notificationws.v4_0.generalnotification.GearChangeNotificationType;

import java.util.Comparator;

public class GearChangeNotificationTypeComparator implements Comparator<GearChangeNotificationType> {

    @Override
    public int compare(GearChangeNotificationType o1, GearChangeNotificationType o2) {
        if(o1 == null && o2 == null){
            return 0;
        }else if(o1 == null && o2!=null){
            return 1;
        }else if(o1!= null && o2 == null){
            return -1;
        }else{
            return o2.getStartTime().compare(o1.getStartTime());
        }
    }
}
