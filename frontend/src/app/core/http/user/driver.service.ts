import { Injectable } from '@angular/core';
import axios from 'axios';
import { DriverRide } from 'src/app/shared/models/ride.model';
import { AuthenticationService } from '../../authentication/authentication.service';

@Injectable({
  providedIn: 'root',
})
export class DriverService {
  private rides: { currentRide: DriverRide; nextRide: DriverRide } | null =
    null;

  constructor(private authenticationService: AuthenticationService) {}

  getDriverByUsername(username: string): Promise<any> {
    return axios.get(`/api/drivers/${username}`, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`,
      },
    });
  }

  fetchRides = async () => {
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

  getCurrentRides = () => {
    return this.rides;
  };

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

  async register(data: any): Promise<any> {
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

  getUpdateRequests(): Promise<any> {
    return axios.get(`/api/preupdate/all`, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`,
      },
    });
  }

  getRideRejectionRequests(): Promise<any> {
    return axios.get(`/api/rides/rejection-requests`, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`,
      },
    });
  }

  sendRideRejectionRequestVerdict(
    rideId: number,
    accepted: boolean
  ): Promise<any> {
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
  ): Promise<any> {
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
}
