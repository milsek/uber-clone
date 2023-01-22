import { Component, OnInit } from '@angular/core';
import { faCheck, faChevronLeft, faXmark, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { DriverService } from 'src/app/core/http/user/driver.service';
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
    private driverService: DriverService
  ) {}

  ngOnInit(): void {
    this.driverService.getRideRejectionRequests()
    .then(res => {
      this.requests = res.data;
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

}
