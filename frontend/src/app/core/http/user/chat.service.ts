import { Injectable } from '@angular/core';
import axios, { AxiosResponse } from 'axios';
import { Chat } from 'src/app/shared/models/chat.model';
import { AuthenticationService } from '../../authentication/authentication.service';

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  constructor(private authenticationService: AuthenticationService) {}

  getAllChats(): Promise<AxiosResponse<Array<Chat>>> {
    return axios.get('api/chat/all');
  }

  getUserChat(username: string): Promise<AxiosResponse<Chat>> {
    return axios.get(`/api/chat/${username}`);
  }

  updateChat(username: string, type: string): void {
    axios
      .post(
        `/api/chat/updateRead`,
        { username: username, type: type },
        {
          headers: {
            Authorization: `Bearer ${this.authenticationService.getToken()}`,
          },
        }
      )
      .then((res) => {
        return res.data;
      });
  }
}
