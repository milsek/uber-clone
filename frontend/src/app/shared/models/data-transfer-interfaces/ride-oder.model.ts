import { Coordinates } from "../coordinates.model"

export interface RideOrderData {
  distance: number;
  expectedTime: number;
  babySeat: boolean;
  petFriendly: boolean;
  vehicleType: string;
  route: {
    coordinates: Coordinates[];
    waypoints: Coordinates[];
  };
  usersToPay: string[];
  startAddress: string;
  destinationAddress: string;
  delayInMinutes: number;
}
