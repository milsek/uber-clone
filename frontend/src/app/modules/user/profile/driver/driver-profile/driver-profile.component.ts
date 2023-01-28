import { Component, Input, HostListener } from '@angular/core';
import { ActivatedRoute  } from '@angular/router';
import { IconDefinition, faBars } from '@fortawesome/free-solid-svg-icons';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { Driver } from 'src/app/shared/models/driver.model';

enum DriverProfileView {
  Details = "details",
  Reviews = "reviews",
  Edit = 'edit'
}

@Component({
  selector: 'app-driver-profile',
  templateUrl: './driver-profile.component.html'
})
export class DriverProfileComponent {
  @Input() driver!: Driver;
  
  _selectedView: DriverProfileView = DriverProfileView.Details; 
  faBars: IconDefinition = faBars;
  showDropdown: boolean = false;
  clickedInDropdown: boolean = false;

  constructor(private authenticationService: AuthenticationService, private route: ActivatedRoute) { }

  set selectedView(value: string) {
    this._selectedView = value as DriverProfileView;
  }

  get selectedView() {
    return this._selectedView;
  }

  isOwnAccount(): boolean {
    if (window.location.href.includes('account')) return true;
    const session = this.authenticationService.getSession();
    if (session)
      return this.route.snapshot.paramMap.get('username') === session.username;
    return false;
  }

  @HostListener('document:click')
  clickout() {
    if (this.showDropdown && !this.clickedInDropdown) {
      this.showDropdown = false;
    }
    this.clickedInDropdown = false;
  }

}
