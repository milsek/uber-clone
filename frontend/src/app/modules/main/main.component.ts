import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { DriverService } from 'src/app/core/http/user/driver.service';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import { SocketService } from 'src/app/core/socket/socket.service';
import { DriverRide, RideSimple } from 'src/app/shared/models/ride.model';
import { Session } from 'src/app/shared/models/session.model';
import { MapComponent } from './map/map.component';

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
})
export class MainComponent implements OnInit {
  @ViewChild(MapComponent, { static: true }) mapComponent!: MapComponent;

  // Driver
  isActive: boolean | null = null;

  isMainLoaded: boolean = false;

  showErrorModal: boolean = false;
  errorTitle: string = ''
  errorMessage: string = '';

  constructor(
    private driverService: DriverService,
    private passengerService: PassengerService, 
    private authenticationService: AuthenticationService,
    private socketService: SocketService
    ) { }

  async ngOnInit(): Promise<void> {
    if (this.authenticationService.getAccountType() === 'driver') {
      this.isActive = await this.driverService.getDriverActivity();
      await this.driverService.fetchRides();
      this.subscribeDriverToRideMessages();
      this.subscribeDriverToOvertimeMessage();
    }
    if (this.authenticationService.getAccountType() === 'passenger') {
      await this.passengerService.fetchCurrentRide();
      this.subscribePassengerToRideMessages();
    }
    this.isMainLoaded = true;
  }
  
  subscribePassengerToRideMessages() {
    const session: Session | null = this.authenticationService.getSession();
    if (session) {
      this.socketService.stompClient.subscribe(`/user/${ session.username }/private/passenger/ride`, (message: any) => {
        let messageData = JSON.parse(message.body);
        if (messageData.type === 'RIDE_ERROR') {
          this.showErrorModal = true;
          this.errorTitle = 'Sorry';
          this.errorMessage = messageData.content;
        }
      });
    }
  }

  subscribeDriverToRideMessages() {
    const session: Session | null = this.authenticationService.getSession();
    if (session) {
      this.socketService.stompClient.subscribe(`/user/${ session.username }/private/driver/ride`, (message: any) => {
        let messageData = JSON.parse(message.body);
        if (messageData.type === 'RIDE_ERROR') {
          this.showErrorModal = true;
          this.errorTitle = 'Sorry';
          this.errorMessage = messageData.content;
        }
        else if (messageData.type === 'RIDE_UPDATE') {
          this.showErrorModal = true;
          this.errorTitle = 'Update';
          this.errorMessage = messageData.content;
        }
      });
    }
  }

  subscribeDriverToOvertimeMessage(): void {
    const session: Session | null = this.authenticationService.getSession();
    if (session && session.accountType === 'driver') {
      this.socketService.stompClient.subscribe(`/user/${ session.username }/private/driver/overtime`,
      (message: any) => {
        let messageData = JSON.parse(message.body);
        if (messageData.type === 'NOTIFICATION') {
          this.showErrorModal = true;
          this.errorTitle = 'Thank you';
          this.errorMessage = messageData.content;
        }
      });
    }
  }

  closeErrorModal () {
    this.showErrorModal = false;
    window.location.href="/";
  }

  addNewStop(event: string): void {
    this.mapComponent.addNewWaypoint(event);
  }

  removeStop(event: number): void {
    this.mapComponent.removeWaypoint(event);
  }

  clearMarkers(): void {
    this.mapComponent.clearMarkers();
  }

  get accountType(): string {
    return this.authenticationService.getAccountType();
  }

  get route(): any {
    return this.mapComponent.chosenRoute;
  }

  get alternativeRoute(): any {
    return this.mapComponent.alternativeRoute;
  }

  get waypoints(): any {
    return this.mapComponent.waypoints;
  }

  get currentRide(): RideSimple | null {
    return this.passengerService.getCurrentRide();
  }

  get currentDriverRide(): DriverRide | undefined {
    return this.driverService.getCurrentRides()?.currentRide;
  }
}
