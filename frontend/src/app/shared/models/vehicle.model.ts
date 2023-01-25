import { Coordinates } from "./coordinates.model";
import { VehicleType } from "./vehicle-type.model";

export interface Vehicle {
    babySeat: boolean,
    petsAllowed: boolean,
    vehicleType: VehicleType,
    make: string,
    model: string,
    colour: string,
    licensePlateNumber: string,
    id?: number,
    currentCoordinates?: Coordinates,
    nextCoordinates?: Coordinates,
    coordinatesChangedAt?: string,
    expectedTripTime?: number,
    rideActive?: boolean
}
