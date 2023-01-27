import { Component, Input, OnInit } from '@angular/core';
import { IconDefinition, faCar, faEnvelope, faPaperPlane, faMobileRetro, faBabyCarriage, faPaw } from '@fortawesome/free-solid-svg-icons';
import { PhotoService } from 'src/app/core/http/user/photo.service';
import { Driver } from 'src/app/shared/models/driver.model';

@Component({
  selector: 'app-driver-details',
  templateUrl: './driver-details.component.html',
})
export class DriverDetailsComponent implements OnInit {
  @Input() driver!: Driver;
  userImage: string = '';

  faCar: IconDefinition = faCar;
  faPaperPlane: IconDefinition = faPaperPlane;
  faMobileRetro: IconDefinition = faMobileRetro;
  faBabyCarriage: IconDefinition = faBabyCarriage;
  faPaw: IconDefinition = faPaw;
  faEnvelope: IconDefinition = faEnvelope;

  constructor(private photoService: PhotoService) { }

  ngOnInit(): void {
    this.loadImage();
  }

  loadImage(): void {
    if (this.driver.profilePicture) {
      this.photoService.loadImage(this.driver.profilePicture)
      .then((response) => {
        this.userImage = response.data;
      });
    }
  }

  getDriverRating(): string {
    if (this.driver.totalRatingSum === 0) return '-';
    return parseFloat((this.driver.totalRatingSum / this.driver.numberOfReviews).toString()).toFixed(2);
  }

  getDistanceTravelled(): string {
    return parseFloat(this.driver.distanceTravelled.toString()).toFixed(2);
  }

  getCarImage(): string {
    switch (this.driver.vehicle.vehicleType.name.toLocaleLowerCase()) {
      case "coupe":
        return 'assets/icons/car-coupe.png';
      case "minivan":
        return 'assets/icons/car-minivan.png';
      case "station":
        return 'assets/icons/car-station.png';
      default:
        return 'assets/icons/car-coupe.png';
    }
  }

}
