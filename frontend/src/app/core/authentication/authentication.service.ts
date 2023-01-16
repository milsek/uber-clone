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
      let reload: boolean = false;
      if (!this.getSession()) reload = true;
      this.saveSession(response.data);
      if (reload) window.location.href="/";
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
    if (session) {
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

  async resetPasword(email: String) : Promise<boolean>{
    if (email) {
      var formData =
      {
        "email": email,
      }
      await axios
      .post("http://localhost:8080/api/auth/reset-password", formData)
      .then((resp) => {
        return true;
      })
      .catch((err) => {
        return false;
      });      
    }
    return false;
  }

  async confirmReset(token: String,password: String) : Promise<boolean>{
    if (password) {
      
      var formData =
      {
        "token" : token,
        "newPassword" : password
      }
      await axios
      .post("http://localhost:8080/api/auth/confirm-password-reset", formData)
      .then((res) => {
        return true;
      })
      .catch((err) => {
        return false;
      });      
    }
    return false;
  }

  async login(username: string, password: string): Promise<boolean> {
    
    var formData =
    {
      "username": username,
      "password": password
    }
    
    const successfulLogin = await axios
      .post("http://localhost:8080/api/auth/custom-login", formData)
      .then((res) => {
        if (res.data) {
          window.localStorage.setItem("token", res.data["accessToken"]);
          axios.defaults.headers.common['Authorization'] = `Bearer ${localStorage.getItem('token')}`;
          return true;
        }
        else{
          return false;
        }
      })
      .catch((err) => {
        console.log(err)
        return false;
      });      
    return successfulLogin;
  }

}
