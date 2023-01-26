import { Injectable } from '@angular/core';
import { faCity } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';
import { Session } from 'src/app/shared/models/session.model';
import { VehicleType } from 'src/app/shared/models/vehicle-type.model';

@Injectable({
  providedIn: 'root',
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
    axios
      .get(`/api/users/whoami`, {
        headers: {
          Authorization: `Bearer ${this.getToken() || localStorage.getItem('token2')}`,
        },
      })
      .then((response) => {
        let reload: boolean = false;
        if (!this.getSession()) reload = true;
        this.saveSession(response.data);
        if (reload) window.location.href = '/';

        if(!localStorage.getItem('token')){
          localStorage.setItem('token',localStorage.getItem('token2')!);
        }
        localStorage.removeItem('token2');
      })
      .catch((err) => {
        this.logout();
      });
  }

  getSession(): Session | null {
    const sessionString: string | null = localStorage.getItem('session');
    if (sessionString) return JSON.parse(sessionString);
    return null;
  }

  getAccountType(): string {
    const session: Session | null = this.getSession();
    if (session) {
      return session.accountType;
    }
    return 'anonymous';
  }

  logout(): void {
    localStorage.removeItem('session');
    localStorage.removeItem('token');
  }

  saveSession(session: Session): void {
    localStorage.setItem('session', JSON.stringify(session));
  }

  async resetPasword(email: String): Promise<boolean> {
    if (email) {
      var formData = {
        email: email,
      };
      await axios
        .post('http://localhost:8080/api/auth/reset-password', formData)
        .then((resp) => {
          return true;
        })
        .catch((err) => {
          return false;
        });
    }
    return false;
  }

  async confirmReset(token: String, password: String): Promise<boolean> {
    if (password) {
      var formData = {
        token: token,
        newPassword: password,
      };
      await axios
        .post('http://localhost:8080/api/auth/confirm-password-reset', formData)
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
    const formData = {
      username: username,
      password: password,
    };

    const successfulLogin = await axios
      .post('http://localhost:8080/api/auth/custom-login', formData)
      .then((res) => {
        if (res.data) {
          window.localStorage.setItem('token', res.data['accessToken']);
          axios.defaults.headers.common[
            'Authorization'
          ] = `Bearer ${localStorage.getItem('token')}`;
          return true;
        } else {
          return false;
        }
      })
      .catch((err) => {
        console.log(err);
        return false;
      });
    return successfulLogin;
  }

  async updateUser(
    username: string,
    name: string,
    surname: string,
    phoneNumber: string,
    city: string,
    profilePicture: string
  ): Promise<boolean> {
    const formData = {
      username: username,
      name: name,
      surname: surname,
      phoneNumber: phoneNumber,
      city: city,
      profilePicture: profilePicture,
    };

    const successfulLogin = await axios
      .post('http://localhost:8080/api/users/update', formData)
      .then((resp) => {
        if (resp.data) {
          return true;
        } else {
          return false;
        }
      })
      .catch((err) => {
        return false;
      });
    return successfulLogin;
  }

  sendUpdateRequest(
    username: string,
    name: string,
    surname: string,
    phoneNumber: string,
    city: string,
    profilePicture: string,
    vehicleType: string,
    babySeat: boolean,
    petsAllowed: boolean,
    make: string,
    model: string,
    colour: string,
    licensePlateNumber: string
  ) {
    const formData = {
      username: username,
      name: name,
      surname: surname,
      phoneNumber: phoneNumber,
      city: city,
      profilePicture: profilePicture,
      vehicleType: vehicleType,
      babySeat: babySeat,
      petsAllowed: petsAllowed,
      make: make,
      model: model,
      colour: colour,
      licensePlateNumber: licensePlateNumber,
    };
    const successfulLogin = axios
      .post('http://localhost:8080/api/preupdate/sendUpdateRequest', formData)
      .then((resp) => {
        if (resp.data) {
          return true;
        } else {
          return false;
        }
      })
      .catch((err) => {
        console.log(err);
        return false;
      });
    return successfulLogin;
  }

  updateDriver(
    username: string,
    name: string,
    surname: string,
    phoneNumber: string,
    city: string,
    profilePicture: string,
    vehicleType: string,
    babySeat: boolean,
    petsAllowed: boolean,
    make: string,
    model: string,
    colour: string,
    licensePlateNumber: string
  ) {
    const formData = {
      username: username,
      name: name,
      surname: surname,
      phoneNumber: phoneNumber,
      city: city,
      profilePicture: profilePicture,
      vehicleType: vehicleType,
      babySeat: babySeat,
      petsAllowed: petsAllowed,
      make: make,
      model: model,
      colour: colour,
      licensePlateNumber: licensePlateNumber,
    };
    return axios.post('http://localhost:8080/api/drivers/update', formData);
  }

  cancelRequest(username: string) {
    return axios
      .post(
        `http://localhost:8080/api/preupdate/cancel`,
        {
          username,
        },
        {
          headers: {
            Authorization: `Bearer ${this.getToken()}`,
          },
        }
      );
  }
}
