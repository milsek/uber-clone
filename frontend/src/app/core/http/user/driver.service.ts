import { Injectable } from '@angular/core';
import axios from 'axios';
import { AuthenticationService } from '../../authentication/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class DriverService {

  constructor(private authenticationService: AuthenticationService) { }

  getDriverByUsername(username: string): Promise<any> {
    return axios.get(`/api/drivers/${username}`);
  }

  async getDriverActivity(): Promise<boolean> {
    const activity: boolean = await axios.get(`/api/drivers/activity`, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    }).then((res => {
      return res.data;
    }));
    return activity;
  }

  toggleActivity(): void {
    axios.patch(`/api/drivers/activity`, {},
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }
}
