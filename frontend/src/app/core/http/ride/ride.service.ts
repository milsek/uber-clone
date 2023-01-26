import { Injectable } from '@angular/core';
import axios from 'axios';
import { Coordinates } from 'src/app/shared/models/coordinates.model';
import { ReviewData } from 'src/app/shared/models/data-transfer-interfaces/review-data.model';
import { AuthenticationService } from '../../authentication/authentication.service';

interface RideOrderData {
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
  } | null,
  usersToPay: string[]
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

  orderBasicRide(orderData: RideOrderData): Promise<any> {
    return axios.post(`/api/rides/basic`, 
    orderData,
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

  orderSplitFareRide(orderData: RideOrderData): Promise<any> {
    return axios.post(`/api/rides/split-fare`, 
    orderData,
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

  confirmRide(rideId: number): Promise<any> {
    return axios.patch(`/api/rides/confirm`, 
    {
      rideId
    },
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

  rejectRide(rideId: number): Promise<any> {
    return axios.patch(`/api/rides/reject`, 
    {
      rideId
    },
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

  driverRejectRide(rideId: number, reason: string): Promise<any> {
    return axios.patch(`/api/rides/driver-rejection`, 
    {
      rideId,
      reason
    },
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

  beginRide(rideId: number): Promise<any> {
    return axios.patch(`/api/rides/begin`, 
    {
      rideId,
    },
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

  completeRide(rideId: number): Promise<any> {
    return axios.patch(`/api/rides/complete`, 
    {
      rideId,
    },
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

  sendReview(data: ReviewData): Promise<any> {
    return axios.post(`/api/rides/reviews`, 
    data,
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

}
