import { Injectable } from '@angular/core';
import axios from 'axios';

@Injectable({
  providedIn: 'root'
})
export class DriverService {

  constructor() { }

  getDriverByUsername(username: string): Promise<any> {
    return axios.get(`/api/drivers/${username}`);
  }
}
