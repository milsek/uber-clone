import { Component, Input, OnInit } from '@angular/core';
import { IconDefinition, faCar, faEnvelope, faPaperPlane, faMobileRetro, faBabyCarriage, faPaw } from '@fortawesome/free-solid-svg-icons';
import { Driver } from 'src/app/shared/models/driver.model';

@Component({
  selector: 'app-driver-details',
  templateUrl: './driver-details.component.html',
})
export class DriverDetailsComponent implements OnInit {
  @Input() driver!: Driver;

  faCar: IconDefinition = faCar;
  faPaperPlane: IconDefinition = faPaperPlane;
  faMobileRetro: IconDefinition = faMobileRetro;
  faBabyCarriage: IconDefinition = faBabyCarriage;
  faPaw: IconDefinition = faPaw;
  faEnvelope: IconDefinition = faEnvelope;

  constructor() { }

  ngOnInit(): void {
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
