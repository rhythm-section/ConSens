// This is a manifest file that'll be compiled into application.js, which will include all the files
// listed below.
//
// Any JavaScript/Coffee file within this directory, lib/assets/javascripts, vendor/assets/javascripts,
// or vendor/assets/javascripts of plugins, if any, can be referenced here using a relative path.
//
// It's not advisable to add code directly here, but if you do, it'll appear at the bottom of the
// compiled file.
//
// Read Sprockets README (https://github.com/sstephenson/sprockets#sprockets-directives) for details
// about supported directives.
//
//= require jquery
//= require jquery_ujs
//= require turbolinks
//= require_tree .

var ready;

ready = function() {

  var map;
  // var locations = $('#locations').data('locations')
  var locations = gon.clusters;

  init_map = function() {
    console.log(locations.length);
    // console.log(locations);

    var mapOptions = {
      zoom: 8,
      center: new google.maps.LatLng(47.770364, 14.128511)
    };

    map = new google.maps.Map($('#map-canvas')[0], mapOptions);

    set_map_markers();
  }

  set_map_markers = function() {
    // console.log(locations);
    var markers = new Array();

    for(var i = 0; i < locations.length; i++) {
      console.log(locations[i].count)
      position = new google.maps.LatLng(locations[i].latitude, locations[i].longitude);

      output_map_link(locations[i].latitude, locations[i].longitude, locations[i].address);

      markers[i] = new google.maps.Marker({
        position: position,
        map: map,
        icon: 'http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=' + locations[i].id + '|' + Array(7).join(locations[i].device_id) + '|EEEEEE',
        title: 'Device: ' + locations[i].device_id + ' / ' + locations[i].count + 'Orte'
      });
    }
  }

  output_map_link = function(latitude, longitude, address) {
    console.log(latitude + ', ' + longitude)
    console.log('<a href="http://maps.google.com/maps?z=10&q=' + latitude + ',' + longitude + '&mrt=yp">' + address + '</a>')
  }

  init_map();

};

$(document).ready(ready);
$(document).on('page:load', ready);