import { Component, Input, OnInit } from '@angular/core';
import { faEnvelope, faMobileRetro, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { PhotoService } from 'src/app/core/http/user/photo.service';
import { Admin } from 'src/app/shared/models/admin.model';

@Component({
  selector: 'app-admin-details',
  templateUrl: './admin-details.component.html',
})
export class AdminDetailsComponent implements OnInit {
  @Input() admin!: Admin;
  userImage: string = '';

  faMobileRetro: IconDefinition = faMobileRetro;
  faEnvelope: IconDefinition = faEnvelope;

  constructor(private photoService: PhotoService) { }

  ngOnInit(): void {
    this.loadImage();
  }

  loadImage(): void {
    if (this.admin.profilePicture) {
      this.photoService.loadImage(this.admin.profilePicture)
      .then((response) => {
        this.userImage = response.data;
      });
    }
  }

}
