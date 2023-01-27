import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { IconDefinition, faChevronRight, faChevronLeft, faChevronUp, faChevronDown,
  faCircle, faFlagCheckered, faStop, faPlus, faXmark, faStopwatch, faRoute, faPaw, 
  faBabyCarriage, faHandHoldingUsd, faEnvelope, faStopwatch20 } from '@fortawesome/free-solid-svg-icons';
import * as moment from 'moment';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { RideService } from 'src/app/core/http/ride/ride.service';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import { VehicleType } from 'src/app/shared/models/vehicle-type.model';

@Component({
  selector: 'app-order-menu',
  templateUrl: './order-menu.component.html',
  styleUrls: ['./order-menu.component.css', './order-menu-modal.component.scss']
})
export class OrderMenuComponent implements OnInit {
  @Input() waypoints: any[] = [];
  @Input() route!: any;
  @Output() stopAdded: EventEmitter<string> = new EventEmitter<string>();
  @Output() stopRemoved: EventEmitter<number> = new EventEmitter<number>();

  faChevronRight: IconDefinition = faChevronRight;
  faChevronLeft: IconDefinition = faChevronLeft;
  faChevronUp: IconDefinition = faChevronUp;
  faChevronDown: IconDefinition = faChevronDown;
  faCircle: IconDefinition = faCircle;
  faStop: IconDefinition = faStop;
  faPlus: IconDefinition = faPlus;
  faFlagCheckered: IconDefinition = faFlagCheckered;
  faXmark: IconDefinition = faXmark;
  faStopwatch: IconDefinition = faStopwatch;
  faRoute: IconDefinition = faRoute;
  faBabyCarriage: IconDefinition = faBabyCarriage;
  faPaw: IconDefinition = faPaw;
  faEnvelope: IconDefinition = faEnvelope;
  faHandHoldingUsd: IconDefinition = faHandHoldingUsd;
  faStopwatch20: IconDefinition = faStopwatch20;

  accountType: string = this.authenticationService.getAccountType();
  isOpened: boolean = false;
  newStopQuery: string = '';

  expandSplitFare: boolean = false;
  linkedPassengers: string[] = [];
  splitFareForm = new FormGroup({
    splitFareEmail: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)
    ]),
  });

  expandReservations: boolean = false;
  delayInHours: number = 0;
  delayInMinutes: number = 0;

  coupeImg: string = 'assets/icons/car-coupe.png';
  minivanImg: string = 'assets/icons/car-minivan-gray.png';
  stationImg: string = 'assets/icons/car-station-gray.png';

  selectedVehicleType!: VehicleType;
  hasBabySeat: boolean = false;
  isPetFriendly: boolean = false;

  vehicleTypes: VehicleType[] = [];

  showSplitFareSuccessModal: boolean = false;

  showErrorModal: boolean = false;
  errorTitle: string = ''
  errorMessage: string = '';

  constructor(
      private authenticationService: AuthenticationService,
      private rideService: RideService,
      private passengerService: PassengerService
    ) { }

  async ngOnInit(): Promise<void> {
    await this.loadVehicleTypes();
  }

  orderRide(): void {
    const startWaypoint: any = this.waypoints[0];
    const destinationWaypoint: any = this.waypoints[this.waypoints.length - 1];
    const orderData: any = {
      distance: Number((this.route.summary.totalDistance / 1000).toLocaleString('fullwide', {minimumFractionDigits:2, maximumFractionDigits:2})),
      babySeat: this.hasBabySeat,
      petFriendly: this.isPetFriendly,
      vehicleType: this.selectedVehicleType.name,
      expectedTime: this.route.summary.totalTime,
      route: {
        waypoints: this.route.waypoints.map((waypoint: any) => waypoint.latLng),
        coordinates: this.route.coordinates
      },
      usersToPay: this.linkedPassengers,
      startAddress: this.getAddressName(startWaypoint),
      destinationAddress: this.getAddressName(destinationWaypoint),
      delayInMinutes: this.totalDelayInMinutes
    };
    if (orderData.usersToPay.length > 0) {
      this.rideService.orderSplitFareRide(orderData)
      .then(res => {
          this.showSplitFareSuccessModal = true;
          setTimeout(() => {
            this.confirmRideResponse();
          }, 2000);
      })
      .catch(err => {
        if (err.response.data?.message) {
          this.openErrorModal('Error', err.response.data?.message);
        } else if (err.response.data?.distance) {
          this.openErrorModal('Error', err.response.data?.distance);
        } else {
          this.openErrorModal('Oops!', 'Something went wrong...');
        }
      });
    }
    else {
      this.rideService.orderBasicRide(orderData)
      .then(res => {
        this.passengerService.setCurrentRide(res.data);
        window.location.href="/";
      })
      .catch(err => {
        if (err.response.data?.message) {
          this.openErrorModal('Error', err.response.data?.message);
        } else if (err.response.data?.distance) {
          this.openErrorModal('Error', err.response.data?.distance);
        } else {
          this.openErrorModal('Oops!', 'Something went wrong...');
        }
      });
    }
  }

  getAddressName(waypoint: any): string {
    return `${waypoint.raw.address.road ? waypoint.raw.address.road + (waypoint.raw.address.house_number ? ' ' +
    waypoint.raw.address.house_number : '') + (waypoint.raw.address.city ? ', ' : '') : ''}${waypoint.raw.address.quarter ?
    waypoint.raw.address.quarter + (waypoint.raw.address.city ? ', ' : '') : ''}${waypoint.raw.address.city}`;
  }

  calculateRidePrice(): number {
    if (this.selectedVehicleType)
      return Number((this.selectedVehicleType.price + Number((this.route.summary?.totalDistance / 1000).toFixed(2)) * 120).toFixed(0));
    return -1;
  }

  setVehicleType(typeName: string) {
    const potentialType: VehicleType | undefined = this.vehicleTypes.find(type => type.name === typeName);
    if (potentialType) {
      this.selectedVehicleType = potentialType;
      this.splitFareEmail?.reset();
      if (this.linkedPassengers.length > this.selectedVehicleType.seats) {
        this.linkedPassengers.splice(this.selectedVehicleType.seats)
      }
    }
  }

  addStop(): void {
    this.stopAdded.emit(this.newStopQuery);
    this.newStopQuery = '';
  }

  removeStop(i: number): void {
    this.stopRemoved.emit(i);
  }

  linkPassenger(): void {
    this.splitFareForm.updateValueAndValidity();
    this.splitFareEmail?.markAsTouched();
    if (this.splitFareEmail?.value && this.splitFareEmail?.valid) {
      this.linkedPassengers.push(this.splitFareEmail?.value);
      this.splitFareEmail.reset();
    }
  }

  unlinkPassenger(i: number): void {
    this.linkedPassengers.splice(i, 1);
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
      this.loadVehicleTypes();
    }
    else {
      setTimeout(() => { this.isOpened = false; }, 250);
      document.getElementById('order-menu')?.classList.remove(inAnimation);
      document.getElementById('order-menu')?.classList.add(outAnimation);
    }
  }

  confirmRideResponse(): void {
    this.showSplitFareSuccessModal = false;
    window.location.href="/";
  }

  async loadVehicleTypes(): Promise<void> {
    if (this.vehicleTypes.length === 0)
      this.vehicleTypes = await this.rideService.getVehicleTypes();
      this.selectedVehicleType = this.vehicleTypes[0];
  }

  getIcon(i: number) {
    if (i === 0) return this.faCircle;
    else if (i === this.waypoints.length - 1) return this.faFlagCheckered;
    else return this.faStop;
  }

  get splitFareEmail() {
    return this.splitFareForm.get('splitFareEmail');
  }
  
  openErrorModal (errorTitle: string, errorMessage: string): void {
    this.showErrorModal = true;
    this.errorTitle = errorTitle;
    this.errorMessage = errorMessage;
    this.showErrorModal = true;
  }

  closeErrorModal () {
    this.showErrorModal = false;
    this.errorMessage = '';
    this.errorTitle = '';
  }
  
  get reservationTime(): string {
    if (this.expandReservations && this.totalDelayInMinutes > 0) {
      const now: moment.Moment = moment();
      now.add(this.totalDelayInMinutes, 'minutes')
      return now.format('HH:mm');
    }
    return '';
  }

  get totalDelayInMinutes(): number {
    return 60 * this.delayInHours + this.delayInMinutes;
  }

  @ViewChild('hourdelayinput') hourDelayInput!: ElementRef;
  @ViewChild('minutedelayinput') minuteDelayInput!: ElementRef;

  get delayInHoursFormatted(): number {
    return this.delayInHours;
  }

  set delayInHoursFormatted(value: number) {
    if (!value) {
      this.delayInHours = 0;
    } else if (value < 0) {
      this.delayInHours = 0;
    } else if (value >= 5) {
      this.delayInHours = 5;
      this.delayInMinutes = 0;
    } else {
      this.delayInHours = value;
    }
    this.hourDelayInput.nativeElement.value = this.delayInHours;
  }

  get delayInMinutesFormatted(): number {
    return this.delayInMinutes;
  }

  set delayInMinutesFormatted(value: number) {
    if (!value) {
      this.delayInMinutes = 0;
    } else if (value < 0) {
      this.delayInMinutes = 0;
    } else if (value > 59) {
      this.delayInMinutes = 59;
    } else if (this.delayInHours >= 5) {
      this.delayInMinutes = 0;
    } else {
      this.delayInMinutes = value;
    }
    this.minuteDelayInput.nativeElement.value = this.delayInMinutes;
  }
}
