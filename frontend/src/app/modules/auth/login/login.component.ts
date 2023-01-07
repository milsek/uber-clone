import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { faChevronLeft, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
  faChevronLeft: IconDefinition = faChevronLeft;
  wrongCredentials: boolean = false;

  loginForm = new FormGroup({
    email: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)
    ]),
    password: new FormControl('', [
      Validators.required
    ]),
  });

  constructor(private authenticationService: AuthenticationService, private router: Router) { 
    document.getElementById('login-email')?.focus();
  }

  ngOnInit(): void {
  }

  async resetPassword() {
    return this.authenticationService.resetPasword(this.email?.value!);
  }

  async onSubmit() {
    const success = await this.authenticationService.login(this.email?.value!, this.password?.value!);
    if (success) {   
      this.authenticationService.whoami();
    }
    else {
      this.wrongCredentials = true;
    }
  }

  get email() {
    return this.loginForm.get('email');
  }
  get password() {
    return this.loginForm.get('password');
  }


}
