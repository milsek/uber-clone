import { Coordinates } from "./coordinates.model";
import { VehicleType } from "./vehicle-type.model";

export interface Vehicle {
    id: number,
    babySeat: boolean,
    petsAllowed: boolean,
    vehicleType: VehicleType,
    make: string,
    model: string,
    colour: string,
    licensePlateNumber: string,
}

export interface VehiclePosition extends Vehicle {
    currentCoordinates: Coordinates,
    nextCoordinates: Coordinates,
    coordinatesChangedAt: string,
    expectedTripTime: number,
    rideActive: boolean
}
