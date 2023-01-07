import { Coordinates } from "./coordinates.model";

export interface Route {
  id: number,
  waypoints: Coordinates[],
  coordinates: Coordinates[],
}