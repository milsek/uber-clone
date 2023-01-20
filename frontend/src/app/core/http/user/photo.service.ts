import { Injectable } from '@angular/core';
import axios from 'axios';
import { Observable } from 'rxjs';
import { AuthenticationService } from '../../authentication/authentication.service';

@Injectable({
  providedIn: 'root',
})
export class PhotoService {
  constructor(private authenticationService: AuthenticationService) {}

  storeImage(file: File): Promise<any> {
    const formData = new FormData();
    formData.append('file', file);
    return axios.post(`/api/image/save`, formData, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`,
      },
    });
  }

  loadImage(photoName: string): Promise<any> {
    return axios.get(`/api/image/load/${photoName}`);
  }
}
