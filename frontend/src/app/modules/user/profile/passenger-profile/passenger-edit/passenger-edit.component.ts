import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Passenger } from 'src/app/shared/models/passenger.model';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { PhotoService } from 'src/app/core/http/user/photo.service';

@Component({
  selector: 'app-passenger-edit',
  templateUrl: './passenger-edit.component.html',
})
export class PassengerEditComponent implements OnInit {
  @Input() passenger!: Passenger;
  currentImage = '';
  newImage: any = '';
  newFile: File | undefined;

  showResetPasswordConfirmationModal: boolean = false;

  @Output() changeView = new EventEmitter<void>();

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
        Validators.pattern(
          '^[+]?[(]?[0-9]{3}[)]?[-s.]?[0-9]{3}[-s.]?[0-9]{4,6}$'
        ),
      ]),
    },
    {}
  );

  constructor(
    private authenticationService: AuthenticationService,
    private photoService: PhotoService
  ) {
    setTimeout(
      () =>
        this.userEditForm.setValue({
          name: this.passenger.name,
          surname: this.passenger.surname,
          username: this.passenger.username,
          phoneNumber: this.passenger.phoneNumber,
          city: this.passenger.city,
        }),
      10
    );
  }

  ngOnInit(): void {
    this.photoService
      .loadImage(this.passenger.profilePicture)
      .then((response) => {
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

  async resetPassword() {
    this.authenticationService.resetPasword(this.passenger.email!);
    this.showResetPasswordConfirmationModal = true;
  }

  async onSubmitUserUpdate() {
    if (this.userEditForm.valid) {
      let filename = '';
      if (this.newImage !== '') {
        await this.photoService.storeImage(this.newFile!).then((res) => {
          filename = res.data;
        });
      }
      if (filename === '') filename = this.passenger.profilePicture;
      this.authenticationService.updateUser(
        this.username?.value!,
        this.name?.value!,
        this.surname?.value!,
        this.phoneNumber?.value!,
        this.city?.value!,
        filename
      );
      this.changeView.emit();
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

  closeResetPasswordConfirmationModal(): void {
    this.showResetPasswordConfirmationModal = false;
  }
}
