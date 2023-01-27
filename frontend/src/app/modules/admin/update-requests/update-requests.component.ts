import { Component, OnInit } from '@angular/core';
import {
  faChevronLeft,
  IconDefinition,
  faCheck,
  faXmark,
} from '@fortawesome/free-solid-svg-icons';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { DriverService } from 'src/app/core/http/user/driver.service';
import { PhotoService } from 'src/app/core/http/user/photo.service';
import { Driver, DriverNewData } from 'src/app/shared/models/driver.model';

@Component({
  selector: 'app-update-requests',
  templateUrl: './update-requests.component.html',
})
export class UpdateRequestsComponent implements OnInit {
  faChevronLeft: IconDefinition = faChevronLeft;
  faCheck: IconDefinition = faCheck;
  faXmark: IconDefinition = faXmark;
  requests: Array<DriverNewData> = [];
  newData: DriverNewData | undefined;
  oldData: Driver | undefined;
  selectedRequest: number = 0;
  oldPicture = '';
  newPicture = '';
  confirmModalTitle: string = '';
  confirmModalDescription: string = '';
  showConfirmModal: boolean = false;

  constructor(
    private authenticationService: AuthenticationService,
    private driverService: DriverService,
    private photoService: PhotoService
  ) {}

  ngOnInit(): void {
    this.driverService.getUpdateRequests().then((res) => {
      this.requests = res.data;
      this.newData = this.requests[0];
      this.driverService
        .getDriverByUsername(this.requests[0].username)
        .then((res) => {
          this.oldData = res.data;
          this.getPictures();
        });
    });
  }

  getPictures(): void {
    this.photoService
      .loadImage(this.oldData!.profilePicture)
      .then((response) => {
        this.oldPicture = response.data;
      });
    this.photoService
      .loadImage(this.newData!.profilePicture)
      .then((response) => {
        this.newPicture = response.data;
      });
  }

  changeRequest(username: string): void {
    for (let i = 0; i < this.requests.length; i++) {
      if (this.requests[i].username === username) {
        if (i === this.selectedRequest) return;
        this.selectedRequest = i;
        this.newData = this.requests[i];
        this.driverService
          .getDriverByUsername(this.requests[i].username)
          .then((res) => {
            this.oldData = res.data;
            this.getPictures();
          });
        return;
      }
    }
  }

  getImage(pictureName: string): void {
    this.photoService.loadImage(pictureName).then((response) => {
      for (let i = 0; i < this.requests.length; i++) {
        if (this.requests[i].profilePicture === pictureName) {
          this.requests[i].userImage = response.data;
        }
      }
    });
  }

  approveUpdate(): void {
    this.authenticationService.updateDriver(
      this.newData!.username,
      this.newData!.name,
      this.newData!.surname,
      this.newData!.phoneNumber,
      this.newData!.city,
      this.newData!.profilePicture,
      this.newData!.vehicleType,
      this.newData!.babySeat,
      this.newData!.petsAllowed,
      this.newData!.make,
      this.newData!.model,
      this.newData!.colour,
      this.newData!.licensePlateNumber
    ).then(res => {
      this.displayConfirmModal("Success", "Update approved.");
      this.driverService.getUpdateRequests().then((res) => {
        this.requests = res.data;
        this.newData = this.requests[0];
        this.driverService
          .getDriverByUsername(this.requests[0].username)
          .then((res) => {
            this.oldData = res.data;
            this.getPictures();
          });
      });
    });
  }

  rejectUpdate(): void {
    this.authenticationService.cancelRequest(this.newData!.username)
    .then(res => {
      this.displayConfirmModal("Success", "Update rejected.");
      this.driverService.getUpdateRequests().then((res) => {
        this.requests = res.data;
        this.newData = this.requests[0];
        this.driverService
          .getDriverByUsername(this.requests[0].username)
          .then((res) => {
            this.oldData = res.data;
            this.getPictures();
          });
      });
    })
  }

  displayConfirmModal(title: string, description: string): void {
    this.confirmModalTitle = title;
    this.confirmModalDescription = description;
    this.showConfirmModal = true;
  }

  closeConfirmModal(): void {
    this.confirmModalTitle = '';
    this.confirmModalDescription = '';
    this.showConfirmModal = false;
  }
  
  goHome(): void {
    window.location.href = '/';
  }
}
