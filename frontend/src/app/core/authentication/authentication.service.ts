import { Injectable } from '@angular/core';

interface Session {
  username: string;
  name: string;
  surname: string;
  profilePicture: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  constructor() { }

  getToken() {
    const token = localStorage.getItem('token');
    return token;
  }

  getSession() : Session | null {
    const sessionString: string | null = localStorage.getItem('session');
    if (sessionString)
      return JSON.parse(sessionString);
    return null;
  }

}
