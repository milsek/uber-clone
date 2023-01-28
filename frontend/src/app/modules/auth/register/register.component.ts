import { Component } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { faChevronLeft, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { PassengerService } from 'src/app/core/http/user/passenger.service';


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  faChevronLeft: IconDefinition = faChevronLeft;

  registrationSuccessful: boolean = false;
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
    ])
  }, { validators: this.checkPasswords });

  constructor(private passengerService: PassengerService) {
    document.getElementById('register-email')?.focus();
  }

  onSubmit() {
    if (this.registerForm.valid) {
      this.passengerService.register(
        {
          username: this.email?.value,
          email: this.email?.value,
          password: this.password?.value,
          name: this.name?.value,
          surname: this.surname?.value,
          phoneNumber: this.phoneNumber?.value,
          city: this.city?.value,
        }
      )
      .then((res) => {
        this.registerForm.reset();
        this.registrationSuccessful = true;
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

  get arePasswordsDifferent() {
    return this.password?.value !== this.confirmPassword?.value;
  }

  goHome(): void {
    window.location.href = '/';
  }
}
