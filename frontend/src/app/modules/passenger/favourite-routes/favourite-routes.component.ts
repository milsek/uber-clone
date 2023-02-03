import { Component, OnInit } from '@angular/core';
import {
  IconDefinition,
  faChevronLeft,
} from '@fortawesome/free-solid-svg-icons';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import * as GeoSearch from 'leaflet-geosearch';
import { Route } from 'src/app/shared/models/route.model';
import { PassengerService } from 'src/app/core/http/user/passenger.service';

const service_url = 'https://nominatim.openstreetmap.org/reverse?format=json';
const API_KEY = null;

@Component({
  selector: 'app-favourite-routes',
  templateUrl: './favourite-routes.component.html',
})
export class FavouriteRoutesComponent implements OnInit {
  faChevronLeft: IconDefinition = faChevronLeft;
  elemNumber: number = 0;
  numberOfElements: number = 0;
  disableNext: boolean = false;
  disablePrev: boolean = true;
  page: number = 0;
  selectedRoute: Route | null = null;

  private map: any;
  private control: any;
  private provider!: GeoSearch.OpenStreetMapProvider;

  constructor(private passengerService: PassengerService) {}

  ngOnInit(): void {
    setTimeout(() => {
      this.initMap();
      this.getRoute();
    }, 10);
  }

  private initMap(): void {
    L.Marker.prototype.options.icon = L.icon({
      iconUrl: './assets/icons/alternative-marker.png',
      iconSize: [30, 45],
      iconAnchor: [15, 45],
    });

    this.map = L.map('map', {
      center: [45.254326, 19.827178],
      zoom: 13,
    });

    const tiles = L.tileLayer(
      'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        maxZoom: 19,
        minZoom: 3,
        attribution:
          '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      }
    );
    tiles.addTo(this.map);

    this.provider = new GeoSearch.OpenStreetMapProvider();

    this.control = L.Routing.control({
      lineOptions: {
        styles: [{ color: '#ff7035', weight: 4 }],
        extendToWaypoints: true,
        missingRouteTolerance: 0.1,
      },
      showAlternatives: false,
      addWaypoints: false,
      waypoints: [],
      autoRoute: true,
      routeWhileDragging: true,
      useZoomParameter: true,
    }).addTo(this.map);
  }

  getRoute(): void {
    this.passengerService.getFavouriteRoute(this.page).then((res) => {
      if (res.data.content.length === 0) {
        this.selectedRoute = null;
        this.elemNumber = 0;
        this.numberOfElements = 0;
        this.disableNext = true;
        this.disablePrev = true;
        return;
      }
      this.selectedRoute = res.data.content[0];
      this.elemNumber = res.data.number;
      this.numberOfElements = res.data.totalElements;
      if (res.data.last) {
        this.disableNext = true;
      } else {
        this.disableNext = false;
      }
      if (res.data.first) {
        this.disablePrev = true;
      } else {
        this.disablePrev = false;
      }
      this.control.setWaypoints([
        this.selectedRoute!.waypoints[0],
        this.selectedRoute!.waypoints[1],
      ]);
    });
  }

  next(): void {
    this.page++;
    this.getRoute();
  }

  prev(): void {
    this.page--;
    this.getRoute();
  }

  orderRouteAgain(): void {
    this.passengerService.setTemporaryRoute(this.selectedRoute);
    window.location.href = '/';
  }

  goHome(): void {
    window.location.href = '/';
  }
}
