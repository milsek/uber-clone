import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
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

  passwordResetForm = new FormGroup({
    email: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)
    ]),
    password: new FormControl('', [
      Validators.required
    ]),
  });
  constructor(private authenticationService: AuthenticationService, private activatedRoute: ActivatedRoute) { 
  }

  get email() {
    return this.passwordResetForm.get('email');
  }
  get password() {
    return this.passwordResetForm.get('password');
  }
  
  async onSubmit() {
    const firstParam: string = this.activatedRoute.snapshot.queryParamMap.get('token')!;
    const success = await this.authenticationService.confirmReset(firstParam, this.password?.value!);
    alert("Password reset");
  }

  goHome(): void {
    window.location.href = '/';
  }
}
