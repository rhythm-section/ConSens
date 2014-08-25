class DevicesController < ApplicationController

  def index
    @devices = Device.all

    unless @devices.nil?
      respond_to do |format|
        format.html { render :index }
      end
    end
  end

  def show
    @device = Device.find(params[:id])

    start_datetime = {}
    unless @device.locations.count == 0
      start_datetime[:locations] = @device.locations.order('timestamp ASC').first.timestamp
    end
    unless @device.activities.count == 0
      start_datetime[:activities] = @device.activities.order('timestamp ASC').first.timestamp
    end
    start_datetime[:system_settings] = @device.system_settings.order('timestamp ASC').first.timestamp
    start_datetime[:app_sessions] = @device.app_sessions.order('timestamp_start ASC').first.timestamp_start
    @start_datetime = Hash[ start_datetime.sort_by{ |key, value| value } ].values.first.strftime('%d.%m.%Y (%H:%M)')

    end_datetime = {}
    unless @device.locations.count == 0
      end_datetime[:locations] = @device.locations.order('timestamp ASC').last.timestamp
    end
    unless @device.activities.count == 0
      end_datetime[:activities] = @device.activities.order('timestamp ASC').last.timestamp
    end
    end_datetime[:system_settings] = @device.system_settings.order('timestamp ASC').last.timestamp
    end_datetime[:app_sessions] = @device.app_sessions.order('timestamp_start ASC').last.timestamp_start
    @end_datetime = Hash[ end_datetime.sort_by{ |key, value| value } ].values.last.strftime('%d.%m.%Y (%H:%M)')

    # Statistics
    @records_count = @device.app_sessions.count + @device.app_usages.count + @device.locations.count + @device.activities.count + @device.system_settings.count

    # Context: location
    clusters = @device.location_clusters
    @locations = @device.locations

    clusters_with_location_count = []
    clusters.each do |cluster|
      new_cluster = Hash[cluster.attributes]
      new_cluster['count'] = @locations.where(cluster: cluster.id).count

      clusters_with_location_count.push(new_cluster)
    end

    # Save location clusters for showing them on a map (with javascript)
    @clusters = clusters_with_location_count.sort_by!{ |location| location['count'] }.reverse!
    @locations_without_cluster = @locations.where(cluster: -1)
    gon.clusters = clusters_with_location_count

    unless @device.nil?
      respond_to do |format|
        format.html { render :show }
      end
    end
  end

  private
    def get_category_counts(app_usages)
      apps = {}
      app_usages.each do |app_usage|
        # unless unnecessary_apps.include?(app_usage.package_name)
          categroy = app_usage.app_category
          if apps.has_key?(categroy)
            apps[categroy] += 1
          else
            apps[categroy] = 1
          end
        # end
      end
      apps = Hash[ apps.sort_by{ |key, value| value }.reverse ]
    end

    def get_app_counts(app_usages)
      apps = app_usages.group(:package_name).count
      # apps = app_usages.group(:app_name).count
      apps = Hash[ apps.sort_by{ |key, value| value }.reverse ]
      # apps = {}
      # app_usages.each do |app_usage|
      #   package_name = app_usage.package_name
      #   if apps.has_key?(package_name)
      #     apps[package_name] += 1
      #   else
      #     apps[package_name] = 1
      #   end
      # end
      # apps = Hash[ apps.sort_by{ |key, value| value }.reverse ]
    end

    def get_activity_counts(activities)
      count = activities.group(:name).count
      count = Hash[ count.sort_by{ |key, value| value }.reverse ]
    end

    def get_trigger_apps(app_usages)
      apps = {}
      app_usages.order('app_session_id ASC').each do |app_usage|
        app_session_id = app_usage.app_session_id
        unless apps.has_key?(app_session_id)
          apps[app_session_id] = []
        end
          
        apps[app_session_id].push(app_usage.package_name)
      end

      trigger_apps = {}
      apps.each do |key, array|
        # Remove unnecessary apps
        loop do
          # puts array[0]
          if unnecessary_apps.include?(array[0])
            array.shift
          else
            break
          end
          # puts array[0]
        end

        package_name = array[0]
        
        unless package_name.nil?
          # puts package_name
          # puts '------------------------------------------------------------------------'
          # Count trigger apps
          if trigger_apps.has_key?(package_name)
            trigger_apps[package_name] += 1
          else  
            trigger_apps[package_name] = 1
          end
        end
      end
      trigger_apps = Hash[ trigger_apps.sort_by{ | key, value | value }.reverse ]
    end

    def get_apps_with_categories(app_usages)
      app_usages.select(:package_name, :app_category)
    end

    # new import_backup rake task should avoid these duplicates
    def remove_duplicate_app_sessions(app_sessions)
      duplicates = app_sessions.group_by{|model| [model.timestamp_start,model.timestamp_end] }
      removed_usages = 0
      removed_sessions = 0
      duplicates.values.each do |duplicate|
        first_one = duplicate.shift
        # puts first_one.inspect
        # puts "Behalte #{first_one.id}"
        duplicate.each do|double|
          # puts double.inspect
          removed_sessions += 1
          apps = AppUsage.where(app_session_id: double.id)
          # puts "Lösche #{double.id}"
          removed_usages += apps.count
          double.destroy
        end
      end
      # puts "#{removed_sessions} Sessions gelöscht"
      # puts "#{removed_usages} Apps gelöscht"
    end

end