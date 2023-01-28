import { Component, EventEmitter, Output } from '@angular/core';
import { PassengerService } from 'src/app/core/http/user/passenger.service';

@Component({
  selector: 'app-ride-complete',
  templateUrl: './ride-complete.component.html',
  styleUrls: ['./ride-complete.component.scss']
})
export class RideCompleteComponent {
  @Output() closeModal: EventEmitter<void> = new EventEmitter();
  showReviewModal: boolean = false;

  constructor(
    private passengerService: PassengerService,
    ) { }

  openReviewModal(): void {
    this.showReviewModal = true;
  }

  onReviewSent(): void {
    window.location.href = '/';
  }

  closeSelf () {
    this.closeModal.emit();
  }

  get ride() {
    return this.passengerService.getCurrentRide();
  }
}
