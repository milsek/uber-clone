import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { DriverService } from 'src/app/core/http/user/driver.service';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import { RideSimple } from 'src/app/shared/models/ride.model';
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

  constructor(private driverService: DriverService, private passengerService: PassengerService, private authenticationService: AuthenticationService) { }

  async ngOnInit(): Promise<void> {
    if (this.authenticationService.getAccountType() === 'driver')
      this.isActive = await this.driverService.getDriverActivity();
    if (this.authenticationService.getAccountType() === 'passenger')
      await this.passengerService.fetchCurrentRide();
    this.isMainLoaded = true;
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
}
