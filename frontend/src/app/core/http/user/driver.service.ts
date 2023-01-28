import { Injectable } from '@angular/core';
import axios, { AxiosResponse } from 'axios';
import { DriverRegistrationData } from 'src/app/shared/models/data-transfer-interfaces/registration.model';
import { Driver, DriverNewData, DriverSearchResult } from 'src/app/shared/models/driver.model';
import { DriverCurrentRides, PassengerRide } from 'src/app/shared/models/ride.model';
import { AuthenticationService } from '../../authentication/authentication.service';
import { DriverRideRejectionRequest } from 'src/app/shared/models/drver-ride-rejection-request.model';

@Injectable({
  providedIn: 'root',
})
export class DriverService {
  private rides: DriverCurrentRides | null =
    null;

  constructor(private authenticationService: AuthenticationService) {}

  getDriverByUsername(username: string): Promise<AxiosResponse<Driver>> {
    return axios.get(`/api/drivers/${username}`, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`,
      },
    });
  }

  fetchRides = async (): Promise<void> => {
    await axios
      .get(`/api/drivers/current-rides`, {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      })
      .then((res) => {
        if (res.data) this.rides = res.data;
      })
      .catch((err) => {
        this.rides = null;
      });
  };

  getCurrentRides = (): DriverCurrentRides | null => {
    return this.rides;
  };

  fetchReviews = (username: string, page: number, amount: number) => {
    return axios
      .get(`/api/drivers/reviews?username=${username}&page=${page}&amount=${amount}`, {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      });
  }

  async getDriverActivity(): Promise<boolean> {
    const activity: boolean = await axios
      .get(`/api/drivers/activity`, {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      })
      .then((res) => {
        return res.data;
      });
    return activity;
  }

  async register(data: DriverRegistrationData): Promise<void> {
    await axios
      .post(`/api/drivers`, data, {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      })
      .then((res) => {
        return res.data;
      });
  }

  toggleActivity(): void {
    axios.patch(
      `/api/drivers/activity`,
      {},
      {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      }
    );
  }

  getUpdateRequests(): Promise<AxiosResponse<DriverNewData[]>> {
    return axios.get(`/api/preupdate/all`, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`,
      },
    });
  }

  getRideRejectionRequests(): Promise<AxiosResponse<DriverRideRejectionRequest[]>> {
    return axios.get(`/api/rides/rejection-requests`, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`,
      },
    });
  }

  sendRideRejectionRequestVerdict(
    rideId: number,
    accepted: boolean
  ): Promise<AxiosResponse<boolean>> {
    return axios.patch(
      `/api/rides/driver-rejection-verdict`,
      {
        rideId,
        accepted,
      },
      {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      }
    );
  }

  getDrivers(
    name: string,
    surname: string,
    username: string,
    page: number
  ): Promise<AxiosResponse<DriverSearchResult>> {
    return axios.post(
      `/api/drivers/search`,
      { name: name, surname: surname, username: username, page: page },
      {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      }
    );
  }

  getRides(
    page: number,
    amount: number,
    sortBy: string,
    username: string
  ): Promise<AxiosResponse<{ totalElements: number, content: PassengerRide[] }>> {
    return axios.get(
      `/api/rides/driver-history?page=${page}&amount=${amount}&sortBy=${sortBy}&username=${username}`,
      {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      }
    );
  }

  getRideDetails(rideId: number): Promise<any> {
    return axios.get(
      `/api/rides/detailed-ride-history-driver?rideId=${rideId}`,
      {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      }
    );
  }
}
