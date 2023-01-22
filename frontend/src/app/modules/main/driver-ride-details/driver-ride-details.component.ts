import { Component, HostListener, Input, OnInit } from '@angular/core';
import { faChevronDown, faChevronLeft, faChevronRight, faChevronUp, faCircle, faFlagCheckered, faHandHoldingUsd, faRoute, faStop, faStopwatch, IconDefinition, faUser } from '@fortawesome/free-solid-svg-icons';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { RideService } from 'src/app/core/http/ride/ride.service';
import { DriverService } from 'src/app/core/http/user/driver.service';
import { DriverRide } from 'src/app/shared/models/ride.model';

@Component({
  selector: 'app-driver-ride-details',
  templateUrl: './driver-ride-details.component.html',
  styleUrls: ['./driver-ride-details.component.css']
})
export class DriverRideDetailsComponent implements OnInit {
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
  faUser: IconDefinition = faUser;

  accountType: string = this.authenticationService.getAccountType();
  isOpened: boolean = true;

  clickedRejectRideModal: boolean = false;
  showRejectRideModal: boolean = false;
  rideRejectionReason: string = '';
  rideRejectionErrorMessage: string = '';
  rideRejectionSent: boolean = false;

  constructor(
    private authenticationService: AuthenticationService,
    private driverService: DriverService,
    private rideService: RideService
    ) { }

  ngOnInit(): void {
  }

  openRejectRideModal(): void {
    this.showRejectRideModal = true;
    this.clickedRejectRideModal = true;
  }

  rejectRide(): void {
    if (this.rideRejectionReason.length < 10) {
      this.rideRejectionErrorMessage = 'Reason is too short.';
      return;
    }
    if (this.ride && this.ride.status === 'DRIVER_ARRIVING')
      this.rideService.driverRejectRide(this.ride.id, this.rideRejectionReason)
      .then(res => {
        this.showRejectRideModal = false;
        this.rideRejectionSent = true;
      })
      .catch(err => {
        this.rideRejectionErrorMessage = 'Something went wrong.';
      });
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

  get ride(): DriverRide | undefined {
    return this.driverService.getCurrentRides()?.currentRide;
  }

  @HostListener('document:click')
  clickout() {
    if (this.showRejectRideModal && !this.clickedRejectRideModal) {
      this.showRejectRideModal = false;
    }
    this.clickedRejectRideModal = false;
  }

}
