import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { RideService } from 'src/app/core/http/ride/ride.service';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import { SocketService } from 'src/app/core/socket/socket.service';
import { Session } from 'src/app/shared/models/session.model';

@Component({
  selector: 'app-split-fare-wait',
  templateUrl: './split-fare-wait.component.html',
  styleUrls: ['./split-fare-wait.component.scss']
})
export class SplitFareWaitComponent implements OnInit {
  showSelf: boolean = true;
  showErrorModal: boolean = false;
  errorTitle: string = ''
  errorMessage: string = '';

  constructor(
    private passengerService: PassengerService,
    private rideService: RideService,
    private socketService: SocketService,
    private authenticationService: AuthenticationService,
    ) {
    this.subscribeToErrorMessages();
  }

  ngOnInit(): void {
  }

  confirmRide(): void {
    if (this.ride)
      this.rideService.confirmRide(this.ride.id)
      .then(res => {
        window.location.href="/";
      });
  }

  rejectRide(): void {
    if (this.ride)
      this.rideService.rejectRide(this.ride.id)
      .then(res => {
        window.location.href="/";
      });
  }

  subscribeToErrorMessages() {
    const session: Session | null = this.authenticationService.getSession();
    if (session) {
      this.socketService.stompClient.subscribe(`/user/${ session.username }/private/ride/error`, (message: any) => {
        let messageData = JSON.parse(message.body);
        if (messageData.type === 'RIDE_UPDATE') {
          this.showSelf = false;
          this.showErrorModal = true;
          this.errorTitle = 'Sorry';
          this.errorMessage = messageData.content;
        }
      });
    }
  }

  get ride() {
    return this.passengerService.getCurrentRide();
  }

  closeErrorModal () {
    this.showErrorModal = false;
    window.location.href="/";
  }

}
