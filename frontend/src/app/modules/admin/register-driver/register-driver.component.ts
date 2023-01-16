import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { faBabyCarriage, faChevronLeft, faPaw, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { DriverService } from 'src/app/core/http/user/driver.service';

@Component({
  selector: 'app-register-driver',
  templateUrl: './register-driver.component.html',
})
export class RegisterDriverComponent implements OnInit {
  faChevronLeft: IconDefinition = faChevronLeft;
  faBabyCarriage: IconDefinition = faBabyCarriage;
  faPaw: IconDefinition = faPaw;
  accountType: string = this.authenticationService.getAccountType();

  selectedVehicleType: string = 'COUPE';
  hasBabySeat: boolean = false;
  isPetFriendly: boolean = false;

  responseMessage: string = '';
  errorMessage: string = '';
  

  checkPasswords: ValidatorFn = (group: AbstractControl):  ValidationErrors | null => { 
    let pass = group.get('password')?.value;
    let confirmPass = group.get('confirmPassword')?.value
    return pass === confirmPass ? null : { notSame: true }
  }

  registerForm = new FormGroup({
    email: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)
    ]),
    name: new FormControl('', [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(20)
    ]),
    surname: new FormControl('', [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(30)
    ]),
    city: new FormControl('', [
      Validators.required,
    ]),
    phoneNumber: new FormControl('', [
      Validators.required,
      Validators.pattern(/[+]?[(]?\d{3}[)]?[-\s.]?\d{3}[-\s.]?\d{4,6}/)
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8),
    ]),
    confirmPassword: new FormControl('', [
      Validators.required,
    ]),
    make: new FormControl('', [
      Validators.required,
    ]),
    model: new FormControl('', [
      Validators.required,
    ]),
    colour: new FormControl('', [
      Validators.required,
    ]),
    licensePlateNumber: new FormControl('', [
      Validators.required,
    ]),
  }, { validators: this.checkPasswords });

  constructor(private authenticationService: AuthenticationService, private driverService: DriverService) {
    document.getElementById('register-email')?.focus();
  }

  ngOnInit(): void {
  }

  onSubmit() {
    if (this.registerForm.valid) {
      this.driverService.register(
        {
          username: this.email?.value,
          email: this.email?.value,
          password: this.password?.value,
          name: this.name?.value,
          surname: this.surname?.value,
          phoneNumber: this.phoneNumber?.value,
          city: this.city?.value,
          vehicleType: this.selectedVehicleType,
          babySeat: this.hasBabySeat,
          petsAllowed: this.isPetFriendly,
          make: this.make?.value,
          model: this.model?.value,
          colour: this.colour?.value,
          licensePlateNumber: this.licensePlateNumber?.value
        }
      )
      .then((res) => {
        this.registerForm.reset();
        this.responseMessage = 'Registration successful!';
      })
      .catch((err) => {
        if (err.response.data?.message === 'Username or email already exists.') {
          this.errorMessage = "Email already in use.";
        } else {
          this.errorMessage = 'Invalid information.'
        }
      })
    }
  }

  get email() {
    return this.registerForm.get('email');
  }
  get name() {
    return this.registerForm.get('name');
  }
  get surname() {
    return this.registerForm.get('surname');
  }
  get city() {
    return this.registerForm.get('city');
  }
  get phoneNumber() {
    return this.registerForm.get('phoneNumber');
  }
  get password() {
    return this.registerForm.get('password');
  }
  get confirmPassword() {
    return this.registerForm.get('confirmPassword');
  }
  get make() {
    return this.registerForm.get('make');
  }
  get model() {
    return this.registerForm.get('model');
  }
  get colour() {
    return this.registerForm.get('colour');
  }
  get licensePlateNumber() {
    return this.registerForm.get('licensePlateNumber');
  }
  
  setVehicleType(typeName: string) {
    this.selectedVehicleType = typeName;
  }

  get arePasswordsDifferent() {
    return this.password?.value !== this.confirmPassword?.value;
  }

}
