import { Component, OnInit } from '@angular/core';
import { RideService } from 'src/app/core/http/ride/ride.service';
import { PassengerService } from 'src/app/core/http/user/passenger.service';

@Component({
  selector: 'app-split-fare-wait',
  templateUrl: './split-fare-wait.component.html',
  styleUrls: ['./split-fare-wait.component.scss']
})
export class SplitFareWaitComponent implements OnInit {

  constructor(
    private passengerService: PassengerService,
    private rideService: RideService,
    ) {}

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

  get ride() {
    return this.passengerService.getCurrentRide();
  }

}
