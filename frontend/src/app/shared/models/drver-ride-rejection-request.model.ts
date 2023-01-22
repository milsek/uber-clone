import { DriverNano } from "./driver.model";

export interface DriverRideRejectionRequest {
  id: number,
  reason: string,
  driver: DriverNano
}