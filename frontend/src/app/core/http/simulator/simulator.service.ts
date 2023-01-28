import { Injectable } from '@angular/core';
import axios, { AxiosResponse } from 'axios';
import { VehiclePosition } from 'src/app/shared/models/vehicle.model';

@Injectable({
  providedIn: 'root'
})
export class SimulatorService {

  constructor() { }

  getVehiclePositions(): Promise<AxiosResponse<VehiclePosition[]>> {
    return axios.get(`/api/vehicles/positions`);
  }

}
