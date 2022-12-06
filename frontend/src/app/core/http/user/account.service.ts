import { Injectable } from '@angular/core';
import { AuthenticationService } from '../../authentication/authentication.service';
import axios from 'axios';

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  constructor(private authenticationService: AuthenticationService) { }

  getAccount(): Promise<any> {
    return axios.get(`/api/users/account`, 
    {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    });
  }
}
