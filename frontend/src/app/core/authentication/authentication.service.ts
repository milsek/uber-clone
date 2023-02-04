import { Injectable } from '@angular/core';
import axios, { AxiosResponse } from 'axios';
import { Session } from 'src/app/shared/models/session.model';

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

        if (!localStorage.getItem('token')){
          localStorage.setItem('token',localStorage.getItem('token2')!);
        }
        localStorage.removeItem('token2');

        if (reload) window.location.href = '/';
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

  async resetPasword(email: String): Promise<AxiosResponse<boolean>> {
    return axios
      .post('http://localhost:8080/api/auth/reset-password', {
        email
      });
  }

  confirmReset(token: String, password: String): Promise<AxiosResponse<boolean>> {
    return axios
      .post('http://localhost:8080/api/auth/confirm-password-reset', {
        token: token,
        newPassword: password,
      })
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
          this.toggleDriverActivityIfNeeded(res.data['accessToken']);
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
  ) : Promise<AxiosResponse<boolean>> {
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
    return axios.post('http://localhost:8080/api/preupdate/sendUpdateRequest', formData);
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

  async toggleDriverActivityIfNeeded(token: string): Promise<void> {
    axios
    .get(`/api/drivers/activity`, {
      headers: {
        Authorization: `Bearer ${ token }`,
      },
    })
    .then((res) => {
      if (!res.data) {
        axios.patch(
          `/api/drivers/activity`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
      }
    });
  }
}
