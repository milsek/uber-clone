import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { RideService } from 'src/app/core/http/ride/ride.service';
import { PassengerService } from 'src/app/core/http/user/passenger.service';

@Component({
  selector: 'app-ride-complete',
  templateUrl: './ride-complete.component.html',
  styleUrls: ['./ride-complete.component.scss']
})
export class RideCompleteComponent implements OnInit {
  @Output() onClose: EventEmitter<void> = new EventEmitter();

  constructor(
    private passengerService: PassengerService,
    private rideService: RideService,
    ) { }

  ngOnInit(): void {
  }

  close () {
    this.onClose.emit();
  }

  get ride() {
    return this.passengerService.getCurrentRide();
  }
}
