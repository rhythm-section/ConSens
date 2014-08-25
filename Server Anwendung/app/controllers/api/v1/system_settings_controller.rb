module Api
  module V1
    class SystemSettingsController < DevicesController
      include DatabaseConnection

      public

      private
        def system_setting_params
          params.permit(
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
          )
        end
    end
  end
end