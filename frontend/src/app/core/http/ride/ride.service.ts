import { Injectable } from '@angular/core';
import axios from 'axios';
import { Coordinates } from 'src/app/shared/models/coordinates.model';
import { AuthenticationService } from '../../authentication/authentication.service';

interface BasicOrderData {
  distance: number,
  expectedTime: number,
  babySeat: boolean,
  petFriendly: boolean,
  vehicleType: string,
  actualRoute: {
    coordinates: Coordinates[],
    waypoints: Coordinates[]
  },
  expectedRoute: {
    coordinates: Coordinates[],
    waypoints: Coordinates[]
  } | null
}

@Injectable({
  providedIn: 'root'
})
export class RideService {

  constructor(private authenticationService: AuthenticationService) { }

  getVehicleTypes(): Promise<any> {
    return axios.get(`/api/vehicles/types`).then((res => {
      return res.data;
    }));
  }

  orderBasicRide(orderData: BasicOrderData): Promise<any> {
    return axios.post(`/api/rides/basic`, 
    orderData,
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    }).then((res => {
      return res.data;
    }));
  }

}
