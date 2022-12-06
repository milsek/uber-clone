import { VehicleType } from "./vehicle-type.model";

export interface Vehicle {
    babySeat: boolean,
    petsAllowed: boolean,
    vehicleType: VehicleType,
    make: string,
    model: string,
    colour: string,
    licensePlateNumber: string,
}
