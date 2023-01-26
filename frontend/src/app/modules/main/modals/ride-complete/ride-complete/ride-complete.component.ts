import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { PassengerService } from 'src/app/core/http/user/passenger.service';

@Component({
  selector: 'app-ride-complete',
  templateUrl: './ride-complete.component.html',
  styleUrls: ['./ride-complete.component.scss']
})
export class RideCompleteComponent implements OnInit {
  @Output() onClose: EventEmitter<void> = new EventEmitter();
  showReviewModal: boolean = false;

  constructor(
    private passengerService: PassengerService,
    ) { }

  ngOnInit(): void {
  }

  openReviewModal(): void {
    this.showReviewModal = true;
  }

  onReviewSent(): void {
    window.location.href = '/';
  }

  close () {
    this.onClose.emit();
  }

  get ride() {
    return this.passengerService.getCurrentRide();
  }
}
