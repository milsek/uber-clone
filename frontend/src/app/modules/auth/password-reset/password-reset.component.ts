import { Component } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { faChevronLeft, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { ActivatedRoute } from '@angular/router';



@Component({
  selector: 'app-password-reset',
  templateUrl: './password-reset.component.html',
})
export class PasswordResetComponent {
  faChevronLeft: IconDefinition = faChevronLeft;
  wrongCredentials: boolean = false;

  showPasswordChangeResponseModal: boolean = false;
  passwordChangeResponseModalTitle: string = '';
  passwordChangeResponseModalMessage: string = '';

  errorMessage: string = '';

  checkPasswords: ValidatorFn = (group: AbstractControl):  ValidationErrors | null => { 
    let pass = group.get('password')?.value;
    let confirmPass = group.get('confirmPassword')?.value
    return pass === confirmPass ? null : { notSame: true }
  }

  passwordResetForm = new FormGroup({
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8)
    ]),
    confirmPassword: new FormControl('', [
      Validators.required
    ]),
  }, { validators: this.checkPasswords });
  constructor(private authenticationService: AuthenticationService, private activatedRoute: ActivatedRoute) { 
  }

  get password() {
    return this.passwordResetForm.get('password');
  }
  get confirmPassword() {
    return this.passwordResetForm.get('confirmPassword');
  }
  
  async onSubmit() {
    if (this.passwordResetForm.valid) {
      const firstParam: string = this.activatedRoute.snapshot.queryParamMap.get('token')!;
      console.log(firstParam)
      this.authenticationService.confirmReset(firstParam, this.password?.value!)
      .then(res => {
        this.passwordChangeResponseModalTitle = 'Success!';
        this.passwordChangeResponseModalMessage = 'Your password was changed.';
        this.showPasswordChangeResponseModal = true;
      })
      .catch(err => {
        this.passwordChangeResponseModalTitle = 'Error!';
        this.passwordChangeResponseModalMessage = 'Something went wrong. Try requesting a password change again.';
        this.showPasswordChangeResponseModal = true;
      });
    } else {
      this.errorMessage = 'Invalid data. Try again.'
    }
  }

  goHome(): void {
    window.location.href = '/';
  }

  closePasswordChangeResponseModal(): void {
    if (this.passwordChangeResponseModalTitle === 'Success!') window.location.href = '/';
    this.passwordChangeResponseModalTitle = '';
    this.passwordChangeResponseModalMessage = '';
    this.showPasswordChangeResponseModal = false;
  }
}
