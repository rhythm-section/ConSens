# coding: utf-8

require 'algorithms/cluster'
require 'algorithms/clusterer'
require 'algorithms/data_point'

namespace :location do
  desc "Add Clusters to locations."
  task :add_clusters_to_locations => :environment do
    distance = [0.1, 0.2, 0.3, 0.5, 1, 3, 5, 10, 30, 50] # 100m, 200m, 300m, 500m, 1km, 3km, 5km, 10km, 30km, 50km

    def add_clusters(device, clusters, locations, current_distance)
      puts "Device #{device.id}:"
      puts "Distance: #{current_distance}"

      clusters.each do |cluster|
        center_point = [cluster.latitude, cluster.longitude]
        box = Geocoder::Calculations.bounding_box(center_point, current_distance)
        nearby_locations = Location.within_bounding_box(box)

        # puts "Cluster: #{cluster.id} / #{cluster.latitude} / #{cluster.longitude}"
        # puts "#{nearby_locations.count} nearby locations."
        # puts "#{nearby_locations.count} Nearby Locations: #{nearby_locations.inspect}"

        # nearby_locations.where(device_id: device.id).update_all(cluster: cluster.id, cluster_latitude: cluster.latitude, cluster_longitude: cluster.longitude)
        nearby_locations.where('device_id = ? AND cluster = ?', device.id, -1).update_all(cluster: cluster.id)
      end

      puts '--------------------------------------------------------------------------------'
    end

    # before adding cluster IDs to locations, set standard value (-1)
    Location.all.update_all(cluster: -1)

    devices = Device.all

    devices.each do |device|
      # get all clusters & locations for current device
      clusters = LocationCluster.where(device_id: device.id)
      locations = Location.where(device_id: device.id)

      # set distance to begin with
      current_distance_index = -1
      
      unless clusters.nil?
        loop do
          # get locations for current device
          remaining_locations = locations.where(cluster: -1)
          puts "#{remaining_locations.count} remaining locations."
          
          # as long as there are locations without cluster ID and max distance is not reached
          current_distance_index += 1
          current_distance = distance[current_distance_index]
          unless remaining_locations.count == 0 || current_distance_index >= 10
            add_clusters(device, clusters, remaining_locations, current_distance)
            current_distance = distance[current_distance_index]
          else
            remaining_locations = locations.where(cluster: -1)
            puts '================================================================================'
            break
          end
        end
        puts 'BREAK'
        puts '================================================================================'
      end
    end

    remaining_locations = Location.where(cluster: -1)
    puts "Remaining locations where no cluster was found (all devices): #{remaining_locations.count}"
  end

  desc "Reduce clusters."
  task :reduce_clusters => :environment do
    grouped = LocationCluster.all.group_by{ |cluster| [cluster.device_id, cluster.address] }

    grouped.values.each do |duplicates|
      # keep first entry
      first_one = duplicates.shift

      # only remove dupliactes if cluster has address
      unless first_one.address.nil?
        # remove duplicates
        duplicates.each{ |double| double.destroy }

        puts "Removed duplicates of address: #{first_one.address}:"
        puts duplicates.inspect
        puts '--------------------------------------------------------------------------------'
      end
    end
  end

  desc "Calculate location clusters (k-Means)."
  task :make_clusters => :environment do
    include Algorithms::KMeans

    def remove_unnecessary_apps(app_usages)
      unnecessary_apps = [
        'at.lukasmayerhofer.consens',
        'com.android.launcher',                             # launcher
        'com.android.systemui',
        'com.cyanogenmod.trebuchet',                        # launcher
        'com.google.android.googlequicksearchbox',
        'com.htc.launcher',                                 # launcher
        'com.nidhoeggr.stressdetector.main',
        'com.nidhoeggr.thestresscollector',
        'thesis.dumb.alert'
      ]

      cleaned_app_usages = []
      app_usages.each do |app_usage|
        if unnecessary_apps.include?(app_usage.package_name)
           next
        else
          cleaned_app_usages.push(app_usage)
        end
      end
      return cleaned_app_usages
    end

    def search_location(device_id, timestamp_start, timestamp_end)
      location = Location.where("device_id = ? AND timestamp BETWEEN ? AND ?", device_id, timestamp_start, timestamp_end).order("timestamp ASC").first
      if location.nil?
        location = Location.where("device_id = ? AND timestamp <= ?", device_id, timestamp_end).first
      end
      return location
    end

    def calculate_and_write_clusters(device_id, locations)
      # rule of thumb (http://en.wikipedia.org/wiki/Determining_the_number_of_clusters_in_a_data_set)
      cluster_count = Math.sqrt(locations.count / 2).round
      puts "Cluster Count: #{cluster_count}"

      cluster_data = {
        num_clusters: cluster_count,
        filename: '',
        data_points: []
      }

      puts "Count before: #{locations.count}"
      locations.each_with_index do |location, index|
        # if location.location_accuracy > 25
        #   next
        # end
        unless location.nil?
          cluster_data[:data_points].push( DataPoint.new(latitude: location.latitude, longitude: location.longitude) )
        end
      end
      puts "Count after: #{cluster_data[:data_points].count}"

      # calculate k-means to get location clusters
      clusterer = Clusterer.new(cluster_data)
      clusterer.run
      locations_with_cluster_id = clusterer.to_chart_data
      # cluster_centroids = clusterer.get_cluster_centroids

      puts locations_with_cluster_id.inspect

      locations_with_cluster_id.each do |location|
        cluster = {
          latitude: location[0],
          longitude: location[1],
          device_id: device_id
        }

        # save new cluster
        new_cluster = LocationCluster.new(cluster).save!

        # add cluster id to corresponding locations
        Location.where("latitude = ? AND longitude = ? AND device_id = ?", location[0], location[1], device_id).update_all(cluster: location[2])
      end

      # cluster_centroids.each do |location|
      #   # puts location.inspect
      #   # puts "Location (for Device #{device_id}): #{location[0]}/#{location[1]}/#{location[2]}"
      #   # cluster = {
      #   #   latitude: location[0],
      #   #   longitude: location[1],
      #   #   device_id: device_id
      #   # }
      #   cluster = {
      #     latitude: location.latitude,
      #     longitude: location.longitude,
      #     device_id: device_id
      #   }
      #   # puts cluster.inspect
      #   LocationCluster.new(cluster).save!
      #   # Location.where("latitude = ? AND longitude = ? AND device_id = ?", location[0], location[1], device_id).update_all(cluster: location[2])
      # end
    end

    # LocationCluster.where(device_id: 5).destroy_all

    # LocationCluster.delete_all()
    # ActiveRecord::Base.connection.execute("DELETE from sqlite_sequence where name = 'location_clusters'")
    # Location.all.update_all(cluster: -1)
    devices = Device.all

    devices.each do |device|
      puts "Device #{device.id} (#{device.user_name}):"

      if device.id != 8
        next
      end

      app_usages = AppUsage.where(device_id: device.id)
      app_usages = remove_unnecessary_apps(app_usages)

      app_usages_count = app_usages.count
      puts "#{app_usages_count} app usages"

      locations = []

      app_usages.each_with_index do |app_usage, index|
        # get corresponding app session
        app_session = AppSession.find_by_id(app_usage.app_session_id)

        # determine timestamps for searches
        timestamp_start = app_session.timestamp_start.strftime('%Y-%m-%dT%H:%M:%S')
        timestamp_end = app_session.timestamp_end.strftime('%Y-%m-%dT%H:%M:%S')

        # search corresponding location
        if Location.where(device_id: device.id).nil?
          break
        else
          location = search_location(device.id, timestamp_start, timestamp_end)
          if !location.nil?
            puts "#{index}/#{app_usages_count}: #{location.timestamp}"
          end
          locations.push( location )
        end
      end

      if !locations.nil?
        puts "#{locations.count} locations"
        calculate_and_write_clusters(device.id, locations)
      end
    end
  end
end