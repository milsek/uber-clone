import { Component, Input, Output, EventEmitter } from '@angular/core';
import { faStar, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { RideService } from 'src/app/core/http/ride/ride.service';

@Component({
  selector: 'app-ride-review-modal',
  templateUrl: './ride-review-modal.component.html',
})
export class RideReviewModalComponent {
  @Input() rideId!: number | null | undefined;
  @Output() reviewSent: EventEmitter<any> = new EventEmitter();
  
  faStar: IconDefinition = faStar;

  selectedDriverRating: number = -1;
  hoveredDriverRating: number = -1;
  selectedVehicleRating: number = -1;
  hoveredVehicleRating: number = -1;

  comment: string = '';
  errorMessage: string = '';

  constructor(private rideService: RideService) { }

  sendReview(): void {
    if (this.rideId) {
      if (!this.isDataValid()) return;
      this.rideService.sendReview(
        {
          rideId: this.rideId,
          driverRating: this.selectedDriverRating + 1,
          vehicleRating: this.selectedVehicleRating + 1,
          comment: this.comment.trim()
        }
      ).then(res => {
        this.reviewSent.emit();
        // window.location.href = '/';
      }).catch(err => {
        this.errorMessage = 'Something went wrong. Try again later using your ride history.';
      })
    }
  }

  isDataValid(): boolean {
    if (this.selectedDriverRating < 0) {
      this.errorMessage = 'Please select a rating for the driver.';
      return false;
    }
    if (this.selectedVehicleRating < 0) {
      this.errorMessage = 'Please select a rating for the vehicle.';
      return false;
    }
    if (this.comment.trim().length < 1) {
      this.errorMessage = 'Please enter a comment.';
      return false;
    }
    if (this.comment.trim().length > 255) {
      this.errorMessage = 'Maximum comment length is 255 characters.';
      return false;
    }
    return true;
  }

  setSelectedDriverRating(i: number): void {
    this.selectedDriverRating = i;
  }

  setHoveredDriverRating(i: number): void {
    this.hoveredDriverRating = i;
  }

  setSelectedVehicleRating(i: number): void {
    this.selectedVehicleRating = i;
  }

  setHoveredVehicleRating(i: number): void {
    this.hoveredVehicleRating = i;
  }

  setArrayFromNumber(i: number) {
    return new Array(i);
  }

  close(): void {
    window.location.href = '/';
  }

}
