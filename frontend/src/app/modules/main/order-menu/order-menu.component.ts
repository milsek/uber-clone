import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { IconDefinition, faChevronRight, faChevronLeft, faChevronUp, faChevronDown, faCircle, faFlagCheckered, faStop, faPlus, faXmark, faStopwatch, faRoute, faPaw, faBabyCarriage, faHandHoldingUsd } from '@fortawesome/free-solid-svg-icons';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { RideService } from 'src/app/core/http/ride/ride.service';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import { VehicleType } from 'src/app/shared/models/vehicle-type.model';

@Component({
  selector: 'app-order-menu',
  templateUrl: './order-menu.component.html',
  styleUrls: ['./order-menu.component.css']
})
export class OrderMenuComponent implements OnInit {
  @Input() waypoints: any[] = [];
  @Input() route!: any;
  @Input() alternativeRoute!: any;
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
  faHandHoldingUsd: IconDefinition = faHandHoldingUsd;

  accountType: string = this.authenticationService.getAccountType();
  isOpened: boolean = false;
  newStopQuery: string = '';

  coupeImg: string = 'assets/icons/car-coupe.png';
  minivanImg: string = 'assets/icons/car-minivan-gray.png';
  stationImg: string = 'assets/icons/car-station-gray.png';

  // selectedVehicleType: string = 'coupe';
  selectedVehicleType!: VehicleType;
  hasBabySeat: boolean = false;
  isPetFriendly: boolean = false;

  vehicleTypes: VehicleType[] = [];

  constructor(private authenticationService: AuthenticationService, private rideService: RideService, private passengerService: PassengerService) { }

  async ngOnInit(): Promise<void> {
    await this.loadVehicleTypes();
  }

  orderRide(): void {
    const deviateFromRoute: boolean = Math.random() > 0.75 && this.alternativeRoute;
    const actualRoute: any = deviateFromRoute ? this.alternativeRoute : this.route;
    this.rideService.orderBasicRide({
      distance: Number((this.route.summary.totalDistance / 1000).toLocaleString('fullwide', {minimumFractionDigits:2, maximumFractionDigits:2})),
      babySeat: this.hasBabySeat,
      petFriendly: this.isPetFriendly,
      vehicleType: this.selectedVehicleType.name,
      expectedTime: actualRoute.summary.totalTime,
      expectedRoute: deviateFromRoute ? {
        waypoints: this.route.waypoints,
        coordinates: this.route.coordinates
      } : null,
      actualRoute: {
        waypoints: actualRoute.waypoints.map((waypoint: any) => waypoint.latLng),
        coordinates: actualRoute.coordinates
      }
    })
    .then((res: any) => {
      this.passengerService.setCurrentRide(res.data);
      window.location.href="/";
    })
    .catch((err: any) => {
      console.log(err);
    });
  }

  calculateRidePrice(): number {
    if (this.selectedVehicleType)
      return Number((this.selectedVehicleType.price + Number((this.route.summary?.totalDistance / 1000).toFixed(2)) * 120).toFixed(0));
    return -1;
  }

  setVehicleType(typeName: string) {
    const potentialType: VehicleType | undefined = this.vehicleTypes.find(type => type.name === typeName);
    if (potentialType) this.selectedVehicleType = potentialType;
  }

  addStop(): void {
    this.stopAdded.emit(this.newStopQuery);
    this.newStopQuery = '';
  }

  removeStop(i: number): void {
    this.stopRemoved.emit(i);
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
}
