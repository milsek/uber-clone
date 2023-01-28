import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';


@Component({
  selector: 'app-google-login',
  templateUrl: './google-login.component.html',
})
export class GoogleLoginComponent implements OnInit {
  constructor(private activatedRoute: ActivatedRoute, private router: Router, private authenticationService: AuthenticationService) { 
  }

  ngOnInit(): void {
    window.localStorage.setItem('token2', this.activatedRoute.snapshot.queryParamMap.get('token')!);
    this.authenticationService.whoami();
    this.router.navigate(['/']);
  }

}
