import { Injectable } from '@angular/core';
import { AuthenticationService } from '../../authentication/authentication.service';
import axios, { AxiosResponse } from 'axios';
import { Passenger } from 'src/app/shared/models/passenger.model';
import { Admin } from 'src/app/shared/models/admin.model';
import { Driver } from 'src/app/shared/models/driver.model';

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  constructor(private authenticationService: AuthenticationService) { }

  getAccount(): Promise<AxiosResponse<Passenger | Driver | Admin>> {
    return axios.get(`/api/users/account`, 
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }
}
