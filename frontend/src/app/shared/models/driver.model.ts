import { Vehicle } from "./vehicle.model";

export interface Driver {
  username: string,
  name: string,
  surname: string,
  phoneNumber: string,
  city: String,
  profilePicture: string,
  vehicle: Vehicle,
  distanceTravelled: number,
  ridesCompleted: number,
  totalRatingSum: number,
  numberOfReviews: number,
  active: boolean,
  email?: string,
  accountType?: string,
}

export interface DriverSimple {
  username: string,
  name: string,
  surname: string,
  phoneNumber: string,
  profilePicture: string,
  vehicle: Vehicle,
  totalRatingSum: number,
  numberOfReviews: number,
}
