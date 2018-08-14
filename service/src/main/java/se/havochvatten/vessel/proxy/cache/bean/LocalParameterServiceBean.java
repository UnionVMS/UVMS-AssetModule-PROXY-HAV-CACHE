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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.havochvatten.vessel.proxy.cache.LocalParameterService;
import se.havochvatten.vessel.proxy.cache.constant.Constants;
import se.havochvatten.vessel.proxy.cache.constant.ParameterKey;
import se.havochvatten.vessel.proxy.cache.entity.Parameter;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class LocalParameterServiceBean implements LocalParameterService {

    @PersistenceContext(unitName = "asset")
    EntityManager em;

    final static Logger LOG = LoggerFactory.getLogger(LocalParameterServiceBean.class);

    @Override
    public String getParameterValue(ParameterKey key)  {
        try {
            Query query = em.createNamedQuery(Constants.FIND_BY_NAME);
            query.setParameter("key", key.getKey());
            Parameter entity = (Parameter) query.getSingleResult();
            return entity.getParamValue();
        }catch (NoResultException e){
            LOG.error("Parameter is not found in database, key: " + key);
            throw e;
        }
    }
}