import { Injectable } from '@angular/core';
import axios, { AxiosResponse } from 'axios';
import { Coordinates } from 'src/app/shared/models/coordinates.model';
import { ReviewData } from 'src/app/shared/models/data-transfer-interfaces/review-data.model';
import { RideOrderData } from 'src/app/shared/models/data-transfer-interfaces/ride-oder.model';
import { RideSimple } from 'src/app/shared/models/ride.model';
import { VehicleType } from 'src/app/shared/models/vehicle-type.model';
import { AuthenticationService } from '../../authentication/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class RideService {

  constructor(private authenticationService: AuthenticationService) { }

  getVehicleTypes(): Promise<VehicleType[]> {
    return axios.get(`/api/vehicles/types`).then((res => {
      return res.data;
    }));
  }

  orderBasicRide(orderData: RideOrderData): Promise<AxiosResponse<RideSimple>> {
    return axios.post(`/api/rides/basic`, 
    orderData,
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

  orderSplitFareRide(orderData: RideOrderData): Promise<AxiosResponse<boolean>> {
    return axios.post(`/api/rides/split-fare`, 
    orderData,
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

  confirmRide(rideId: number): Promise<AxiosResponse<boolean>> {
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

  rejectRide(rideId: number): Promise<AxiosResponse<boolean>> {
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

  driverRejectRide(rideId: number, reason: string): Promise<AxiosResponse<boolean>> {
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

  beginRide(rideId: number): Promise<AxiosResponse<boolean>> {
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

  completeRide(rideId: number): Promise<AxiosResponse<boolean>> {
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

  sendReview(data: ReviewData): Promise<AxiosResponse<boolean>> {
    return axios.post(`/api/rides/reviews`, 
    data,
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

  reportInconsistency(rideId: number): Promise<AxiosResponse<boolean>> {
    return axios.patch(`/api/rides/inconsistency`, 
    {
      rideId
    },
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }

}
