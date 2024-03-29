package org.matsim.contrib.cba.analyzers.privateVehiclesAnalysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.population.Person;
import org.matsim.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.List;

final class PrivateVehicleTrip {
    static final class TripSegment {
        final Id<Vehicle> vehicleId;
        double travelTime;
        double distance;
        final String mode;
        final double waitingTime;

        TripSegment(String mode, Id<Vehicle> vehicleId, double travelTime, double distance, double waitingTime) {
            this.vehicleId = vehicleId;
            this.travelTime = travelTime;
            this.distance = distance;
            this.mode = mode;
            this.waitingTime = waitingTime;
        }
    }

    Id<Person> personId;
    List<TripSegment> segments;
    ActivityEndEvent previousActivityEnd;
    ActivityStartEvent nextActivityStart;

    PrivateVehicleTrip(Id<Person> personId) {
        this.personId = personId;
        this.segments = new ArrayList<>();
    }

    public TripSegment getLastSegment() {
        if(this.segments.size() > 0) {
            return this.segments.get(this.segments.size()-1);
        }
        return null;
    }

    public String getPurpose() {
        return this.previousActivityEnd.getActType() + " -- " + nextActivityStart.getActType();
    }

    public double getAccessTime() {
        if(this.segments.size() > 0 && this.segments.get(0).mode.equals("walk")){
            return this.segments.get(0).travelTime;
        }
        return 0.0;
    }
    public double getEgressTime() {
        if(this.segments.size() > 0 && this.segments.get(this.segments.size()-1).mode.equals("walk")) {
            return this.segments.get(this.segments.size()-1).travelTime;
        }
        return 0.0;
    }

    public double getInvehicleTime() {
        double result=0;
        for(TripSegment segment: this.segments) {
            if(segment.mode.equals("car")) {
                result+=segment.travelTime;
            }
        }
        return result;
    }

    public double getTransferTime(){
        double transferTime=0;
        for(int i=1; i<segments.size()-1; i++) {
            if(this.segments.get(i).mode.equals("walk")){
                transferTime+=this.segments.get(i).travelTime;
            }
        }
        return transferTime;
    }
}
