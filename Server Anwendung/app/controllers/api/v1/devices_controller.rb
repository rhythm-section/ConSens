module Api
  module V1
    class DevicesController < ApplicationController
      include DatabaseConnection

      public
        def create
          @device = Device.find_or_create_by(uuid: devices_params[:uuid])

          # add sytem info
          if devices_params[:system_info] != nil then
            @device.system_infos = SystemInfo.new(devices_params[:system_info])
          end

          # add app sessions
          if devices_params[:app_sessions] != nil then
            new_app_sessions = []
            devices_params[:app_sessions].each do |app_session|
              new_app_sessions << AppSession.new(app_session)
            end
            @device.app_sessions.push(new_app_sessions)
          end

          # add app usages
          if devices_params[:app_usages] != nil then
            new_app_usages = []
            devices_params[:app_usages].each do |app_usage|
              new_app_usages << AppUsage.new(app_usage)
            end
            @device.app_usages.push(new_app_usages)
          end

          # add system settings
          if devices_params[:system_settings] != nil then
            new_system_settings = []
            devices_params[:system_settings].each do |system_setting|
              new_system_settings << SystemSetting.new(system_setting)
            end
            @device.system_settings.push(new_system_settings)
          end

          # add user activities
          if devices_params[:user_activities] != nil then
            new_user_activities = []
            devices_params[:user_activities].each do |user_activity|
              new_user_activities << UserActivity.new(user_activity)
            end
            @device.user_activities.push(new_user_activities)
          end

          # add user locations
          if devices_params[:user_locations] != nil then
            new_user_locations = []
            devices_params[:user_locations].each do |user_location|
              new_user_locations << UserLocation.new(user_location)
            end
            @device.user_locations.push(new_user_locations)
          end

          respond_to do |format|
            if @device.save
              format.html { redirect_to api_devices_path }
              format.json { render json: 'data successfully added' }
            else
              format.html { render action: 'new' }
              format.json { render json: @device.errors.messages, status: 422 }
            end
          end
        end

        def new
          @device = Device.new
        end

      private
        def devices_params
          params.require(:device).permit(
            :uuid,
            {
              :system_info => [
                :os_version,
                :model,
                :product,
                :manufacturer,
                :brand,
                :android_id,
                :user_name
              ]
            },
            {
              :app_sessions => [
                :app_session_timestamp_start,
                :app_session_timestamp_end,
                :app_session_timestamp_duration,
                :server_sent
              ]
            },
            {
              :app_usages => [
                :timestamp_start,
                :timestamp_end,
                :timestamp_duration,
                :app_session_id,
                :app_name,
                :package_name,
                :version_name,
                :version_code,
                :base_activity,
                :top_activity,
                :first_install_time,
                :last_update_time,
                :server_sent
              ]
            },
            {
              :system_settings => [
                :settings_app_session_id,
                :settings_timestamp,
                :settings_airplane_mode,
                :settings_bluetooth,
                :settings_data_roaming,
                :settings_development_settings_enabled,
                :settings_http_proxy,
                :settings_mode_ringer,
                :settings_volume_alarm,
                :settings_volume_music,
                :settings_volume_notification,
                :settings_volume_ring,
                :settings_volume_system,
                :settings_volume_voice,
                :settings_network_preference,
                :settings_stay_on_while_plugged_in,
                :settings_usb_mass_storage_enabled,
                :settings_wifi,
                :settings_wifi_ssid,
                :settings_location_mode_3,
                :settings_location_mode_17,
                :server_sent
              ]
            },
            {
              :user_activities => [
                :activity_app_session_id,
                :activity_name,
                :activity_confidence,
                :activity_type,
                :activity_timestamp,
                :server_sent
              ]
            },
            {
              :user_locations => [
                :location_app_session_id,
                :location_timestamp,
                :location_elapsed_realtime_nanos,
                :location_provider,
                :location_latitude,
                :location_longitude,
                :location_altitude,
                :location_speed,
                :location_accuracy,
                :server_sent
              ]
            }
          )
        end
    end
  end
end