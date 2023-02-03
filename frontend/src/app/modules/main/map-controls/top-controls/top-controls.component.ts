import { Component, HostListener, Input, OnInit } from '@angular/core';
import {
  IconDefinition,
  faBars,
  faArrowRightToBracket,
  faUser,
  faPowerOff,
  faUserPlus,
  faUserEdit,
  faCircleXmark,
  faSearch,
  faStar,
  faRoute,
  faPieChart
} from '@fortawesome/free-solid-svg-icons';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { DriverService } from 'src/app/core/http/user/driver.service';
import { PhotoService } from 'src/app/core/http/user/photo.service';
import { DriverInfo } from 'src/app/shared/models/data-transfer-interfaces/driver-info.model';
import { Session } from 'src/app/shared/models/session.model';

@Component({
  selector: 'app-top-controls',
  templateUrl: './top-controls.component.html',
})
export class TopControlsComponent implements OnInit {
  accountType: string = this.authenticationService.getAccountType();
  @Input() driverInfo!: DriverInfo;

  faBars: IconDefinition = faBars;
  faArrowRightToBracket: IconDefinition = faArrowRightToBracket;
  faUser: IconDefinition = faUser;
  faPowerOff: IconDefinition = faPowerOff;
  faUserPlus: IconDefinition = faUserPlus;
  faUserEdit: IconDefinition = faUserEdit;
  faCircleXmark: IconDefinition = faCircleXmark;
  faSearch: IconDefinition = faSearch;
  faStar: IconDefinition = faStar;
  faRoute: IconDefinition = faRoute;
  faPieChart: IconDefinition = faPieChart;

  showActionsDropdown: boolean = false;
  clickedActionsDropdown: boolean = false;
  showProfileDropdown: boolean = false;
  showProfileDropdown2: boolean = false;
  clickedProfileDropdown: boolean = false;

  userImage: string = '';

  constructor(
    private driverService: DriverService,
    private authenticationService: AuthenticationService,
    private photoService: PhotoService
  ) {}

  ngOnInit(): void {
    this.loadImage();
  }

  toggleActivity = (): void => {
    this.driverInfo.isActive = !this.driverInfo.isActive;
    this.driverService.toggleActivity();
  };

  logout(): void {
    if (this.driverInfo.isActive) this.driverService.toggleActivity();
    this.authenticationService.logout();
    window.location.href = '/';
  }

  loadImage(): void {
    const session: Session | null = this.authenticationService.getSession();
    if (session && session.profilePicture) {
      this.photoService.loadImage(session.profilePicture)
      .then((response) => {
        this.userImage = response.data;
      });
    }
  }

  @HostListener('document:click')
  clickout() {
    if (this.showProfileDropdown && !this.clickedProfileDropdown) {
      this.showProfileDropdown = false;
    }
    this.clickedProfileDropdown = false;
    if (this.showActionsDropdown && !this.clickedActionsDropdown) {
      this.showActionsDropdown = false;
    }
    this.clickedActionsDropdown = false;
  }
}
