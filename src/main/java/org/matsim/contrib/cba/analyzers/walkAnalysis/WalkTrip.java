package org.matsim.contrib.cba.analyzers.walkAnalysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.TeleportationArrivalEvent;
import org.matsim.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.List;

final class WalkTrip {

    Id<Person> personId;
    ActivityEndEvent previousActivityEnd;
    ActivityStartEvent nextActivityStart;
    PersonDepartureEvent departureEvent;
    TeleportationArrivalEvent teleportationArrivalEvent;

    WalkTrip(Id<Person> personId) {
        this.personId = personId;
    }

    public String getPurpose() {
        return this.previousActivityEnd.getActType() + " -- " + nextActivityStart.getActType();
    }
}
