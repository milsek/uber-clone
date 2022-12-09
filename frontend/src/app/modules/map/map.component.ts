import { Component, AfterViewInit } from '@angular/core';
import * as L from 'leaflet';
import 'leaflet-routing-machine';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
})
export class MapComponent implements AfterViewInit {
  private map: any;
  private control: any;
  public chosenRoute: any;

  private initMap(): void {
    L.Marker.prototype.options.icon = L.icon({
      iconUrl: './assets/icons/pinpoint-marker.png',
      iconSize:     [30, 45],
      iconAnchor:   [15, 45],
    });
  
    this.map = L.map('map', {
      center: [ 45.254326, 19.827178 ],
      zoom: 15
    });

    const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      minZoom: 3,
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    });
    tiles.addTo(this.map);

    this.map.on('click', this.addMarker); 

    const that = this;
    this.control = L.Routing.control({
      lineOptions: {styles: [{color: '#006D5B', weight: 4}], extendToWaypoints: true, missingRouteTolerance: 0.1},
      altLineOptions: {styles: [{color: '#8aa1ad', weight: 7}], extendToWaypoints: true, missingRouteTolerance: 0.1},
      showAlternatives: true,
      addWaypoints: true,
      waypoints: [],
      autoRoute: true,
      routeWhileDragging: true,
      plan: L.Routing.plan([], {
        draggableWaypoints: true,
        routeWhileDragging: true,
        createMarker: function(i, wp) {
          return L.marker(wp.latLng, {draggable: true}).on('contextmenu', function(e: any) { 
            that.removeMarker(wp);
          });
        }
      })
    })
    .on('routesfound', function(e) {
      that.chosenRoute = e.routes[0];
    })
    .on('routeselected', function(e) {
      that.chosenRoute = e.route;
    })
    .addTo(this.map);
    document.getElementById('map')!.style.cursor = 'crosshair';
  }

  constructor() { }

  ngAfterViewInit(): void {
    this.initMap();
  }

  addMarker = (e: any) => {
    this.control.setWaypoints([...this.control.getPlan().getWaypoints().filter((x: L.Routing.Waypoint) => x.latLng), e.latlng]);
  }

  removeMarker(wp: any): void {
    this.control.setWaypoints([...this.control.getPlan().getWaypoints().filter((x: L.Routing.Waypoint) => x !== wp)])
  }

  clearMarkers(): void {
    this.control.setWaypoints([]);
    this.chosenRoute = null;
  }

  drawRoute(route: L.Routing.IRoute): void {
    L.Routing.line(route, {styles: [{color: '#006D5B', weight: 4}], extendToWaypoints: true, missingRouteTolerance: 0.1}).addTo(this.map);
    route.waypoints?.forEach((e : any) => {
      L.marker(e.latLng).addTo(this.map);
    });
  }

}
