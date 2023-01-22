import { DriverSimple } from "./driver.model";
import { PassengerSimple } from "./passenger.model";
import { Route } from "./route.model";

export interface RideSimple {
  id: number,
  distance: number,
  expectedTime: number,
  price: number,
  driver: DriverSimple,
  route: Route,
  createdAt: Date,
  allConfirmed: boolean,
  passengerConfirmed: boolean,
  status: string,
}

export interface DriverRide {
  id: number,
  distance: number,
  expectedTime: number,
  price: number,
  passengers: PassengerSimple[],
  route: Route,
  createdAt: Date,
  status: string,
  startAddress: string,
  endAddress: string
} 
