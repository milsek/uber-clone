import { Component, Input, OnInit } from '@angular/core';
import { faChevronDown, faChevronLeft, faChevronRight, faChevronUp, faCircle, faFlagCheckered, faHandHoldingUsd, faRoute, faStop, faStopwatch, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import * as moment from 'moment';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { RideService } from 'src/app/core/http/ride/ride.service';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import { PhotoService } from 'src/app/core/http/user/photo.service';
import { RideSimple } from 'src/app/shared/models/ride.model';
import { Vehicle } from 'src/app/shared/models/vehicle.model';

@Component({
  selector: 'app-passenger-ride-details',
  templateUrl: './passenger-ride-details.component.html',
  styleUrls: ['./passenger-ride-details.component.css']
})
export class PassengerRideDetailsComponent implements OnInit {
  @Input() waypoints: any[] = [];

  faChevronRight: IconDefinition = faChevronRight;
  faChevronLeft: IconDefinition = faChevronLeft;
  faChevronUp: IconDefinition = faChevronUp;
  faChevronDown: IconDefinition = faChevronDown;
  faCircle: IconDefinition = faCircle;
  faStop: IconDefinition = faStop;
  faFlagCheckered: IconDefinition = faFlagCheckered;
  faStopwatch: IconDefinition = faStopwatch;
  faRoute: IconDefinition = faRoute;
  faHandHoldingUsd: IconDefinition = faHandHoldingUsd;

  accountType: string = this.authenticationService.getAccountType();
  isOpened: boolean = true;
  newStopQuery: string = '';

  coupeImg: string = 'assets/icons/car-coupe-gray.png';
  minivanImg: string = 'assets/icons/car-minivan-gray.png';
  stationImg: string = 'assets/icons/car-station-gray.png';

  constructor(
    private authenticationService: AuthenticationService,
    private passengerService: PassengerService,
    private rideService: RideService,
    private photoService: PhotoService
    ) { }

  ngOnInit(): void { 
    this.fetchDriverImage();
  }

  reportInconsistency(): void {
    if (this.ride)
      this.rideService.reportInconsistency(this.ride.id);
  }

  toggleOpened(): void {
    let inAnimation = 'slide-in';
    let outAnimation = 'slide-out';
    if (window.screen.width < 640) {
      inAnimation = 'slide-in-bottom';
      outAnimation = 'slide-out-bottom';
    }
    if (!this.isOpened) {
      this.isOpened = true;
      document.getElementById('order-menu')?.classList.remove(outAnimation);
      document.getElementById('order-menu')?.classList.add(inAnimation);
    }
    else {
      setTimeout(() => { this.isOpened = false; }, 250);
      document.getElementById('order-menu')?.classList.remove(inAnimation);
      document.getElementById('order-menu')?.classList.add(outAnimation);
    }
  }

  getIcon(i: number): IconDefinition {
    if (i === 0) return this.faCircle;
    else if (i === this.waypoints.length - 1) return this.faFlagCheckered;
    else return this.faStop;
  }

  get ride(): RideSimple | null {
    return this.passengerService.getCurrentRide();
  }
  
  get rideStatus(): string {
    let status: string = '';
    if (this.ride) {
      status = this.ride.status.replace(/_/g, ' ');
      if (this.ride.status === 'RESERVED' && this.ride.delayInMinutes > 0) return status += ' [' + this.reservationTime + "]";
      if (this.ride.status === 'DRIVER_ARRIVING') {
        const arrivalTimeInMinutes: number = this.calculateArrivalTimeInMinutes()
        if (arrivalTimeInMinutes > 0) status += ' IN ~' + arrivalTimeInMinutes + ' MINUTE' + (arrivalTimeInMinutes === 1 ? '' : 'S');
        if (arrivalTimeInMinutes === 0) status += ' IN LESS THAN A MINUTE';
        return status;
      }
      if (this.ride.status === 'IN_PROGRESS') {
        const arrivalTimeInMinutes: number = this.calculateArrivalTimeInMinutes()
        if (arrivalTimeInMinutes > 0) status += ' │ ARRIVAL IN ~' + arrivalTimeInMinutes + ' MINUTE' + (arrivalTimeInMinutes === 1 ? '' : 'S');
        if (arrivalTimeInMinutes === 0) status += ' │ ARRIVAL IN LESS THAN A MINUTE';
        return status;
      }
      return status;
    }
    return '';
  }

  fetchDriverImage(): void {
    const ride: RideSimple | null = this.ride;
    if (ride)
      this.photoService.loadImage(ride.driver.profilePicture).then((response) => {
        ride.driver.profilePicture = response.data;
      });
  }

  calculateArrivalTimeInMinutes(): number {
    if (this.ride) {
      const vehicle: Vehicle = this.ride.driver.vehicle;
      if (vehicle.expectedTripTime) {
        const tripTime: number = this.ride.status === 'IN_PROGRESS' ? this.ride.expectedTime : vehicle.expectedTripTime;
        let startingMoment = moment(vehicle.coordinatesChangedAt);
        let currentMoment = moment();
        const difference = currentMoment.diff(startingMoment, 'seconds');
        const remainingTime = tripTime - difference;
        if (remainingTime <= 0) return -1;
        return Math.round(remainingTime / 60);
      }
    }
    return -1;
  }

  get driverRating(): string {
    if (!this.ride) return ''
    if (this.ride.driver.totalRatingSum === 0) return '-';
    return parseFloat((this.ride.driver.totalRatingSum / this.ride.driver.numberOfReviews).toString()).toFixed(2);
  }

  get reservationTime(): string {
    if (this.ride) {
      const creationTime: moment.Moment = moment(this.ride.createdAt);
      creationTime.add(this.ride.delayInMinutes, 'minutes')
  
      return creationTime.format('HH:mm');
    }
    return '';
  }

  get vehicleImage(): string {
    if (this.ride?.driver.vehicle.vehicleType.name === 'COUPE')
      return this.coupeImg;
    else if (this.ride?.driver.vehicle.vehicleType.name  === 'MINIVAN')
      return this.minivanImg;
    else if (this.ride?.driver.vehicle.vehicleType.name  === 'STATION')
      return this.stationImg;
    return this.coupeImg;
  }

}
