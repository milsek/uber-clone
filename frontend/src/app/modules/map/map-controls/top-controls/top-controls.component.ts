import { Component, HostListener, Input, OnInit } from '@angular/core';
import { IconDefinition, faBars, faArrowRightToBracket, faUser, faPowerOff } from '@fortawesome/free-solid-svg-icons';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { DriverService } from 'src/app/core/http/user/driver.service';

interface SharedInfo {
  accountType: string;
}

interface DriverInfo {
  isActive: boolean | null;
}

@Component({
  selector: 'app-top-controls',
  templateUrl: './top-controls.component.html',
})
export class TopControlsComponent implements OnInit {
  @Input() sharedInfo!: SharedInfo;
  @Input() driverInfo!: DriverInfo;

  faBars: IconDefinition = faBars;
  faArrowRightToBracket: IconDefinition = faArrowRightToBracket;
  faUser: IconDefinition = faUser;
  faPowerOff: IconDefinition = faPowerOff;

  showProfileDropdown: boolean = false;
  clickedProfileDropdown: boolean = false;

  constructor(private driverService: DriverService, private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
  }

  toggleActivity = (): void => {
    this.driverInfo.isActive = !this.driverInfo.isActive;
    this.driverService.toggleActivity();
  }

  logout(): void {
    this.authenticationService.logout();
    window.location.href="/";
  }
  
  @HostListener('document:click')
  clickout() {
    if (this.showProfileDropdown && !this.clickedProfileDropdown) {
      this.showProfileDropdown = false;
    }
    this.clickedProfileDropdown = false;
  }
  
}
