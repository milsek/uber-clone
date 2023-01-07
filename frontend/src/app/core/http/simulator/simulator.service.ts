import { Injectable } from '@angular/core';
import axios from 'axios';

@Injectable({
  providedIn: 'root'
})
export class SimulatorService {

  constructor() { }

  getVehiclePositions(): Promise<any> {
    return axios.get(`/api/vehicles/positions`);
  }

}
