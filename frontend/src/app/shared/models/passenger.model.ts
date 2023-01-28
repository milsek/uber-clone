import { Driver } from "./driver.model";
import { Note } from "./note.model";

export interface Passenger {
  username: string;
  name: string;
  surname: string;
  phoneNumber: string;
  city: string;
  profilePicture: string;
  distanceTravelled: number;
  ridesCompleted: number;
  tokenBalance: number;
  email?: string;
  accountType?: string;
  accountStatus?: string;
  userImage: string;
  notes: Array<Note>;
}

export interface PassengerSimple {
  username: string;
  name: string;
  surname: string;
  profilePicture: string;
}

export interface PassengerSearchResult {
  passengers: Driver[],
  numberOfPassengers: number
}
