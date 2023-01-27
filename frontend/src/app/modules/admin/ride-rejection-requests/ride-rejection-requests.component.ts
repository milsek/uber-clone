import { Component, OnInit } from '@angular/core';
import { faCheck, faChevronLeft, faXmark, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { DriverService } from 'src/app/core/http/user/driver.service';
import { PhotoService } from 'src/app/core/http/user/photo.service';
import { DriverRideRejectionRequest } from 'src/app/shared/models/drver-ride-rejection-request.model';

@Component({
  selector: 'app-ride-rejection-requests',
  templateUrl: './ride-rejection-requests.component.html',
})
export class RideRejectionRequestsComponent implements OnInit {
  faChevronLeft: IconDefinition = faChevronLeft;
  faCheck: IconDefinition = faCheck;
  faXmark: IconDefinition = faXmark;
  requests: DriverRideRejectionRequest[] = [];

  constructor(
    private driverService: DriverService,
    private photoService: PhotoService
  ) {}

  ngOnInit(): void {
    this.fetchRequests();
  }

  fetchRequests(): void {
    this.driverService.getRideRejectionRequests()
    .then(res => {
      this.requests = res.data;
      this.fetchImages();
    });
  }

  fetchImages(): void {
    this.requests.map(request => request.driver)
    .forEach(driver => this.getImage(driver.profilePicture));
  }

  getImage(profilePicture: string): void {
    this.photoService.loadImage(profilePicture).then((response) => {
      for (let driver of this.requests.map(request => request.driver)) {
        if (driver.profilePicture === profilePicture) {
          driver.profilePicture = response.data;
        }
      }
    });
  }

  rejectRequest(i: number) {
    this.driverService.sendRideRejectionRequestVerdict(this.requests[i].id, false)
    .then(res => {
      this.requests.splice(i, 1);
    });
  }

  acceptRequest(i: number) {
    this.driverService.sendRideRejectionRequestVerdict(this.requests[i].id, true)
    .then(res => {
      this.requests.splice(i, 1);
    });
  }

  goHome(): void {
    window.location.href = '/';
  }

}
