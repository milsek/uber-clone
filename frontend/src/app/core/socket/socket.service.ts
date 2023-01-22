import { Injectable } from '@angular/core';
import { CompatClient, Stomp } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { Session } from 'src/app/shared/models/session.model';
import { AuthenticationService } from '../authentication/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class SocketService {
  stompClient!: CompatClient;

  constructor(private authenticationService: AuthenticationService) {}

  initWS(): Promise<void> {
    const session: Session | null = this.authenticationService.getSession();
    if (!session) return new Promise((resolve) => { resolve() });
    if (session.accountType !== 'anonymous')
      return new Promise((resolve) => {
        this.stompClient = Stomp.over(new SockJS('http://localhost:8080/ws'));
        this.stompClient.connect({}, () => { 
          this.subscribeToRideUpdates(session);
          resolve();
        });
      });
    return new Promise((resolve) => { resolve() });
  }

  subscribeToRideUpdates(session: Session): void {
    let addr: string = '';
    if (session.accountType === 'passenger' || session.accountType === 'driver') {
      addr = `/user/${ session.username }/private/ride/refresh`;
    }
    this.stompClient.subscribe(addr, (message: any) => {
      let messageData = JSON.parse(message.body);
      if (messageData.type === 'RIDE_UPDATE' && messageData.content === 'REFRESH')
        window.location.href="/";
    });
  }

}
