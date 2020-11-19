package eu.europa.ec.fisheries.uvms.plugins.mapper;

import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementType;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselPositionEventType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselTransportMeansType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.IDType;

public class FluxMessageMapperTest {

    @Test
    public void testMessageMapping() {
        MovementType movement = getMovementType();
        FLUXVesselPositionMessage message = FluxMessageMapper.mapToFluxVesselPositionMessage(movement, "Test");
        VesselTransportMeansType vesselTransportMeans = message.getVesselTransportMeans();
        List<IDType> ids = vesselTransportMeans.getIDS();
        Optional<IDType> ircs = ids.stream().filter(id -> id.getSchemeID().equals("IRCS")).findFirst();
        Optional<IDType> externalMarking = ids.stream().filter(id -> id.getSchemeID().equals("EXT_MARK")).findFirst();
        assertThat(ircs.get().getValue(), CoreMatchers.is(movement.getIrcs()));
        assertThat(externalMarking.get().getValue(), CoreMatchers.is(movement.getExternalMarking()));
        assertThat(vesselTransportMeans.getRegistrationVesselCountry().getID().getValue(), CoreMatchers.is(movement.getFlagState()));
        VesselPositionEventType position = vesselTransportMeans.getSpecifiedVesselPositionEvents().get(0);
        assertThat(position.getSpecifiedVesselGeographicalCoordinate().getLatitudeMeasure().getValue(), CoreMatchers.is(BigDecimal.valueOf(movement.getPosition().getLatitude())));
        assertThat(position.getSpecifiedVesselGeographicalCoordinate().getLongitudeMeasure().getValue(), CoreMatchers.is(BigDecimal.valueOf(movement.getPosition().getLongitude())));
        assertThat(position.getCourseValueMeasure().getValue(), CoreMatchers.is(BigDecimal.valueOf(movement.getReportedCourse())));
        assertThat(position.getSpeedValueMeasure().getValue(), CoreMatchers.is(BigDecimal.valueOf(movement.getReportedSpeed())));
    }

    @Test
    public void testMessageMappingNullCourse() {
        MovementType movement = getMovementType();
        movement.setReportedCourse(null);
        FLUXVesselPositionMessage message = FluxMessageMapper.mapToFluxVesselPositionMessage(movement, "Test");
        VesselTransportMeansType vesselTransportMeans = message.getVesselTransportMeans();
        VesselPositionEventType position = vesselTransportMeans.getSpecifiedVesselPositionEvents().get(0);
        assertThat(position.getCourseValueMeasure(), CoreMatchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void testMessageMappingNegativeCourse() {
        MovementType movement = getMovementType();
        movement.setReportedCourse(-1d);
        FLUXVesselPositionMessage message = FluxMessageMapper.mapToFluxVesselPositionMessage(movement, "Test");
        VesselTransportMeansType vesselTransportMeans = message.getVesselTransportMeans();
        VesselPositionEventType position = vesselTransportMeans.getSpecifiedVesselPositionEvents().get(0);
        assertThat(position.getCourseValueMeasure(), CoreMatchers.is(CoreMatchers.nullValue()));
    }

    private MovementType getMovementType() {
        MovementType movement = new MovementType();
        movement.setIrcs("IRCS");
        AssetId assetId = new AssetId();
        AssetIdList assetIdList = new AssetIdList();
        assetIdList.setIdType(AssetIdType.CFR);
        assetIdList.setValue("CFR123456789");
        assetId.getAssetIdList().add(assetIdList);
        movement.setAssetId(assetId);
        movement.setExternalMarking("EXTMARK");
        MovementPoint point = new MovementPoint();
        point.setLatitude(56d);
        point.setLongitude(11d);
        movement.setPositionTime(new Date());
        movement.setPosition(point);
        movement.setReportedCourse(123d);
        movement.setReportedSpeed(3.5);
        movement.setFlagState("UNK");
        return movement;
    }
}