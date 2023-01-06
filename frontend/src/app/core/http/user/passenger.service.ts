import { Injectable } from '@angular/core';
import axios from 'axios';
import { AuthenticationService } from '../../authentication/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class PassengerService {

  constructor(private authenticationService: AuthenticationService) { }

  getPassengerByUsername(username: string): Promise<any> {
    return axios.get(`/api/passenger/${username}`);
  }

  async addTokens(amount: number): Promise<void> {
    var tokenData = {
        "intent" : "CAPTURE",
        "purchase_units" : [
            {
                "amount" : {
                    "currency_code": "EUR",
                    "value" : amount
                }
            }
        ]
    };
    
    await axios.post(`/api/checkout`, tokenData, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`
      }
    }).then((resp => {
      window.location= (resp.data.links[1].href);
    }));
  }
}
