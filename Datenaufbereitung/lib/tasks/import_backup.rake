# coding: utf-8

namespace :import_backup do
  desc "Import backups of your local ConSens databases (Input: JSON-File)."
  task :local => :environment do
    path = Rails.root.join("data", "import", "local")    
    device_folders = Dir[File.join(path, "*")].select{|file| File.ftype(file) == "directory"}

    device_folders.each do |device_folder|
      if device_folder != '/Users/Lukas/Desktop/consens_analyzer/data/import/local/thomas_kowar_a155def9d069ae73'
        next
      end

      read_local_backup(path, device_folder)
    end
  end

  desc "Import backups of your serverside ConSens database (Input: JSON-File)."
  task :server => :environment do
    read_server_backup( Rails.root.join("data", "import", "server", "devices.json") )
  end

  def clear_db
    Device.delete_all
    ActiveRecord::Base.connection.execute("DELETE from sqlite_sequence where name = 'devices'")
    Location.delete_all
    ActiveRecord::Base.connection.execute("DELETE from sqlite_sequence where name = 'locations'")
    Activity.delete_all
    ActiveRecord::Base.connection.execute("DELETE from sqlite_sequence where name = 'activities'")
    AppSession.delete_all
    ActiveRecord::Base.connection.execute("DELETE from sqlite_sequence where name = 'app_sessions'")
    AppUsage.delete_all
    ActiveRecord::Base.connection.execute("DELETE from sqlite_sequence where name = 'app_usages'")
    SystemSetting.delete_all
    ActiveRecord::Base.connection.execute("DELETE from sqlite_sequence where name = 'system_settings'")
  end

  def save_device_infos(device)
    device_info = {}

    device_info[:mongo_id] = device['_id']['$oid']
    device_info[:uuid] = device['uuid']
    device_info[:android_id] = device['system_infos']['android_id']
    device_info[:user_name] = device['system_infos']['user_name']
    device_info[:os_version] = device['system_infos']['os_version']
    device_info[:model] = device['system_infos']['model']
    device_info[:product] = device['system_infos']['product']
    device_info[:manufacturer] = device['system_infos']['manufacturer']
    device_info[:brand] = device['system_infos']['brand']
    device_info[:created_at] = device['created_at']['$date']
    device_info[:updated_at] = device['updated_at']['$date']

    # if device already exists: merge data
    existing_device = Device.where(android_id: device['system_infos']['android_id']).first

    if existing_device != nil
      existing_device.assign_attributes(device_info)
      @device = existing_device
    else
      @device = Device.new(device_info)
    end

    # create device and/or save device infos
    @device.save!
  end

  def save_locations(locations, device_id)
    # remove duplicate entries
    locations_no_duplicates = locations.uniq { |location| [ location['location_timestamp'] ] }
    puts "Removed #{locations.count - locations_no_duplicates.count} duplicates from JSON locations."

    if locations_no_duplicates != nil
      location_rows = []
      locations_no_duplicates.each_with_index do |user_location, index|
        # create new hash and rename backup attribute names to right column nams
        new_location_row = remove_and_rename_hash_keys(user_location, 'location_')

        # add device ID and remove certain columns
        new_location_row[:device_id] = device_id
        new_location_row = new_location_row.except('created_at', 'updated_at', :created_at, :updated_at)

        if Location.exists?(timestamp: new_location_row[:timestamp], device_id: device_id)
          puts "#{index}: Vorhanden."
        else
          puts "#{index}: Nicht vorhanden: #{new_location_row[:timestamp]} / #{device_id}"
          location_rows.push( Location.new(new_location_row) )
        end
      end
      Location.import(location_rows)
    end
  end

  def save_activities(activities, device_id)
    # remove duplicate entries
    activities_no_duplicates = activities.uniq { |activity| [ activity['activity_timestamp'] ] }
    puts "Removed #{activities.count - activities_no_duplicates.count} duplicates from JSON activities."

    if activities_no_duplicates != nil
      activity_rows = []
      activities_no_duplicates.each_with_index do |user_activity, index|
        # create new hash and rename backup attribute names to right column nams
        new_activity_row = remove_and_rename_hash_keys(user_activity, 'activity_')

        # add device ID and remove certain columns
        new_activity_row[:device_id] = device_id
        new_activity_row = new_activity_row.except('created_at', 'updated_at', :created_at, :updated_at)

        # puts new_activity_row.inspect
        if Activity.exists?(timestamp: new_activity_row[:timestamp], device_id: device_id)
          puts "#{index}: Vorhanden."
        else
          puts "#{index}: Nicht vorhanden: #{new_activity_row[:timestamp]} / #{new_activity_row[:activity_type]} / #{device_id}"
          activity_rows.push( Activity.new(new_activity_row) )
        end
      end
      Activity.import(activity_rows)
    end
  end

  def save_apps(app_sessions, app_usages, device_id)
    # remove duplicate entries
    app_sessions_no_duplicates = app_sessions.uniq { |app_session| [ app_session['app_session_timestamp_start'], app_session['app_session_timestamp_end'], app_session['server_sent'] ] }
    puts "Removed #{app_sessions.count - app_sessions_no_duplicates.count} duplicates from JSON app sessions."

    if app_sessions_no_duplicates != nil
      app_sessions_no_duplicates.each_with_index do |app_session, index|
        # create new hash and rename backup attribute names to right column nams
        new_session_row = remove_and_rename_hash_keys(app_session, 'app_session_')

        # add device ID and remove certain columns
        new_session_row[:device_id] = device_id
        new_session_row = new_session_row.except('created_at', 'updated_at', :created_at, :updated_at)

        app_session_id = index + 1

        new_session_row[:timestamp_start] = DateTime.parse(new_session_row[:timestamp_start])
        new_session_row[:timestamp_end] = DateTime.parse(new_session_row[:timestamp_end])
        new_session_row[:timestamp_duration] = DateTime.parse(new_session_row[:timestamp_duration])

        if AppSession.exists?(timestamp_start: new_session_row[:timestamp_start], timestamp_end: new_session_row[:timestamp_end], timestamp_duration: new_session_row[:timestamp_duration], device_id: device_id)
          puts "#{index}. Vorhanden."
        else
          puts "Nicht Vorhanden."
          puts "#{index}: Nicht vorhanden: #{new_session_row[:timestamp_start]} / #{new_session_row[:timestamp_end]} / #{device_id}"

          # temp variable to get id
          app_session_temp = AppSession.new( new_session_row )
          app_session_temp.save!

          # related data: app usages
          if app_usages != nil
            # select right apps (that belongs to current session id)
            app_usages_for_saved_app_session = app_usages.select { |app_usage| app_usage["app_session_id"].to_i == app_session_id }
            # puts app_usages_for_saved_app_session.inspect

            if app_usages_for_saved_app_session != []
              app_rows = []
              app_usages_for_saved_app_session.each do |app_usage|
                # add device ID and app session ID
                app_usage[:device_id] = device_id
                app_usage[:app_session_id] = app_session_temp.id

                new_app_row = app_usage.except('_id', 'server_sent', 'created_at', 'updated_at', :created_at, :updated_at)
                app_rows.push( AppUsage.new( new_app_row ) )
              end
              AppUsage.import(app_rows)
            end
          end

        end
      end
    end
  end


  def save_settings(system_settings, device_id)
    # remove duplicate entries
    system_settings_no_duplicates = system_settings.uniq { |system_setting| [ system_setting['settings_timestamp'] ] }
    puts "Removed #{system_settings.count - system_settings_no_duplicates.count} duplicates from JSON system settings."

    if system_settings_no_duplicates != nil
      settings_rows = []
      system_settings_no_duplicates.each_with_index do |system_setting, index|
        # create new hash and rename backup attribute names to right column nams
        new_settings_row = remove_and_rename_hash_keys(system_setting, 'settings_')

        # add device ID and remove certain columns
        new_settings_row[:device_id] = device_id
        new_settings_row = new_settings_row.except('created_at', 'updated_at', :created_at, :updated_at)

        if SystemSetting.exists?(timestamp: new_settings_row[:timestamp], device_id: device_id)
          puts "#{index}: Vorhanden."
        else
          puts "#{index}: Nicht vorhanden: #{new_settings_row[:timestamp]} / #{device_id}"
          settings_rows.push( SystemSetting.new(new_settings_row) )
        end
      end
      SystemSetting.import(settings_rows)
    end
  end

  def remove_and_rename_hash_keys(hash, string)
    new_hash = hash.except('_id', 'server_sent')
    new_hash.keys.each do |old_key|
      if old_key.start_with?(string) && !old_key.end_with?('type') && !old_key.end_with?('networkPreference')
        new_key = old_key.sub(string, '')
        new_hash[new_key.to_sym] = new_hash.delete(old_key)
      elsif old_key.end_with?('networkPreference')
        new_hash[:network_preference] = new_hash.delete(old_key)
      else
        new_key = old_key
        new_hash[new_key.to_sym] = new_hash.delete(old_key)
      end
    end
    return new_hash
  end

  def read_server_backup( path )
    json_contents = File.read(path)
    data = JSON.parse(json_contents)

    clear_db()

    if data.has_key?('mongoexport')
      data['mongoexport'].each do |device|
        
        if save_device_infos(device)
          puts "\nDevice #{@device.id} (#{@device.user_name}):"

          # related data: locations
          if device['user_locations'] != nil
            save_locations(device['user_locations'], @device.id)

            # statistics
            locations_count = Location.where(device_id: @device.id).count
            puts "#{locations_count} Standorte"
          end

          # related data: activities
          if device['user_activities'] != nil
            save_activities(device['user_activities'], @device.id)

            # statistics
            activities_count = Activity.where(device_id: @device.id).count
            puts "#{activities_count} Aktivitäten"
          end

          # related data: system settings
          if device['system_settings'] != nil
            save_settings(device['system_settings'], @device.id)

            # statistics
            system_settings_count = SystemSetting.where(device_id: @device.id).count
            puts "#{system_settings_count} Systemeinstellungen"
          end

          if device['app_sessions'] != nil && device['app_usages'] != nil
            save_apps(device['app_sessions'], device['app_usages'], @device.id)

            # statistics
            app_session_count = AppSession.where(device_id: @device.id).count
            app_usages_count = AppUsage.where(device_id: @device.id).count
            puts "#{app_session_count} App Sessions"
            puts "#{app_usages_count} Apps"
          end
        else
          break
        end
      end
    end
  end

  # removes all entries in hash between two dates
  # def remove_entries_date(data, before_date, after_date, column)
  #   new_data = []
  #   data.each do |entry|
  #     # check if outside date range
  #     if !entry[column].between?(before_date, after_date)
  #       new_data.push(entry)
  #     end
  #   end
  #   return new_data
  # end

  # def remove_entries_id()

  # end

  def read_local_backup(path, device_folder)
    file_names = ["locations", "activities", "app_session", "app_usage", "system_settings"]
    android_id = device_folder.split('_').last
    device = Device.where(android_id: android_id).first
    locations, activities, app_sessions, app_usages, system_settings = []

    # read JSON files
    file_names.each do |file_name|
      folder_path = path.join(device_folder)
      file_path = "#{folder_path}/#{file_name}.json"
      
      if File.file?(file_path)
        puts "Reading #{file_path}"
        json_contents = File.read(file_path)
        data = JSON.parse(json_contents)
        case file_name
        when 'locations'
          # locations = remove_entries_date(data, Location.order('timestamp ASC').first.timestamp, Location.order('timestamp ASC').last.timestamp, 'location_timestamp')

          # statistics
          unless Location.all.empty?
            locations_count = Location.where(device_id: device.id).count
            puts "#{locations_count} Standorte"
          end

          locations = data
          save_locations(locations, device.id)

          # statistics
          unless Location.all.empty?
            locations_count_new = Location.where(device_id: device.id).count
            puts "#{locations_count_new-locations_count} neue Datensätze hinzugefügt."
            puts "Alt: #{locations_count} Standorte"
            puts "Neu: #{locations_count_new} Standorte"
          end
        when 'activities'
          # activities = remove_entries_date(data, Activity.order('timestamp ASC').first.timestamp, Activity.order('timestamp ASC').last.timestamp, 'activity_timestamp')
          
          # statistics
          unless Activity.all.empty?
            activities_count = Activity.where(device_id: device.id).count
            puts "#{activities_count} Aktivitäten"
          end

          activities = data
          save_activities(activities, device.id)

          # statistics
          unless Activity.all.empty?
            activities_count_new = Activity.where(device_id: device.id).count
            puts "#{activities_count_new-activities_count} neue Datensätze hinzugefügt."
            puts "Alt: #{activities_count} Aktivitäten"
            puts "Neu: #{activities_count_new} Aktivitäten" 
          end
        when 'app_session'
          # app_sessions = remove_entries_date(data, AppSession.order('timestamp_start ASC').first.timestamp_start, AppSession.order('timestamp_start ASC').last.timestamp_start, 'app_session_timestamp_start')
          app_sessions = data
        when 'app_usage'
          # puts app_sessions.inspect

          # statistics
          unless AppSession.all.empty?
            app_session_count = AppSession.where(device_id: device.id).count
            puts "#{app_session_count} App Sessions"
          end

          unless AppUsage.all.empty?
            app_usages_count = AppUsage.where(device_id: device.id).count
            puts "#{app_usages_count} Apps"
          end

          app_usages = data
          save_apps(app_sessions, app_usages, device.id)

          # statistics
          unless AppSession.all.empty?
            app_session_count_new = AppSession.where(device_id: device.id).count
            puts "#{app_session_count_new-app_session_count} neue Datensätze hinzugefügt."
            puts "Alt: #{app_session_count} App Sessions"
            puts "Neu: #{app_session_count_new} App Sessions"
          end
          unless AppUsage.all.empty?
            app_usages_count_new = AppUsage.where(device_id: device.id).count
            puts "#{app_usages_count_new-app_usages_count} neue Datensätze hinzugefügt."
            puts "Alt: #{app_usages_count} Apps"
            puts "Neu: #{app_usages_count_new} Apps"  
          end
        when 'system_settings'
          # statistics
          unless SystemSetting.all.empty?
            system_settings_count = SystemSetting.where(device_id: device.id).count
            puts "#{system_settings_count} Systemeinstellungen"
          end

          system_settings = data
          # system_settings = remove_entries_date(data, SystemSetting.order('timestamp ASC').first.timestamp, SystemSetting.order('timestamp ASC').last.timestamp, 'settings_timestamp')
          # puts "Inserting #{system_settings.count} new system settings"
          save_settings(system_settings, device.id)

          # statistics
          unless SystemSetting.all.empty?
            system_settings_count_new = SystemSetting.where(device_id: device.id).count
            puts "#{system_settings_count_new-system_settings_count} neue Datensätze hinzugefügt."
            puts "Alt: #{system_settings_count} Systemeinstellungen"
            puts "Neu: #{system_settings_count_new} Systemeinstellungen"
          end
        end

        next
      else
        puts "There was an error reading the file #{file_path}"
        break
      end
    end

    puts "\nDevice #{device.id} (#{device.user_name}):"
    puts "\nFüge neue Datensätze hinzu..."
    
    # case file_name
    # when 'locations'
    #   # statistics
    #   locations_count = Location.where(device_id: device.id).count
    #   puts "#{locations_count} Standorte"

    #   # import new data
    #   save_locations(data, device.id)

    #   # statistics
    #   locations_count_new = Location.where(device_id: device.id).count
    #   puts "#{locations_count_new-locations_count} neue Datensätze hinzugefügt."
    #   puts "Alt: #{locations_count} Standorte"
    #   puts "Neu: #{locations_count_new} Standorte"
    # when 'activities'
    #   # statistics
    #   activities_count = Activity.where(device_id: device.id).count
    #   puts "#{activities_count} Aktivitäten"

    #   # import new data
    #   save_activities(data, device.id)

    #   # statistics
    #   activities_count_new = Activity.where(device_id: device.id).count
    #   puts "#{activities_count_new-activities_count} neue Datensätze hinzugefügt."
    #   puts "Alt: #{activities_count} Aktivitäten"
    #   puts "Neu: #{activities_count_new} Aktivitäten"      
    # # when 'app_session'
    # #   # statistics
    # #   app_session_count = AppSession.where(device_id: device.id).count
    # #   app_usages_count = AppUsage.where(device_id: device.id).count
    # #   puts "#{app_session_count} App Sessions"
    # #   puts "#{app_usages_count} Apps"

    # #   # import new data
    # #   save_apps(data, device.id)

    # #   # statistics
    # #   app_session_count_new = AppSession.where(device_id: device.id).count
    # #   app_usages_count_new = AppUsage.where(device_id: device.id).count

    # #   puts "#{app_session_count_new-app_session_count} neue Datensätze hinzugefügt."
    # #   puts "Alt: #{app_session_count} App Sessions"
    # #   puts "Neu: #{app_session_count_new} App Sessions"    

    # #   puts "#{app_usages_count_new-app_usages_count} neue Datensätze hinzugefügt."
    # #   puts "Alt: #{app_usages_count} Apps"
    # #   puts "Neu: #{app_usages_count_new} Apps"    
    # when 'system_settings'
    #   # statistics
    #   system_settings_count = SystemSetting.where(device_id: device.id).count
    #   puts "#{system_settings_count} Systemeinstellungen"

    #   # import new data
    #   save_settings(data, device.id)

    #   # statistics
    #   system_settings_count_new = SystemSetting.where(device_id: device.id).count
    #   puts "#{system_settings_count_new-system_settings_count} neue Datensätze hinzugefügt."
    #   puts "Alt: #{system_settings_count} Systemeinstellungen"
    #   puts "Neu: #{system_settings_count_new} Systemeinstellungen"
    # end
  end

end