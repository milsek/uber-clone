import { PassengerSimple } from "./passenger.model";

export interface DriverReview {
  driverRating: number;
  vehicleRating: number;
  comment: string;
  passenger: PassengerSimple;
}
