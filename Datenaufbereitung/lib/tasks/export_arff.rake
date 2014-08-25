# coding: utf-8

desc "Create Weka-compatible ARFF-File."
task :export_arff => :environment do
  ACTIVITY_CONFIDENCE = 75
  TRAINING_CLASSIFICATION_SPLIT = 0.9     # Percentage of all data (80%)

  @unnecessary_apps = [
    # 'android',
    'com.android.launcher',
    'com.android.systemui',
    'com.cyanogenmod.trebuchet',
    # 'com.coverscreen.cover',
    'com.google.android.googlequicksearchbox',
    'com.htc.launcher',
  ]

  # Temp variables for saving features
  arff_data = []
  arff_instance = {}

  # Get device data
  devices = Device.all

  # Iterate through devices
  devices.each do |device|
    puts Time.now.strftime("%d/%m/%Y %H:%M")
    # 1, 3, 4,  
    if device.id != 4
      next
    end

    puts "Device #{device.id}: #{device.user_name}"

    # Remove unnecessary apps
    app_usages = remove_unnecessary_apps(device.app_usages)

    puts "App usages: #{app_usages.count}"

    app_usages.each_with_index do |app_usage, index|
      puts "Create data instance #{index+1}"
      # if index >= 500
      #   break
      # end

      # Create and push instance to ARFF data array
      arff_instance = create_arff_instance(device, app_usages, app_usage)
      arff_data.push(arff_instance)
    end

    # 80% of data = training-set / 20% of data = test-set
    training_classification_split = (arff_data.count * TRAINING_CLASSIFICATION_SPLIT).round

    # get app_session_id of app_usage where to split the data (not exactly 80% training becaus there are many apps in one app session but we want to split on beginning of an app session and not at the beginning of one app)
    split_app_session_id = app_usages[training_classification_split].app_session_id
    puts "Splitting data on index #{training_classification_split} and session id #{split_app_session_id}"

    # Define ARFF file names
    full_path = Rails.root.join('data', 'export', 'arff', "device_#{device.id}_full.arff")
    training_path = Rails.root.join('data', 'export', 'arff', "device_#{device.id}_training.arff")
    test_path = Rails.root.join('data', 'export', 'arff', "device_#{device.id}_test.arff")

    # Write ARFF
    split_data = false
    header = true

    puts "Writing data (Training/Test)..."
    arff_data.each do |instance|
      instance.each_with_index do |(key, value), attribute_index|
        # Check if end of training data and begin of test data
        if key == :app_session_id
          if value < split_app_session_id
            split_data = false
          else
            split_data = true
          end
        else
          if split_data
            # puts "Writing test-data: #{value} / #{split_app_session_id}"

            File.open(test_path, 'a') do |f|
              if attribute_index == instance.count - 1
                f << value << "\n"
              else
                f << value << ', '
              end
            end
          else
            # puts "Writing training-data: #{value} / #{split_app_session_id}"

            if header
              File.open(training_path, 'w') do |f|
                write_arff_header(f, device, app_usages)
                header = false
              end

              File.open(test_path, 'w') do |f|
                write_arff_header(f, device, app_usages)
                header = false
              end
            end

            File.open(training_path, 'a') do |f|
              if attribute_index == instance.count - 1
                f << value << "\n"
              else
                f << value << ', '
              end
            end
          end # if split_data
        end # if key == :app_session_id
      end # instance
    end # arff_data

    puts "Writing data (Full)..."
    File.open(full_path, 'w') do |f|
      write_arff_header(f, device, app_usages)
    end
    arff_data.each do |instance|
      instance.each_with_index do |(key, value), attribute_index|
        unless key == :app_session_id
          File.open(full_path, 'a') do |f|
            if attribute_index == instance.count - 1
              f << value << "\n"
            else
              f << value << ', '
            end
          end
        end
      end # instance
    end # arff_data
    puts Time.now.strftime("%d/%m/%Y %H:%M")
  end
end

# Clean up app usage
def remove_unnecessary_apps(app_usages)
  cleaned_app_usages = []
  app_usages.each do |app_usage|
    if @unnecessary_apps.include?(app_usage.package_name)
      next
    else
      cleaned_app_usages.push(app_usage)
    end
  end
  return cleaned_app_usages
end

# Create header for ARFF-File
def write_arff_header(f, device, app_usages_current_device)
  # cluster_size = Location.where(device_id: device.id).maximum(:cluster)
  location_label_names = LocationCluster.uniq.pluck(:label).map(&:inspect).join(', ')

  package_names_array = []
  app_categories_array = []
  system_settings_array = []

  location_clusters = LocationCluster.where(device_id: device.id)
  location_cluster_array = []
  location_clusters.each_with_index do |cluster, index|
    location_cluster_array.push(cluster.id)
  end
  location_cluster_ids = location_cluster_array.map(&:inspect).join(', ')

  system_settings_for_device = SystemSetting.where(device_id: device.id)
  system_settings_array = system_settings_for_device.pluck(:wifi_ssid).uniq
  system_settings_array.each do |system_settings_item|
    system_settings_item.gsub!('"', '')
    system_settings_item.gsub!('<', '')
    system_settings_item.gsub!('>', '')
  end
  system_settings_array.uniq!
  system_settings = system_settings_array.map(&:inspect).join(', ')
  system_settings.gsub!('"-1", ', '')

  app_usages_current_device.each do |app_usage|
    package_names_array.push(app_usage.package_name)
    app_categories_array.push(app_usage.app_category)
  end

  package_names_array.uniq!
  package_names = package_names_array.map(&:inspect).join(', ')
  app_categories = app_categories_array.uniq!.map(&:inspect).join(', ')

  f << "% ARFF file for data from the ConSens app\n%\n"
  f << "@relation consens\n\n"

  
  f << "@attribute day_of_week numeric\n"
  f << "@attribute hour_of_day numeric\n"
  f << "@attribute weekend { 0, 1 }\n"

  # f << "@attribute location_cluster { " << (1..cluster_size).map(&:inspect).join(', ') << " }\n"
  f << "@attribute location_cluster { " << location_cluster_ids << " }\n"
  f << "@attribute location_label { " << location_label_names << " }\n"

  f << "@attribute activity_name { 'Unkown', 'Tilting', 'Still', 'On Foot', 'On Bicycle', 'In Vehicle' }\n"
  f << "@attribute activity_type { 0, 1, 2, 3, 4, 5 }\n"

  package_names_array.each do |package_name|
    f << "@attribute #{package_name} { 0, 1 }\n"
  end
  f << "@attribute trigger_app { " << package_names << " }\n"
  f << "@attribute last_used_app { " << package_names << " }\n"

  f << "@attribute settings_airplane_mode { 0, 1 }\n"
  f << "@attribute settings_bluetooth { 0, 1 }\n"
  f << "@attribute settings_mode_ringer { 0, 1, 2 }\n"
  f << "@attribute settings_volume_alarm numeric\n"
  f << "@attribute settings_volume_music numeric\n"
  f << "@attribute settings_volume_notification numeric\n"
  f << "@attribute settings_volume_ring numeric\n"
  f << "@attribute settings_volume_system numeric\n"
  f << "@attribute settings_volume_voice numeric\n"
  f << "@attribute settings_wifi { 0, 1 }\n"
  f << "@attribute settings_wifi_ssid { " << system_settings << " }\n"

  f << "@attribute package_name { " << package_names << " }\n"
  f << "@attribute app_category { " << app_categories << " }\n"

  f << "\n@data\n"
  f << "%\n% #{app_usages_current_device.count} instances\n%\n"
end

# Create data instances for ARFF-File
def create_arff_instance(device, app_usages_current_device, app_usage)
  arff_instance = {}

  # Add app session id
  arff_instance[:app_session_id] = app_usage.app_session_id

  # Get app session timestamps of current app usage
  app_session = AppSession.find_by_id(app_usage.app_session_id)
  
  # Create time variables to search for attributes that belong to current app
  timestamp_start = app_session.timestamp_start
  timestamp_start_string = app_session.timestamp_start.strftime('%Y-%m-%dT%H:%M:%S')
  
  timestamp_end = app_session.timestamp_end
  timestamp_end_string = app_session.timestamp_end.strftime('%Y-%m-%dT%H:%M:%S')

  # Set instance attributes (context: time)
  if timestamp_start.nil? && timestamp_end.nil?
    arff_instance[:day_of_week] = '?'
    arff_instance[:hour_of_day] = '?'
    arff_instance[:weekend] = '?'
  else
    arff_instance[:day_of_week] = timestamp_start.wday
    arff_instance[:hour_of_day] = timestamp_start.hour
    arff_instance[:weekend] = (timestamp_start.wday == 0 || timestamp_start.wday == 6) ? 1 : 0
  end

  # Set instance attributes (context: location)
  location = device.locations.where("timestamp BETWEEN ? AND ?", timestamp_start_string, timestamp_end_string).order("timestamp ASC").first
  if location.nil?
    location = device.locations.where("timestamp <= ?", timestamp_end_string).order("timestamp ASC").last
  end

  if location.nil?
    arff_instance[:location_cluster] = '?'
    arff_instance[:location_label] = '?'
  else
    arff_instance[:location_cluster] = (location.cluster.present? && location.cluster != -1) ? "#{location.cluster}" : '?'

    if LocationCluster.find_by_id(location.cluster).nil?
      arff_instance[:location_label] = '?'
    else
      arff_instance[:location_label] = "'#{LocationCluster.find_by_id(location.cluster).label}'"
    end
  end
  # puts "Location: #{arff_instance[:location_cluster]}"

  # Set instance attribute (context: activity)
  activity = Activity.where("device_id = ? AND timestamp BETWEEN ? AND ?", device.id, timestamp_start_string, timestamp_end_string).order("timestamp ASC").first
  if activity.nil?
    activity = device.activities.where("timestamp <= ?", timestamp_end_string).order("timestamp ASC").last
  end

  if !activity.nil? && (activity.confidence >= ACTIVITY_CONFIDENCE)
    arff_instance[:activity_name] = (activity.name.present?) ? "'#{activity.name}'" : '?'
    arff_instance[:activity_type] = (activity.activity_type.present?) ? "#{activity.activity_type}" : '?'
  else
    arff_instance[:activity_name] = '?'
    arff_instance[:activity_type] = '?'
  end
  # puts "Activity: #{arff_instance[:activity_name]}/#{arff_instance[:activity_type]}"

  # Set instance attribute (context: all_apps)
  apps = device.app_usages.where("app_session_id = ?", app_session.id).order("id ASC")
  package_names_array = []
  app_usages_current_device.each do |app_usage|
    package_names_array.push(app_usage.package_name)
  end

  if apps.nil?
    package_names_array.each do |package_name|
      arff_instance[package_name] = '0'
      # puts "#{package_name}: #{arff_instance[package_name]}"
    end
  else
    session_app_names_array = apps.pluck(:app_name).uniq
    session_app_names = session_app_names_array.map(&:inspect).join(', ')

    package_names_array.each do |package_name|
      package_name.gsub!('"', '')
      # Just apps that were used before the current app (in the same session)
      arff_instance[package_name] = (apps.any? { |hash_app_usage| (hash_app_usage.package_name == package_name) && (hash_app_usage.id != app_usage.id) && (hash_app_usage.id <= app_usage.id) }) ? '1' : '0'          # http://stackoverflow.com/questions/1514883/determine-if-a-value-exists-in-an-array-of-hashes
      # puts "#{package_name}: #{arff_instance[package_name]}"
    end
  end

  # Set instance attribute (context: last_used_app)
  unless app_usage.nil?
    # if app_usage.id == 0
    #   arff_instance[:last_used_app] = '?'
    #   arff_instance[:trigger_app] = app_usage.package_name
    # else

    # get trigger app (first app) of app_session
    session_apps = device.app_usages.where(app_session_id: app_session.id).order("id ASC")
    trigger_app = remove_unnecessary_apps(session_apps).first
    if trigger_app.nil?
      arff_instance[:trigger_app] = '?'
    else
      arff_instance[:trigger_app] = "'#{trigger_app.package_name}'"
    end
    # puts "Current Session: #{app_session.id}"
    # puts "Trigger-App: #{trigger_app.package_name}"
    
    # lookback variable for searching last used app
    lookback = 1

    # puts "Current App: #{app_usage.package_name}"
    # last_used_app = session_apps.where("app_session_id = ? AND id = ?", app_session.id, app_usage.id - 1).first

    # if last_used_app.nil? # there is no app before current app in this session
    #   puts "No last used App"
    # else
    #   puts "Last Used App: #{last_used_app.package_name}"
    # end

    loop do
      # loop until last_used_app is not one of the unnecessary apps and the begin of the app_session is not reached
      # puts "Lookup ID: #{app_usage.id - lookback}"
      last_used_app = device.app_usages.where("app_session_id = ? AND id = ?", app_session.id, app_usage.id - lookback).first

      if last_used_app.nil? # there is no app before current app in this session
        arff_instance[:last_used_app] = '?'
        # puts "No last used App"
        break
      elsif @unnecessary_apps.include?(last_used_app.package_name)
        lookback += 1
        # puts "Not this one: #{last_used_app.package_name} => Lookback: #{lookback}"
        next
      else
        arff_instance[:last_used_app] = "'#{last_used_app.package_name}'"
        # puts "Last Used App: #{last_used_app.package_name}"
        break
      end
    end
    # puts "------------------------------------------------------------------------"
  end

  # Set instance attribute (context: system settings)
  setting = device.system_settings.where("timestamp BETWEEN ? AND ? ", timestamp_start_string, timestamp_end_string).order("timestamp ASC").first
  if setting.nil?
    setting = device.system_settings.where("timestamp <= ? ", timestamp_end_string).order("timestamp ASC").last
  end

  if setting.nil?
    arff_instance[:settings_airplane_mode] = '?'
    arff_instance[:settings_bluetooth] = '?'
    arff_instance[:settings_mode_ringer] = '?'
    arff_instance[:settings_volume_alarm] = '?'
    arff_instance[:settings_volume_music] = '?'
    arff_instance[:settings_volume_notification] = '?'
    arff_instance[:settings_volume_ring] = '?'
    arff_instance[:settings_volume_system] = '?'
    arff_instance[:settings_volume_voice] = '?'
    arff_instance[:settings_wifi] = '?'
    arff_instance[:settings_wifi_ssid] = '?'
  else
    # remove unnecessary characters in WIFI SSID
    setting.wifi_ssid.gsub!('"', '')
    setting.wifi_ssid.gsub!('<', '')
    setting.wifi_ssid.gsub!('>', '')

    arff_instance[:settings_airplane_mode] = (setting.airplane_mode.present?) ? "#{setting.airplane_mode}" : '?'
    if setting.bluetooth.present?
      arff_instance[:settings_bluetooth] = (setting.bluetooth == 1 || setting.bluetooth == 2) ? '1' : '0'
    end
    arff_instance[:settings_mode_ringer] = (setting.mode_ringer.present? && setting.mode_ringer != '-1') ? "#{setting.mode_ringer}" : '?'
    arff_instance[:settings_volume_alarm] = (setting.volume_alarm.present?) ? "#{setting.volume_alarm}" : '?'
    arff_instance[:settings_volume_music] = (setting.volume_music.present?) ? "#{setting.volume_music}" : '?'
    arff_instance[:settings_volume_notification] = (setting.volume_notification.present?) ? "#{setting.volume_notification}" : '?'
    arff_instance[:settings_volume_ring] = (setting.volume_ring.present?) ? "#{setting.volume_ring}" : '?'
    arff_instance[:settings_volume_system] = (setting.volume_system.present?) ? "#{setting.volume_system}" : '?'
    arff_instance[:settings_volume_voice] = (setting.volume_voice.present?) ? "#{setting.volume_voice}" : '?'
    arff_instance[:settings_wifi] = (setting.wifi.present? && setting.wifi != -1) ? "#{setting.wifi}" : '?'
    arff_instance[:settings_wifi_ssid] = (setting.wifi_ssid.present? && setting.wifi_ssid != '-1') ? "'#{setting.wifi_ssid}'" : '?'
  end
  # puts "Settings: #{arff_instance[:settings_airplane_mode]}/#{arff_instance[:settings_bluetooth]}/#{arff_instance[:settings_mode_ringer]}/#{arff_instance[:settings_volume_alarm]}/#{arff_instance[:settings_volume_music]}/#{arff_instance[:settings_volume_notification]}/#{arff_instance[:settings_volume_ring]}/#{arff_instance[:settings_volume_system]}/#{arff_instance[:settings_volume_voice]}/#{arff_instance[:settings_wifi]}/#{arff_instance[:settings_wifi_ssid]}"

  # Set instance classifier (prediction: app)
  if app_usage.nil? 
    arff_instance[:package_name] = '?'
    arff_instance[:app_category] = '?'
  else
    arff_instance[:package_name] = (app_usage.package_name.present?) ? "'#{app_usage.package_name}'" : '?'
    arff_instance[:app_category] = (app_usage.app_category.present?) ? "'#{app_usage.app_category}'" : '?'
  end
  # puts "Classifier: #{arff_instance[:package_name]}"

  # puts "--------------------------------------------------------------------------------"

  return arff_instance
end

