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
package eu.europa.ec.fisheries.uvms.plugins.service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.RecipientInfoType;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;

@Stateless
public class MessageRestClient {
    
    private static final Logger LOG = LoggerFactory.getLogger(MessageRestClient.class);
    
    private Client client;
    
    @PostConstruct
    public void init() {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);
        client = clientBuilder.build();
    }

    public int postMessage(FLUXVesselPositionMessage message, List<RecipientInfoType> recipientList) {
        String target = getTarget(recipientList);
        try {
            Response response = client.target(target).request().post(Entity.xml(message));
            return response.getStatus();
        } catch (Exception e) {
            LOG.error("Could not send report to {}", target, e);
            return 400; 
        }
    }
    
    private String getTarget(List<RecipientInfoType> recipientList) {
        for (RecipientInfoType recipientInfoType : recipientList) {
            if (recipientInfoType.getKey().contains("REST")) {
                return recipientInfoType.getValue();
            }
        }
        return null;
    }
}
