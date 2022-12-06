import { Component, Input, OnInit } from '@angular/core';
import { faCar, faEnvelope, faMobileRetro, faPaperPlane, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { Passenger } from 'src/app/shared/models/passenger.model';

@Component({
  selector: 'app-passenger-details',
  templateUrl: './passenger-details.component.html',
})
export class PassengerDetailsComponent implements OnInit {
  @Input() passenger!: Passenger;

  faCar: IconDefinition = faCar;
  faPaperPlane: IconDefinition = faPaperPlane;
  faMobileRetro: IconDefinition = faMobileRetro;
  faEnvelope: IconDefinition = faEnvelope;

  constructor() { }

  ngOnInit(): void {
  }

  getDistanceTravelled(): string {
    return parseFloat(this.passenger.distanceTravelled.toString()).toFixed(2);
  }

}
