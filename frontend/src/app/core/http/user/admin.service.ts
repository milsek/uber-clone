import { Injectable } from '@angular/core';
import axios, { AxiosResponse } from 'axios';
import { Note } from 'src/app/shared/models/note.model';
import { AuthenticationService } from '../../authentication/authentication.service';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  constructor(private authenticationService: AuthenticationService) {}

  banUser(username: string): Promise<AxiosResponse<void>> {
    return axios.post(
      '/api/admin/ban-user',
      { username },
      {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      }
    );
  }
  unbanUser(username: string): Promise<AxiosResponse<void>> {
    return axios.post(
      '/api/admin/unban-user',
      { username },
      {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      }
    );
  }

  addNote(note: string, username: string): Promise<AxiosResponse<void>> {
    var formData = {
      content: note,
      username: username,
    };
    return axios.post('/api/admin/leave-note', formData, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`,
      },
    });
  }

  removeNote(note: Note, username: string): Promise<AxiosResponse<void>> {
    var formData = {
      content: note.content,
      username: username,
      admin: note.admin.username,
    };
    return axios.post('/api/admin/remove-note', formData, {
      headers: {
        Authorization: `Bearer ${this.authenticationService.getToken()}`,
      },
    });
  }
}
