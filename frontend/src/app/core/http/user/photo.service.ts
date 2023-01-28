import { Injectable } from '@angular/core';
import axios, { AxiosResponse } from 'axios';
import { AuthenticationService } from '../../authentication/authentication.service';

@Injectable({
  providedIn: 'root',
})
export class PhotoService {
  constructor(private authenticationService: AuthenticationService) {}

  storeImage(file: File): Promise<AxiosResponse<string>> {
    const formData = new FormData();
    formData.append('file', file);
    return axios.post(`/api/image/save`, formData, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`,
      },
    });
  }

  loadImage(photoName: string): Promise<AxiosResponse<string>> {
    return axios.get(`/api/image/load/${photoName}`);
  }
}
