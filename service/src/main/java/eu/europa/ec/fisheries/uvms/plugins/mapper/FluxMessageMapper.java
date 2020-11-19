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
package eu.europa.ec.fisheries.uvms.plugins.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.plugins.constants.Codes;
import eu.europa.ec.fisheries.uvms.plugins.constants.Codes.FLUXVesselPositionType;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.FLUXPartyType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.FLUXReportDocumentType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselCountryType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselGeographicalCoordinateType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselPositionEventType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselTransportMeansType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.DateTimeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.MeasureType;

public class FluxMessageMapper {
    
    private static final String PURPOSE_CODE = "9";
    
    private FluxMessageMapper() {}
    
    public static FLUXVesselPositionMessage mapToFluxVesselPositionMessage(MovementType movement, String fluxOwner) {
        FLUXVesselPositionMessage message = new FLUXVesselPositionMessage();
        message.setFLUXReportDocument(mapToReportDocument(fluxOwner, movement.getInternalReferenceNumber()));
        message.setVesselTransportMeans(mapToVesselTransportMeans(movement));
        return message;
    }

    private static FLUXReportDocumentType mapToReportDocument(String fluxOwner, String referenceNumber) {
        FLUXReportDocumentType doc = new FLUXReportDocumentType();
        doc.getIDS().add(mapToIdType(UUID.randomUUID().toString()));
        doc.setCreationDateTime(mapToNowDateTime());
        doc.setPurposeCode(mapToCodeType(PURPOSE_CODE));
        doc.setOwnerFLUXParty(mapToFluxPartyType(fluxOwner));
        return doc;
    }
    
    private static VesselTransportMeansType mapToVesselTransportMeans(MovementType movement) {
        VesselTransportMeansType retVal = new VesselTransportMeansType();
        Map<AssetIdType, String> ids = movement.getAssetId().getAssetIdList()
                .stream()
                .collect(Collectors.toMap(AssetIdList::getIdType, AssetIdList::getValue));
        if (movement.getIrcs() != null) {
            retVal.getIDS().add(mapToIdType(Codes.FLUXVesselIDType.IRCS.name(), movement.getIrcs()));
        }
        if (movement.getExternalMarking() != null) {
            retVal.getIDS().add(mapToIdType(Codes.FLUXVesselIDType.EXT_MARK.name(), movement.getExternalMarking()));
        }
        if (ids.containsKey(AssetIdType.CFR)) {
            retVal.getIDS().add(mapToIdType(Codes.FLUXVesselIDType.CFR.name(), ids.get(AssetIdType.CFR)));
        }
        retVal.getIDS().add(mapToIdType(Codes.FLUXVesselIDType.UUID.name(), movement.getConnectId()));
        retVal.getIDS().add(mapToIdType(Codes.FLUXVesselIDType.NAME.name(), movement.getAssetName()));
        retVal.setRegistrationVesselCountry(mapToVesselCountry(movement.getFlagState()));
        retVal.getSpecifiedVesselPositionEvents().add(mapToVesselPosition(movement));
        return retVal;
    }
    
    private static IDType mapToIdType(String value) {
        IDType id = new IDType();
        id.setValue(value);
        return id;
    }
    
    private static IDType mapToIdType(String schemeId, String value) {
        IDType idType = new IDType();
        idType.setSchemeID(schemeId);
        idType.setValue(value);
        return idType;
    }
    
    private static CodeType mapToCodeType(String value) {
        CodeType codeType = new CodeType();
        codeType.setValue(value);
        return codeType;
    }
    
    private static FLUXPartyType mapToFluxPartyType(String ad) {
        FLUXPartyType partyType = new FLUXPartyType();
        partyType.getIDS().add(mapToIdType(ad));
        return partyType;
    }
    
    private static VesselCountryType mapToVesselCountry(String countryCode) {
        VesselCountryType vesselCountry = new VesselCountryType();
        vesselCountry.setID(mapToIdType(countryCode));
        return vesselCountry;
    }
    
    private static MeasureType mapToMeasureType(Double measuredSpeed) {
        MeasureType measureType = new MeasureType();
        measureType.setValue(BigDecimal.valueOf(measuredSpeed));
        return measureType;
    }
    
    private static VesselPositionEventType mapToVesselPosition(MovementType movement) {
        VesselPositionEventType position = new VesselPositionEventType();
        position.setObtainedOccurrenceDateTime(mapToDateTime(movement.getPositionTime()));
        if (movement.getReportedCourse() != null && movement.getReportedCourse() > -1) {
            position.setCourseValueMeasure(mapToMeasureType(movement.getReportedCourse()));
        }
        if (movement.getReportedSpeed() != null) {
            position.setSpeedValueMeasure(mapToMeasureType(movement.getReportedSpeed()));
        }
        position.setTypeCode(mapToCodeType(FLUXVesselPositionType.fromInternal(movement.getMovementType())));
        position.setSpecifiedVesselGeographicalCoordinate(mapToCoordinateType(movement.getPosition()));
        return position;
    }
    
    private static VesselGeographicalCoordinateType mapToCoordinateType(MovementPoint point) {
        VesselGeographicalCoordinateType geoType = new VesselGeographicalCoordinateType();
        geoType.setLatitudeMeasure(mapToMeasureType(point.getLatitude()));
        geoType.setLongitudeMeasure(mapToMeasureType(point.getLongitude()));
        if (point.getAltitude() != null) {
            geoType.setAltitudeMeasure(mapToMeasureType(point.getAltitude()));
        }
        return geoType;
    }
    
    private static DateTimeType mapToNowDateTime() {
        return mapToDateTime(new Date());
    }
    
    private static DateTimeType mapToDateTime(Date date) {
        try {
            DateTimeType dateTime = new DateTimeType();
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            dateTime.setDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
            return dateTime;
        } catch (DatatypeConfigurationException ex) {
            return new DateTimeType();
        }
    }
}
