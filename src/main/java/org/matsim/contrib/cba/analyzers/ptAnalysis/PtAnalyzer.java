package org.matsim.contrib.cba.analyzers.ptAnalysis;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.cba.analyzers.CbaAnalyzer;
import org.matsim.contrib.cba.utils.Tuple;
import org.matsim.core.api.experimental.events.TeleportationArrivalEvent;
import org.matsim.core.api.experimental.events.handler.TeleportationArrivalEventHandler;
import org.matsim.core.events.MobsimScopeEventHandler;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.vehicles.Vehicle;

import java.util.*;

/*
TODO Distances:
 - Distance per person
 - Distance per vehicle (distinguished per mode)
    - Establish a vehicles to modes map with the hypothesis that one vehicle can serve only one mode through the simulation
 - Make sure to get the actual performed distance & not the planned one
 - Access time to PT, DRT.... use the "X interaction" mode
    - Trip is composed of : access - wait - in vehicle - egress
 - Applied Fares
    - Not grave if two fares of the same mode are swapped
    - Can begin without the pt fare

 */

//TODO replace Map<Id... with IdMap
//TODO Follow VehicleEntersLink & VehicleLeavesLink events to compute the travelled distance
public class PtAnalyzer implements PersonDepartureEventHandler, PersonArrivalEventHandler, ActivityEndEventHandler, ActivityStartEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, LinkEnterEventHandler, LinkLeaveEventHandler, TeleportationArrivalEventHandler, TransitDriverStartsEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler, MobsimScopeEventHandler, CbaAnalyzer {

    private static final String[] TRIPS_HEADERS = new String[]{"personID", "purpose", "inVehicleTime", "totalWaitingTime", "accessTime", "egressTime", "transferTime", "segmentIndex", "mode", "vehicleId", "pt_line", "pt_mode", "segmentAccessTime", "segmentEgressTime", "waitingTime", "travelDuration", "travelDistance"};
    private static final String[] VEHICLES_HEADERS = new String[]{"vehicleID", "lineID", "mode", "departureTime", "arrivalTime", "totalDistance"};

    private final Map<Id<Vehicle>, String> vehiclesToModes = new HashMap<>();
    private final Map<Id<Person>, PersonDepartureEvent> agentDepartures = new HashMap();
    private final Map<Id<Person>, PersonArrivalEvent> agentArrivals = new HashMap<>();
    private final Map<Id<Person>, List<Tuple<Tuple<PersonEntersVehicleEvent, Double>, PersonLeavesVehicleEvent>>> agentsInVehicles = new HashMap<>();
    private final Map<Id<Vehicle>, Double> vehiclesDistances = new HashMap<>();
    private final Map<Id<Vehicle>, Set<Id<Person>>> vehiclesToPassengers = new HashMap<>();
    private final Map<Id<Person>, ActivityEndEvent> previousActivities = new HashMap<>();
    private final Map<Id<Person>, PtTrip> currentPtTrips = new HashMap<>();
    private final Network network;
    private final PtAnalyzerConfigGroup configGroup;
    private final Scenario scenario;
    private final Map<Id<Vehicle>, Id<TransitLine>> vehiclesToTransitLines = new HashMap<>();
    private final Map<Id<Vehicle>, Id<TransitRoute>> vehiclesToTransitRoutes = new HashMap<>();
    private final Map<Id<Vehicle>, Double> vehiclesToDepartureTimes = new HashMap<>();
    private final Map<Id<Vehicle>, Double> vehiclesToArrivalTimes = new HashMap<>();
    List<PtTrip> trips = new ArrayList<>();

    private final String[] sheetsNames;

    public PtAnalyzer(PtAnalyzerConfigGroup configGroup, Network network, Scenario scenario) {
        this.configGroup = configGroup;
        this.network = network;
        this.sheetsNames = new String[]{configGroup.getTripsSheetName(), configGroup.getVehiclesSheetName()};
        this.scenario = scenario;
    }


    @Override
    public void handleEvent(PersonArrivalEvent event) {
        // TODO Transport mode
        // event.getLegMode()
        // TODO Travel time with offset between departure & arrival
        this.agentArrivals.put(event.getPersonId(), event);
    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
        this.previousActivities.put(event.getPersonId(), event);
        if (event.getActType().endsWith(" interaction")) {
            return;
        }
        PtTrip trip = new PtTrip(event.getPersonId());
        this.currentPtTrips.put(event.getPersonId(), trip);
        trip.previousActivityEnd = event;
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (event.getActType().endsWith(" interaction")) {
            return;
        }
        PtTrip trip = currentPtTrips.get(event.getPersonId());
        trip.nextActivityStart = event;
        for(PtTrip.TripSegment segment : trip.segments) {
            if(segment.mode.equals(this.configGroup.getMode())){
                trips.add(trip);
                return;
            }
        }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        this.agentDepartures.put(event.getPersonId(), event);
        this.agentsInVehicles.put(event.getPersonId(), new ArrayList<>());
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        PersonDepartureEvent personDeparture = this.agentDepartures.get(event.getPersonId());
        if(personDeparture != null) {
            PtTrip trip = this.currentPtTrips.get(event.getPersonId());
            if(trip != null) {
                trip.segments.add(new PtTrip.TripSegment(personDeparture.getLegMode(), event.getVehicleId(), event.getTime(), 0, event.getTime() - personDeparture.getTime()));
            }
            if(!this.agentsInVehicles.containsKey(event.getPersonId())) {
                this.agentsInVehicles.put(event.getPersonId(), new ArrayList<>());
            }
            this.agentsInVehicles.get(event.getPersonId()).add(new Tuple<>(new Tuple<>(event, 0.0), null));
            if(!this.vehiclesToPassengers.containsKey(event.getVehicleId())) {
                this.vehiclesToPassengers.put(event.getVehicleId(), new HashSet<>());
            }
            this.vehiclesToPassengers.get(event.getVehicleId()).add(event.getPersonId());
        }
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        PtTrip trip = currentPtTrips.get(event.getPersonId());
        if(trip !=null){
            PtTrip.TripSegment segment = trip.getLastSegment();
            if(segment != null && segment.vehicleId.equals(event.getVehicleId())) {
                segment.travelTime = event.getTime() - segment.travelTime;
            }
        }
        List<Tuple<Tuple<PersonEntersVehicleEvent, Double>, PersonLeavesVehicleEvent>> tuples = this.agentsInVehicles.getOrDefault(event.getPersonId(), new ArrayList<>());
        for(Tuple<Tuple<PersonEntersVehicleEvent, Double>, PersonLeavesVehicleEvent> tuple: tuples) {
            if(tuple.getFirst().getFirst().getVehicleId().equals(event.getVehicleId()) && tuple.getSecond() == null) {
                tuple.setSecond(event);
            }
        }
        if(this.vehiclesToPassengers.containsKey(event.getVehicleId())){
            this.vehiclesToPassengers.get(event.getVehicleId()).remove(event.getPersonId());
        }
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {

    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        if(this.scenario.getTransitVehicles().getVehicles().containsKey(event.getVehicleId())) {
            this.vehiclesDistances.put(event.getVehicleId(), this.network.getLinks().get(event.getLinkId()).getLength() + this.vehiclesDistances.getOrDefault(event.getVehicleId(), 0.0));
        }
        for(Id<Person> personId : this.vehiclesToPassengers.getOrDefault(event.getVehicleId(), new HashSet<>())) {
            PtTrip trip = this.currentPtTrips.get(personId);
            if (trip != null) {
                PtTrip.TripSegment segment = this.currentPtTrips.get(personId).getLastSegment();
                if(segment != null && segment.vehicleId.equals(event.getVehicleId())) {
                    segment.distance += network.getLinks().get(event.getLinkId()).getLength();
                }
            }
            for(Tuple<Tuple<PersonEntersVehicleEvent,Double>, PersonLeavesVehicleEvent> inVehicle : this.agentsInVehicles.get(personId)) {
                if(inVehicle.getFirst().getFirst().getVehicleId().equals(event.getVehicleId())) {
                    inVehicle.getFirst().setSecond(inVehicle.getFirst().getSecond() + network.getLinks().get(event.getLinkId()).getLength());
                }
            }
        }
    }

    @Override
    public String[] getSheetsNames() {
        return this.sheetsNames;
    }

    @Override
    public void fillSheets(List<Sheet> sheets) {
        assert sheets.size() >= sheetsNames.length;
        Sheet tripsSheet = sheets.get(0);
        assert tripsSheet.getSheetName().equals(this.sheetsNames[0]);
        Row row = tripsSheet.createRow(0);

        for (int i = 0; i< TRIPS_HEADERS.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(TRIPS_HEADERS[i]);
        }
        int rowCounter = 1;
        for(int i=0; i<trips.size(); i++) {
            for (int j=0; j< trips.get(i).segments.size(); j++) {
                row = tripsSheet.createRow(rowCounter);
                rowCounter++;
                //"personID", "purpose", "inVehicleTime", "totalWaitingTime", "accessTime", "egressTime", "transferTime", "segmentIndex", "mode", "vehicleId", "pt_line", "pt_mode", "segmentAccessTime", "segmentEgressTime", "waitingTime", "travelDuration", "travelDistance"
                Cell cell = row.createCell(0);
                cell.setCellValue(trips.get(i).personId.toString());
                cell = row.createCell(1);
                cell.setCellValue(trips.get(i).getPurpose().replaceAll("[0-9]|_", ""));
                cell = row.createCell(2);
                cell.setCellValue(trips.get(i).getInVehicleTime());
                cell = row.createCell(3);
                cell.setCellValue(trips.get(i).getTotalWaitingTime());
                cell = row.createCell(4);
                cell.setCellValue(trips.get(i).getAccessTime());
                cell = row.createCell(5);
                cell.setCellValue(trips.get(i).getEgressTime());
                cell = row.createCell(6);
                cell.setCellValue(trips.get(i).getTransferTime());
                cell = row.createCell(7);
                cell.setCellValue(j);
                cell = row.createCell(8);
                cell.setCellValue(trips.get(i).segments.get(j).mode);
                cell = row.createCell(9);
                Cell ptLineCell = row.createCell(10);
                Cell ptModeCell = row.createCell(11);
                Id<Vehicle> vehicleId = trips.get(i).segments.get(j).vehicleId;
                if(vehicleId != null) {
                    cell.setCellValue(trips.get(i).segments.get(j).vehicleId.toString());
                    putTransitLineInfoInCell(vehicleId, ptLineCell);
                    ptModeCell.setCellValue(this.scenario.getTransitSchedule().getTransitLines().get(this.vehiclesToTransitLines.get(vehicleId)).getRoutes().get(this.vehiclesToTransitRoutes.get(vehicleId)).getTransportMode());
                }
                cell = row.createCell(12);
                cell.setCellValue(trips.get(i).getSegmentAccessTime(j));
                cell = row.createCell(13);
                cell.setCellValue(trips.get(i).getSegmentEgressTime(j));
                cell = row.createCell(14);
                cell.setCellValue(trips.get(i).segments.get(j).waitingTime);
                cell = row.createCell(15);
                cell.setCellValue(trips.get(i).segments.get(j).travelTime);
                cell = row.createCell(16);
                cell.setCellValue(trips.get(i).segments.get(j).distance);
            }
        }
        Sheet vehiclesSheet = sheets.get(1);
        assert vehiclesSheet.getSheetName().equals(this.sheetsNames[1]);
        row = vehiclesSheet.createRow(0);
        for (int i = 0; i< VEHICLES_HEADERS.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(VEHICLES_HEADERS[i]);
        }
        rowCounter = 1;
        for(Id<Vehicle> vehicleId : this.scenario.getTransitVehicles().getVehicles().keySet()) {
            row = vehiclesSheet.createRow(rowCounter);
            Cell cell = row.createCell(0);
            cell.setCellValue(vehicleId.toString());
            cell = row.createCell(1);
            putTransitLineInfoInCell(vehicleId, cell);
            cell = row.createCell(2);
            cell.setCellValue(this.scenario.getTransitSchedule().getTransitLines().get(this.vehiclesToTransitLines.get(vehicleId)).getRoutes().get(this.vehiclesToTransitRoutes.get(vehicleId)).getTransportMode());
            cell = row.createCell(3);
            cell.setCellValue(this.vehiclesToDepartureTimes.get(vehicleId));
            cell = row.createCell(4);
            cell.setCellValue(this.vehiclesToArrivalTimes.get(vehicleId));
            cell = row.createCell(5);
            cell.setCellValue(this.vehiclesDistances.getOrDefault(vehicleId, 0.0));
            rowCounter++;

        }
    }

    private void putTransitLineInfoInCell(Id<Vehicle> vehicleId, Cell cell) {
        TransitLine transitLine = this.scenario.getTransitSchedule().getTransitLines().get(this.vehiclesToTransitLines.get(vehicleId));
        Object gtfsRouteShortName = transitLine.getAttributes().getAttribute("gtfs_route_short_name");
        if(gtfsRouteShortName != null) {
            cell.setCellValue(gtfsRouteShortName.toString());
        }
        else {
            cell.setCellValue(transitLine.getId().toString());
        }
    }

    @Override
    public void handleEvent(TeleportationArrivalEvent event) {
        double time = event.getTime() - this.agentDepartures.get(event.getPersonId()).getTime();
        this.currentPtTrips.get(event.getPersonId()).segments.add(new PtTrip.TripSegment(event.getMode(), null, time, event.getDistance(), 0));
    }

    @Override
    public void handleEvent(TransitDriverStartsEvent event) {
        this.vehiclesToTransitLines.put(event.getVehicleId(), event.getTransitLineId());
        this.vehiclesToTransitRoutes.put(event.getVehicleId(), event.getTransitRouteId());

    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        if(this.scenario.getTransitVehicles().getVehicles().containsKey(event.getVehicleId())) {
            this.vehiclesToDepartureTimes.put(event.getVehicleId(), event.getTime());
        }
    }

    @Override
    public void handleEvent(VehicleLeavesTrafficEvent event) {
        if(this.scenario.getTransitVehicles().getVehicles().containsKey(event.getVehicleId())) {
            this.vehiclesToArrivalTimes.put(event.getVehicleId(), event.getTime());
        }
    }
}
