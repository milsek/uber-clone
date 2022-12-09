import { Injectable } from '@angular/core';
import axios from 'axios';
import { Session } from 'src/app/shared/models/session.model';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  constructor() { 
    this.whoami();
  }

  getToken() {
    const token = localStorage.getItem('token');
    return token;
  }

  whoami(): void {
    axios.get(`/api/users/whoami`, 
    {
      headers: {
        Authorization: `Bearer ${this.getToken()}`
      }
    })
    .then((response) => {
      this.saveSession(response.data);
    })
    .catch((err) => {
      this.logout();
    });
  }

  getSession() : Session | null {
    const sessionString: string | null = localStorage.getItem('session');
    if (sessionString)
      return JSON.parse(sessionString);
    return null;
  }

  getAccountType(): string {
    const session: Session | null = this.getSession();
    if (session){
      return session.accountType;
    }
    return "anonymous";
  }

  logout(): void {
    localStorage.removeItem('session');
    localStorage.removeItem('token');
  }
  
  saveSession(session: Session): void {
    localStorage.setItem('session', JSON.stringify(session));
  }

}
