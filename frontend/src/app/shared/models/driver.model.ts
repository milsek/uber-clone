import { Vehicle } from './vehicle.model';

export interface Driver {
  username: string;
  name: string;
  surname: string;
  phoneNumber: string;
  city: string;
  profilePicture: string;
  vehicle: Vehicle;
  distanceTravelled: number;
  ridesCompleted: number;
  totalRatingSum: number;
  numberOfReviews: number;
  active: boolean;
  email?: string;
  accountType?: string;
  userImage: string;
}

export interface DriverSimple {
  username: string;
  name: string;
  surname: string;
  phoneNumber: string;
  profilePicture: string;
  vehicle: Vehicle;
  totalRatingSum: number;
  numberOfReviews: number;
}

export interface DriverNewData {
  username: string;
  name: string;
  surname: string;
  phoneNumber: string;
  city: string;
  profilePicture: string;
  vehicleType: string;
  babySeat: boolean;
  petsAllowed: boolean;
  make: string;
  model: string;
  colour: string;
  licensePlateNumber: string;
  userImage: string;
}
