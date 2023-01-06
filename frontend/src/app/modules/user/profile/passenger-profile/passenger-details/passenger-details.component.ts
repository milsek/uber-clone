import { Component, Input, OnInit } from '@angular/core';
import { faCar, faEnvelope, faMobileRetro, faPaperPlane, IconDefinition, faMoneyBill1Wave,faPlus } from '@fortawesome/free-solid-svg-icons';
import { Passenger } from 'src/app/shared/models/passenger.model';
import { PassengerService } from 'src/app/core/http/user/passenger.service';

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
  faMoneyBill1Wave: IconDefinition = faMoneyBill1Wave;
  faPlus: IconDefinition = faPlus;

  constructor(private passengerService: PassengerService) { }

  ngOnInit(): void {
  }

  getDistanceTravelled(): string {
    return parseFloat(this.passenger.distanceTravelled.toString()).toFixed(2);
  }

  addTokens(): void{
    this.passengerService.addTokens(5);
  }

}
