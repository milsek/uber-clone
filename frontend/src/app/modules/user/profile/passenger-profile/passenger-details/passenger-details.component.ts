import { Component, HostListener, Input, OnInit } from '@angular/core';
import { faCar, faEnvelope, faMobileRetro, faPaperPlane, IconDefinition, faMoneyBill1Wave, faPlus } from '@fortawesome/free-solid-svg-icons';
import { Passenger } from 'src/app/shared/models/passenger.model';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import { PhotoService } from 'src/app/core/http/user/photo.service';

@Component({
  selector: 'app-passenger-details',
  templateUrl: './passenger-details.component.html',
})
export class PassengerDetailsComponent implements OnInit {
  @Input() passenger!: Passenger;
  userImage: string = '';

  faCar: IconDefinition = faCar;
  faPaperPlane: IconDefinition = faPaperPlane;
  faMobileRetro: IconDefinition = faMobileRetro;
  faEnvelope: IconDefinition = faEnvelope;
  faMoneyBill1Wave: IconDefinition = faMoneyBill1Wave;
  faPlus: IconDefinition = faPlus;

  clickedBuyTokensModal: boolean = false;
  showBuyTokensModal: boolean = false;

  tokensToBuy: Number = 100;
  buyTokensErrorMessage: string = '';

  constructor(private passengerService: PassengerService, private photoService: PhotoService) { }

  ngOnInit(): void {
    this.loadImage();
  }

  getDistanceTravelled(): string {
    return parseFloat(this.passenger.distanceTravelled.toString()).toFixed(2);
  }

  addTokens(): void {
    if (typeof this.tokensToBuy !== 'number') {
      this.buyTokensErrorMessage = 'Invalid amount';
      return;
    }
    if (!Number.isInteger(this.tokensToBuy)) {
      this.buyTokensErrorMessage = 'Amount must be a positive integer';
      return;
    }
    if (this.tokensToBuy < 100) {
      this.buyTokensErrorMessage = 'Minimum amount is 100';
      return;
    }
    if (this.tokensToBuy % 100 !== 0) {
      this.buyTokensErrorMessage = 'Amount must be divisible by 100';
      return;
    }
    this.passengerService.addTokens(this.tokensToBuy / 100);
  }

  loadImage(): void {
    if (this.passenger.profilePicture) {
      this.photoService.loadImage(this.passenger.profilePicture)
      .then((response) => {
        this.userImage = response.data;
      });
    }
  }

  @HostListener('document:click')
  clickout() {
    if (this.showBuyTokensModal && !this.clickedBuyTokensModal) {
      this.showBuyTokensModal = false;
    }
    this.clickedBuyTokensModal = false;
  }

}
