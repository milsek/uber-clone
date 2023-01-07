import { DriverSimple } from "./driver.model";
import { Route } from "./route.model";

export interface RideSimple {
  id: number,
  distance: number,
  expectedTime: number,
  price: number,
  driver: DriverSimple,
  route: Route,
  createdAt: Date,
}
