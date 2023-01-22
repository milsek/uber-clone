import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Driver } from 'src/app/shared/models/driver.model';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { PhotoService } from 'src/app/core/http/user/photo.service';
import {
  faBabyCarriage,
  faChevronLeft,
  faPaw,
  IconDefinition,
  faChevronUp,
  faChevronDown,
} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-driver-edit',
  templateUrl: './driver-edit.component.html',
})
export class DriverEditComponent implements OnInit {
  faChevronLeft: IconDefinition = faChevronLeft;
  faBabyCarriage: IconDefinition = faBabyCarriage;
  faPaw: IconDefinition = faPaw;
  faChevronUp: IconDefinition = faChevronUp;
  faChevronDown: IconDefinition = faChevronDown;
  @Input() driver!: Driver;
  currentImage = '';
  newImage: any = '';
  newFile: File | undefined;
  selectedVehicleType: string = '';
  hasBabySeat: boolean = false;
  isPetFriendly: boolean = false;

  @Output() changeView = new EventEmitter<void>();

  showConfirmModal: boolean = false;

  userEditForm = new FormGroup(
    {
      name: new FormControl('', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(20),
      ]),
      surname: new FormControl('', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(30),
      ]),
      username: new FormControl('', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(30),
      ]),
      city: new FormControl('', [
        Validators.minLength(2),
        Validators.maxLength(30),
      ]),
      phoneNumber: new FormControl('', [
        Validators.minLength(2),
        Validators.maxLength(30),
        Validators.pattern(/[+]?[(]?\d{3}[)]?[-\s.]?\d{3}[-\s.]?\d{3,6}/),
      ]),
      make: new FormControl('', [Validators.required]),
      model: new FormControl('', [Validators.required]),
      colour: new FormControl('', [Validators.required]),
      licensePlateNumber: new FormControl('', [Validators.required]),
    },
    {}
  );

  constructor(
    private authenticationService: AuthenticationService,
    private photoService: PhotoService
  ) {
    setTimeout(() => {
      this.userEditForm.setValue({
        name: this.driver.name,
        surname: this.driver.surname,
        username: this.driver.username,
        phoneNumber: this.driver.phoneNumber,
        city: this.driver.city,
        make: this.driver.vehicle.make,
        model: this.driver.vehicle.model,
        colour: this.driver.vehicle.colour,
        licensePlateNumber: this.driver.vehicle.licensePlateNumber,
      });
      this.selectedVehicleType = this.driver.vehicle.vehicleType.name;
      this.hasBabySeat = this.driver.vehicle.babySeat;
      this.isPetFriendly = this.driver.vehicle.petsAllowed;
    }, 10);
  }
  setVehicleType(typeName: string) {
    this.selectedVehicleType = typeName;
  }

  ngOnInit(): void {
    this.photoService.loadImage(this.driver.profilePicture).then((response) => {
      this.currentImage = response.data;
    });
  }

  get name() {
    return this.userEditForm.get('name');
  }
  get surname() {
    return this.userEditForm.get('surname');
  }
  get username() {
    return this.userEditForm.get('username');
  }
  get city() {
    return this.userEditForm.get('city');
  }
  get phoneNumber() {
    return this.userEditForm.get('phoneNumber');
  }
  get make() {
    return this.userEditForm.get('make');
  }
  get model() {
    return this.userEditForm.get('model');
  }
  get colour() {
    return this.userEditForm.get('colour');
  }
  get licensePlateNumber() {
    return this.userEditForm.get('licensePlateNumber');
  }
  async resetPassword() {
    this.authenticationService.resetPasword(this.driver.email!);
    alert('Email sent');
  }

  async onSubmitUserUpdate() {
    if (this.userEditForm.valid) {
      let filename = '';
      if (this.newImage !== '') {
        await this.photoService.storeImage(this.newFile!).then((res) => {
          filename = res.data;
        });
      }
      if (filename === '') filename = this.driver.profilePicture;
      this.authenticationService.sendUpdateRequest(
        this.username?.value!,
        this.name?.value!,
        this.surname?.value!,
        this.phoneNumber?.value!,
        this.city?.value!,
        filename,
        this.selectedVehicleType!,
        this.hasBabySeat!,
        this.isPetFriendly!,
        this.make?.value!,
        this.model?.value!,
        this.colour?.value!,
        this.licensePlateNumber?.value!
      );
      this.showConfirmModal = true;
    } else {
      alert('Data not valid!');
    }
  }

  readURL(event: any): void {
    if (event.target.files && event.target.files[0]) {
      const file: File = event.target.files[0];

      const reader = new FileReader();
      reader.onload = (e) => (this.newImage = reader.result);

      reader.readAsDataURL(file);
      this.newFile = file;
    }
  }

  closeModal(): void {
    this.showConfirmModal = false;
    this.changeView.emit();
  }
}
